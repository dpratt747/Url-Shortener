package services

import cats.effect.IO
import persistence.UrlShortenerDBAlg

trait UrlShortenerServiceAlg {
  def storeLongURLAndRetrieveShortUrl(longUrl: String): IO[Option[String]]
  def getLongUrl(shortUrl: String): IO[Option[String]]
}

final class UrlShortenerService private(db: UrlShortenerDBAlg) extends UrlShortenerServiceAlg {
  override def storeLongURLAndRetrieveShortUrl(longUrl: String): IO[Option[String]] =
    db.storeUrl(longUrl) *> db.getShortenedUrl(longUrl)

  override def getLongUrl(shortUrl: String): IO[Option[String]] =
    db.getLongUrl(shortUrl)
}

object UrlShortenerService {
  def make(db: UrlShortenerDBAlg): UrlShortenerServiceAlg = {
    UrlShortenerService(db)
  }
}