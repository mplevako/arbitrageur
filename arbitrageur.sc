// scala 2.13.4
import $file.prelude
import $file.currency, currency._
import $file.fxrate, fxrate._
import $file.matrix, matrix._

import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.option._
import ch.obermuhlner.math.big.DefaultBigDecimalMath
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.numeric.PosBigDecimal
import fxrate.FXRatesClientInterpreter
import matrix.{FiniteValue, MaxPlusTropicalSquareMatrix}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect

import java.math.BigDecimal.{ONE, ZERO}

@main
def main(@arg(doc = "the URL of an exchange rate provider that returns rates as a JSON object with \"currency code\"" +
                    " pairs joined through the _ character as the names and the rates as the values, e.g.:\n" +
                    "{\"QUATLOOS_GOLDPRESSEDLATINUM\":0.0072973525628, \"GOLDPRESSEDLATINUM_QUATLOOS\":1.570359992057584240145136,\n" +
                    "\"NUYEN_EURODOLLAR\":2.718281828459045235360287471352662497757247093,\n" +
                    "\"FLANIANPOBBLEBEAD_FLANIANPOBBLEBEAD\":1.0}") url: String,
         @arg(doc = "the maximum number of URL redirections to follow") maxRedirects: Int = 3): Unit = {
  import scala.concurrent.ExecutionContext.global

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  refineV[Url](url).fold(validationError => IO.raiseError(new Throwable(validationError)), url =>
    BlazeClientBuilder[IO](global).resource.map(FollowRedirect(maxRedirects)).use(
      new FXRatesClientInterpreter(_).getFXRates(url)
    ).map { fxRates =>
      val (index2Currency, distanceMatrix) = buildDistanceMatrix(fxRates)

      var (highestPositiveExponent, bestStrategy) = (ZERO, none[Seq[CurrencyCode]])

      var powerOfDistanceMatrix = distanceMatrix
      val values = Array.ofDim[MaxPlusDomain[BigDecimal]](powerOfDistanceMatrix.dimension)
      for (_ <- 1 until powerOfDistanceMatrix.dimension) {
        powerOfDistanceMatrix *= distanceMatrix
        for (i <- 0 until powerOfDistanceMatrix.dimension) {
          values(i) = powerOfDistanceMatrix.walk(i, i).filter(isSimpleCycle).map(_ => powerOfDistanceMatrix(i, i)).getOrElse(NegativeInfinity)
        }
        MaxPlusDomain.argMax(values) match {
          case (_, NegativeInfinity) => ()
          case (Some(maxIndex), max: FiniteValue[BigDecimal]) =>
            if (max.value.bigDecimal.compareTo(highestPositiveExponent) > 0) {
              highestPositiveExponent = max.value.bigDecimal
              bestStrategy = powerOfDistanceMatrix.walk(maxIndex, maxIndex).map(_.map(index2Currency))
            }
        }
      }

      System.out.println(
        bestStrategy.fold("There are no arbitrage opportunities")(strategy =>
          s"""The "${strategy.mkString("->")}" trade yields the highest positive return of """ +
            s"""${DefaultBigDecimalMath.exp(highestPositiveExponent).subtract(ONE)} on one unit of "currency" given the""" +
            s" following conversion rates: \n${fxRates.mkString("\n")}"
        ))
    }
  ).unsafeRunSync()
}

def buildDistanceMatrix(fxRates: Map[CurrencyPair, PosBigDecimal]):
(Map[Int, CurrencyCode], MaxPlusTropicalSquareMatrix[BigDecimal]) = {
  val currency2index = scala.collection.mutable.Map.empty[CurrencyCode, Int]

  var nextIndex = 0

  def nextAvailableIdx: Int = {
    val idx = nextIndex
    nextIndex += 1
    idx
  }

  val matrixValues = fxRates.filter(_._2.value != ZERO).map { case (currencyPair, rate) =>
    val (base, quote) = currencyPair
    val from = currency2index.getOrElseUpdate(base, nextAvailableIdx)
    val to = currency2index.getOrElseUpdate(quote, nextAvailableIdx)

    ((from, to), FiniteValue(BigDecimal(DefaultBigDecimalMath.log(rate.value.bigDecimal))))
  }

  val index2currency = Map.from(currency2index.map{case (currency, index) => index -> currency})

  (index2currency, MaxPlusTropicalSquareMatrix[BigDecimal](nextIndex, matrixValues))
}

def isSimpleCycle(cycle: IndexedSeq[Int]): Boolean = {
  val next = scala.collection.mutable.Map.empty[Int, Int]
  for (i <- 0 until cycle.size - 1) next(cycle(i)) = cycle(i + 1)
  val start = cycle(0)
  var (slow, fast) = (next(start), next(next(start)))
  while (slow != fast) {
    slow = next(slow)
    fast = next(next(fast))
  }

  var mu = 0
  slow = start
  while (slow != fast) {
    slow = next(slow)
    fast = next(fast)
    mu += 1
  }

  var lambda = 1
  fast = next(slow)
  while (slow != fast) {
    fast = next(fast)
    lambda += 1
  }
  mu == 0 && lambda == cycle.size - 1
}