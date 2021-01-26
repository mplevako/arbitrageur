// scala 2.13.4
import $file.prelude

import cats.Monoid

sealed trait MaxPlusDomain[+T] extends Any with java.io.Serializable

case object NegativeInfinity extends MaxPlusDomain[Nothing]

case class FiniteValue[T](value: T) extends AnyVal with MaxPlusDomain[T]

object MaxPlusDomain {
  def argMax[T](values: IndexedSeq[MaxPlusDomain[T]])(implicit ord: Ordering[T]): (Option[Int], MaxPlusDomain[T]) =
    if (values.isEmpty) (None, NegativeInfinity)
    else {
      var m: Int = 1
      var maxIdx: Int = 0
      var max: MaxPlusDomain[T] = values(maxIdx)
      while (m < values.size) {
        (max, values(m)) match {
          case (FiniteValue(x), acc@FiniteValue(a)) =>
            if (ord.lt(x, a)) {
              maxIdx = m
              max = acc
            }
          case (NegativeInfinity, acc) =>
            maxIdx = m
            max = acc
          case (_, NegativeInfinity) => ()
        }
        m += 1
      }
      (Some(maxIdx), max)
    }
}

class MaxPlusTropicalSquareMatrix[T](private val matrix: Array[Array[MaxPlusDomain[T]]],
                                     private val walks: Map[(Int, Int), IndexedSeq[Int]])
                                    (implicit additive: Monoid[T], ord: Ordering[T]) {
  val dimension: Int = matrix.length

  def apply(i: Int, j: Int): MaxPlusDomain[T] = matrix(i)(j)

  def walk(from: Int, to: Int): Option[IndexedSeq[Int]] = walks.get(from -> to)

  private def update(i: Int, j: Int, value: MaxPlusDomain[T]): Unit = {
    matrix(i)(j) = value
  }

  def *(multiplier: MaxPlusTropicalSquareMatrix[T]): MaxPlusTropicalSquareMatrix[T] = {
    require(multiplier.dimension == dimension)
    val product = Array.fill[MaxPlusDomain[T]](dimension, dimension)(NegativeInfinity)
    var walks = scala.collection.mutable.ListBuffer.empty[((Int, Int), IndexedSeq[Int])]
    if (dimension > 0) {
      val accumulator = Array.ofDim[MaxPlusDomain[T]](dimension)
      var i = 0
      while (i < dimension) {
        var j = 0
        while (j < dimension) {
          var k = 0
          while (k < dimension) {
            accumulator(k) = (this (i, k), multiplier(k, j)) match {
              case (NegativeInfinity, _) => NegativeInfinity
              case (_, NegativeInfinity) => NegativeInfinity
              case (FiniteValue(lv), FiniteValue(rv)) => FiniteValue(additive.combine(lv, rv))
            }
            k += 1
          }
          MaxPlusDomain.argMax(accumulator) match {
            case (_, NegativeInfinity) => ()
            case (Some(maxIndex), max: FiniteValue[T]) =>
              product(i)(j) = max
              walk(i, maxIndex).foreach(walk => walks=walks :+ (i -> j) -> (walk :+ j))
          }
          j += 1
        }
        i += 1
      }
    }
    new MaxPlusTropicalSquareMatrix[T](product, Map.from(walks))
  }
}

object MaxPlusTropicalSquareMatrix {
  def apply[T](dimension: Int, values: Map[(Int, Int), FiniteValue[T]])
              (implicit additive: Monoid[T], ord: Ordering[T]) = {
    val matrix = Array.fill[MaxPlusDomain[T]](dimension, dimension)(NegativeInfinity)
    var walks = scala.collection.mutable.ListBuffer.empty[((Int, Int), IndexedSeq[Int])]
    values.foreachEntry { case ((from, to), value) =>
      matrix(from)(to) = value
      walks = walks :+ (from -> to) -> IndexedSeq(from, to)
    }

    new MaxPlusTropicalSquareMatrix[T](matrix, Map.from(walks))
  }
}