// scala 2.13.4
import $file.currency, currency._

import atto.Atto.toParserOps
import cats.effect.{BracketThrow, Sync}
import cats.syntax.all._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.numeric.PosBigDecimal
import io.circe.KeyDecoder
import io.circe.refined._
import org.http4s._
import org.http4s.circe.{JsonDecoder, jsonOf}
import org.http4s.client.Client

implicit val currencyPairDecoder: KeyDecoder[CurrencyPair] = currencyPair.parseOnly(_).option

trait FXRatesClient[F[_]] {
  def getFXRates(uri: String Refined Url): F[Map[CurrencyPair, PosBigDecimal]]
}

final class FXRatesClientInterpreter[F[_] : JsonDecoder : BracketThrow : Sync](client: Client[F]) extends FXRatesClient[F] {
  override def getFXRates(uri: String Refined Url): F[Map[CurrencyPair, PosBigDecimal]] =
    Uri.fromString(uri.value).liftTo[F].flatMap { uri =>
      client.expect(uri)(jsonOf[F, Map[CurrencyPair, PosBigDecimal]])
    }
}