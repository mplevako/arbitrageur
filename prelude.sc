// scala 2.13.4
val atto="0.9.0"
val cats="2.3.1"
val catsEffect = "2.3.1"
val circe="0.13.0"
val http4s="1.0.0-M10"
val refined = "0.9.20"
val supertagged="2.0-RC2"

interp.load.ivy("org.tpolecat" %% "atto-core" % atto)
interp.load.ivy("org.tpolecat" %% "atto-refined" % atto)
interp.load.ivy("org.typelevel" %% "cats-core" % cats)
interp.load.ivy("org.typelevel" %% "cats-effect" % catsEffect)
interp.load.ivy("io.circe" %% "circe-core" % circe)
interp.load.ivy("io.circe" %% "circe-refined" % circe)
interp.load.ivy("org.http4s" %% "http4s-circe" % http4s)
interp.load.ivy("org.http4s" %% "http4s-dsl" % http4s)
interp.load.ivy("org.http4s" %% "http4s-blaze-client" % http4s)
interp.load.ivy("eu.timepit" %% "refined" % refined)
interp.load.ivy("org.rudogma" %% "supertagged" % supertagged)

private val scalacOptions = List(
  "-Xfatal-warnings",
  "-deprecation",
  "-explaintypes",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard"
)

interp.configureCompiler(_.settings.processArguments(scalacOptions, true))

