// scala 2.13.4
import $file.prelude
import $file.^.currency, ^.currency._
import $file.^.fxrate, ^.fxrate._

import cats.syntax.all._
import currency._
import eu.timepit.refined.api.Validate
import eu.timepit.refined.refineV
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import supertagged.postfix._

def isValid[P]: IsValidPartiallyApplied[P] = new IsValidPartiallyApplied

class IsValidPartiallyApplied[P] {
  def apply[T](t: T)(implicit v: Validate[T, P]): Boolean = v.isValid(t)
}

def notValid[P]: NotValidPartiallyApplied[P] = new NotValidPartiallyApplied

class NotValidPartiallyApplied[P] {
  def apply[T](t: T)(implicit v: Validate[T, P]): Boolean = v.notValid(t)
}

val nonEmptyUppercaseStrings: Gen[String] = nonEmptyListOf(alphaUpperChar).map(_.mkString)
val emptyOrNonUppercaseStrings: Gen[String] = someOf(numChar, alphaLowerChar).map(_.mkString)

val nonEmptyUppercaseStringsPairs = for {
  base <- nonEmptyUppercaseStrings.map(refineV[ValidCurrencyCode](_) @@ Base)
  quote <- nonEmptyUppercaseStrings.map(refineV[ValidCurrencyCode](_) @@ Quote)
} yield (base, quote).tupled

"Non empty uppercase strings are valid currency codes" |:
  forAll(nonEmptyUppercaseStrings)(isValid[ValidCurrencyCode](_)).check

"Empty strings or strings containing digits and/or lowercase characters are invalid" |:
  forAll(emptyOrNonUppercaseStrings)(notValid[ValidCurrencyCode](_)).check

"Pairs of non empty uppercase strings joined with _ are valid currency pair tickers" |:
  forAll(nonEmptyUppercaseStringsPairs) { pair =>
    secure(pair match {
      case Right((base, quote)) =>
        currencyPairDecoder(s"${base.value}_${quote.value}").contains((base, quote))
    })
  }.check

"Pairs of non empty uppercase strings joined with _ are valid currency pair tickers" |:
  forAll(emptyOrNonUppercaseStrings, emptyOrNonUppercaseStrings) { (base, quote) =>
    secure(currencyPairDecoder(s"${base}_$quote") ?= None)
  }.check