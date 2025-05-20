ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.0"

lazy val root = (project in file("."))
  .settings(
    name := "UrlShortener",
    fork := true,
    libraryDependencies ++= globalDependencies
  )

lazy val tapirVersion = "1.11.28"
lazy val catsCoreVersion = "2.13.0"
lazy val catsEffectVersion = "3.6.1"
lazy val http4sVersion = "0.23.17"
lazy val swaggerVerion = "5.21.0"


lazy val globalDependencies = tapirDependencies ++ catsDeps ++ http4sDeps

lazy val tapirDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
  "com.softwaremill.sttp.tapir" %% "tapir-core",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"
).map(_ % tapirVersion)

lazy val catsDeps = Seq(
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion
)

lazy val http4sDeps = Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion
)