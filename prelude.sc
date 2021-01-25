// scala 2.13.4
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