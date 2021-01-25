// scala 2.13.4
import $file.prelude

import atto.Atto.{char, endOfInput, takeWhile1, toParserOps}
import atto.Parser
import atto.syntax.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.char.UpperCase
import eu.timepit.refined.collection.{Forall, NonEmpty}
import eu.timepit.refined.predicates.boolean.And
import supertagged.lift.LiftF
import supertagged.{@@, TaggedType}

type ValidCurrencyCode = NonEmpty And Forall[UpperCase]
type CurrencyCode = String Refined ValidCurrencyCode

object Base extends TaggedType[CurrencyCode]

object Quote extends TaggedType[CurrencyCode]

type Base = Base.Type
type Quote = Quote.Type
type CurrencyPair = (CurrencyCode @@ Base, CurrencyCode @@ Quote)

implicit def currencyCode[T](role: String): Parser[CurrencyCode @@ T] =
  LiftF[Parser].lift(takeWhile1(_.isUpper).namedOpaque(role).refined[ValidCurrencyCode])

val currencyPair: Parser[CurrencyPair] =
  (currencyCode[Base]("base") <~ char('_')) ~ currencyCode[Quote]("quote") <~ endOfInput