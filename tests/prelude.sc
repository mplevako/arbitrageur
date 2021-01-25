// scala 2.13.4
import $file.^.prelude, ^.prelude.refined

val scalacheck = "1.15.2"

interp.load.ivy("eu.timepit" %% "refined-scalacheck" % refined)
interp.load.ivy("org.scalacheck" %% "scalacheck" % scalacheck)