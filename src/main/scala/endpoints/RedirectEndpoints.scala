package endpoints

import cats.effect.IO
import cats.syntax.all.*
import services.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint.Full


final class RedirectEndpoints(service: UrlShortenerServiceAlg) {

  private val redirectEndpoint = endpoint
    .get
    .in(path[String]("endpoint"))
    .out(
      statusCode(StatusCode.TemporaryRedirect)
        .and(header[String]("Location"))
    )
    .errorOut(stringBody)
    .description("This endpoint will redirect the user to the persisted long url")

  /**
   * Business logic for the endpoints
   */
  private val getLongUrlAndRedirect: String => IO[Either[String, String]] = { shortUrl =>
    service.getLongUrl(shortUrl).map {
      case Some(longUrl) => longUrl.asRight[String]
      case None => "Some issue occurred whilst getting the long url and redirection did not work".asLeft[String]
    }
  }

  val serverEndpoints: List[Full[Unit, Unit, String, String, String, Any, IO]] = List(
    redirectEndpoint.serverLogic(getLongUrlAndRedirect)
  )

}

object RedirectEndpoints {
  def make(service: UrlShortenerServiceAlg): RedirectEndpoints = {
    RedirectEndpoints(service)
  }
}
