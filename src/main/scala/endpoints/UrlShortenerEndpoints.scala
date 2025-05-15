package endpoints

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*
import services.*
import sttp.tapir.server.ServerEndpoint.Full
import cats.syntax.all.*
import config.Config


final class UrlShortenerEndpoints(service: UrlShortenerServiceAlg) {

  final case class UrlShortenerRequest(longUrl: String)

  final case class UrlShortenerResponse(originalUrl: String, shortUrl: String)

  private val getShortenedUrlEndpoint = endpoint
    .post
    .in("shorten") //todo: prepend "/v1" to the request
    .tag("Shorten your URL here")
    .in(jsonBody[UrlShortenerRequest]
      .description("The request json body for retrieving a shorter URL")
      .example(UrlShortenerRequest("https://www.reddit.com/r/scala/comments/12ijqng/scala_tooling_summit_recap/")))
    .out(jsonBody[UrlShortenerResponse])
    .errorOut(stringBody)
    .description("This endpoint will take a long URL and return a potentially shorter URL")

  /**
   *  Business logic for the endpoints
   */

  private val storeAndRetrieveShortenedUrl: UrlShortenerRequest => IO[Either[String, UrlShortenerResponse]] = { request =>
    service.storeLongURLAndRetrieveShortUrl(request.longUrl).map {
      case Some(shortUrl) => UrlShortenerResponse(request.longUrl, s"http://${Config.host}:${Config.port}/$shortUrl").asRight[String]
      case None => "Unable to retrieve the short url".asLeft[UrlShortenerResponse]
    }
  }

  val serverEndpoints: List[Full[Unit, Unit, UrlShortenerRequest, String, UrlShortenerResponse, Any, IO]] = List(
    getShortenedUrlEndpoint.serverLogic(storeAndRetrieveShortenedUrl)
  )

}

object UrlShortenerEndpoints {
  def make(service: UrlShortenerServiceAlg): UrlShortenerEndpoints = {
    UrlShortenerEndpoints(service)
  }
}
