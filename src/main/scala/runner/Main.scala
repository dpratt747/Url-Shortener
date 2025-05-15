package runner

import cats.effect.{ExitCode, IO, IOApp, Ref}
import config.Config
import endpoints.{RedirectEndpoints, UrlShortenerEndpoints}
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.blaze.server.BlazeServerBuilder
import persistence.UrlShortenerInMemoryDB
import services.{UrlShortenerService, UrlShortenerServiceAlg}
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.time.ZonedDateTime


object Main extends IOApp {

  import cats.syntax.all.*

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      /**
       * Modules
       */
      ref <- Ref[IO].of(scala.collection.immutable.HashMap[String, (String, ZonedDateTime)]())
      db = UrlShortenerInMemoryDB.make(ref)
      service = UrlShortenerService.make(db)
      urlShortenerEndpoints = UrlShortenerEndpoints.make(service)
      redirectEndpoints = RedirectEndpoints.make(service)

      /**
       * Swagger routes
       */
      swaggerRoutes = SwaggerInterpreter(
        swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = List("v1"))
      ).fromServerEndpoints(
        endpoints = urlShortenerEndpoints.serverEndpoints, // todo: add new endpoints here
        title = "Url Shortener API",
        version = "1.0"
      )
      swaggerHttp4sRoutes = Http4sServerInterpreter[IO]().toRoutes(swaggerRoutes)

      /**
       * Non-swagger routes
       */
      // todo: add new endpoints here
      http4sRoutes = Http4sServerInterpreter[IO]().toRoutes(
        urlShortenerEndpoints.serverEndpoints
      )
      redirectRoutes = Http4sServerInterpreter[IO]().toRoutes(
        redirectEndpoints.serverEndpoints
      )

      // todo: add a scheduled service that clears the cache? only clear values that have been persisted for 15 mins and more
      httpApp = Router(
        "/v1" -> (http4sRoutes <+> swaggerHttp4sRoutes),
        "/" -> redirectRoutes,
      ).orNotFound

      _ <- IO.println(s"Server is up and running on the following address: http://${Config.host}:${Config.port}")
      _ <- BlazeServerBuilder[IO].bindHttp(Config.port, Config.host).withHttpApp(httpApp).serve.compile.drain
    } yield ExitCode.Success
  }
}
