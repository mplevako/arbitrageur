// scala 2.13.4
val atto = "0.9.0"
val refined = "0.9.20"
val supertagged = "2.0-RC2"

interp.load.ivy("org.tpolecat" %% "atto-core" % atto)
interp.load.ivy("org.tpolecat" %% "atto-refined" % atto)
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

