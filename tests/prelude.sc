// scala 2.13.4
import $file.^.prelude, ^.prelude.circe, ^.prelude.refined

val scalacheck = "1.15.2"
val scalatest = "3.2.3"

interp.load.ivy("eu.timepit" %% "refined-scalacheck" % refined)
interp.load.ivy("org.scalacheck" %% "scalacheck" % scalacheck)
interp.load.ivy("org.scalatest" %% "scalatest" % scalatest)