// scala 2.13.4
import $file.prelude
import $file.^.currency, ^.currency._

import eu.timepit.refined.api.Validate
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck.Gen

class IsValidPartiallyApplied[P] {
  def apply[T](t: T)(implicit v: Validate[T, P]): Boolean = v.isValid(t)
}

class NotValidPartiallyApplied[P] {
  def apply[T](t: T)(implicit v: Validate[T, P]): Boolean = v.notValid(t)
}

def isValid[P]: IsValidPartiallyApplied[P] = new IsValidPartiallyApplied
def notValid[P]: NotValidPartiallyApplied[P] = new NotValidPartiallyApplied

val nonEmptyUppercaseStrings: Gen[String] = nonEmptyListOf(alphaUpperChar).map(_.mkString)
val emptyOrNonUppercaseStrings: Gen[String] = someOf(numChar, alphaLowerChar).map(_.mkString)

"Non empty uppercase strings are valid currency codes" |:
  forAll(nonEmptyUppercaseStrings)(isValid[ValidCurrencyCode](_)).check

"Empty strings or strings containing digits and/or lowercase characters are invalid" |:
  forAll(emptyOrNonUppercaseStrings)(notValid[ValidCurrencyCode](_)).check