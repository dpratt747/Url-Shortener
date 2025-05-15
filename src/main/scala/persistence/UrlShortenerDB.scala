package persistence

import cats.effect.{IO, Ref}
import cats.syntax.all.*
import persistence.UrlShortenerInMemoryDB.*

import java.time.ZonedDateTime
import scala.util.Random

trait UrlShortenerDBAlg {
  def storeUrl(longUrl: String): IO[Unit]

  def getShortenedUrl(longUrl: String): IO[Option[String]]

  def getLongUrl(shortUrl: String): IO[Option[String]]
}

final class UrlShortenerInMemoryDB private(ref: Ref[IO, scala.collection.immutable.HashMap[String, (String, ZonedDateTime)]]) extends UrlShortenerDBAlg {

  /**
   * This function will generate a short alphanumeric value for the provided longUrl, if the long url is already
   * present in the hashmap no action occurs
   */
  override def storeUrl(longUrl: String): IO[Unit] =
    ref.getAndUpdate { map =>
      map.updatedWith(longUrl) {
        case None => (randomAlphanumeric(5), ZonedDateTime.now()).some
        case Some(value) => value.some
      }
    }.void

  override def getShortenedUrl(longUrl: String): IO[Option[String]] =
    for {
      map <- ref.get
      valuesOpt = map.get(longUrl)
      shortUrlOpt = valuesOpt.map { case (shortUrl, _) => shortUrl }
    } yield shortUrlOpt

  override def getLongUrl(shortUrl: String): IO[Option[String]] = {
    for {
      map <- ref.get
      longUrl = map.collectFirst { case (key, (`shortUrl`, _)) => key }
    } yield longUrl
  }

}

object UrlShortenerInMemoryDB {
  def make(ref: Ref[IO, scala.collection.immutable.HashMap[String, (String, ZonedDateTime)]]): UrlShortenerDBAlg = {
    UrlShortenerInMemoryDB(ref)
  }

  private def randomAlphanumeric(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    Random.shuffle(chars).take(length).mkString
  }
}