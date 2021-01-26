// scala 2.13.4
import $file.prelude

import $file.^.matrix, ^.matrix._

import org.scalatest.matchers.should
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.propspec.AnyPropSpec

object matrixspec extends AnyPropSpec with TableDrivenPropertyChecks with should.Matchers {
  private val samples = Table(
    ("d", "d-by-d distance matrix M", "M to the dth", "longest d-step walks"),
    (
      4,
      Map(
        (0 -> 2, FiniteValue(1.0)), (0 -> 3, FiniteValue(.75)),
        (1 -> 0, FiniteValue(.75)),
        (2 -> 0, FiniteValue(1.0)), (2 -> 1, FiniteValue(.75)),
        (3 -> 2, FiniteValue(.75))
      ),
      Array(
        Array(FiniteValue(4.0), FiniteValue(3.75), FiniteValue(3.5), FiniteValue(3.25)),
        Array(FiniteValue(3.25), FiniteValue(3.0), FiniteValue(3.75), FiniteValue(3.5)),
        Array(FiniteValue(3.5), FiniteValue(3.25), FiniteValue(4.0), FiniteValue(3.75)),
        Array(FiniteValue(3.75), FiniteValue(3.5), FiniteValue(3.25), FiniteValue(3.0))
      ),
      Array(
        Array(Seq(0, 2, 0, 2, 0), Seq(0, 2, 0, 2, 1), Seq(0, 2, 1, 0, 2), Seq(0, 2, 1, 0, 3)),
        Array(Seq(1, 0, 2, 1, 0), Seq(1, 0, 3, 2, 1), Seq(1, 0, 2, 0, 2), Seq(1, 0, 2, 0, 3)),
        Array(Seq(2, 0, 2, 1, 0), Seq(2, 1, 0, 2, 1), Seq(2, 0, 2, 0, 2), Seq(2, 0, 2, 0, 3)),
        Array(Seq(3, 2, 0, 2, 0), Seq(3, 2, 0, 2, 1), Seq(3, 2, 1, 0, 2), Seq(3, 2, 1, 0, 3))
      )
    ),
    (
      4,
      Map(
        (0 -> 1, FiniteValue(.1)), (0 -> 2, FiniteValue(.6)), (0 -> 3, FiniteValue(1.0)),
        (1 -> 0, FiniteValue(.2)), (1 -> 2, FiniteValue(.3)),
        (2 -> 0, FiniteValue(.5)), (2 -> 1, FiniteValue(.4)), (2 -> 3, FiniteValue(.7)),
        (3 -> 0, FiniteValue(.9)), (3 -> 2, FiniteValue(.8))
      ),
      Array(
        Array(FiniteValue(3.8), FiniteValue(2.9), FiniteValue(3.7), FiniteValue(3.3)),
        Array(FiniteValue(2.7), FiniteValue(2.4), FiniteValue(2.7), FiniteValue(3.1)),
        Array(FiniteValue(3.5), FiniteValue(2.6999999999999997), FiniteValue(3.4000000000000004), FiniteValue(3.4)),
        Array(FiniteValue(3.2), FiniteValue(3.1), FiniteValue(3.4), FiniteValue(3.8))
      ),
      Array(
        Array(Seq(0, 3, 0, 3, 0), Seq(0, 3, 0, 2, 1), Seq(0, 3, 0, 3, 2), Seq(0, 3, 2, 0, 3)),
        Array(Seq(1, 2, 0, 3, 0), Seq(1, 0, 3, 2, 1), Seq(1, 0, 3, 0, 2), Seq(1, 0, 3, 0, 3)),
        Array(Seq(2, 3, 0, 3, 0), Seq(2, 0, 3, 2, 1), Seq(2, 3, 0, 3, 2), Seq(2, 0, 3, 0, 3)),
        Array(Seq(3, 0, 3, 2, 0), Seq(3, 0, 3, 2, 1), Seq(3, 0, 3, 0, 2), Seq(3, 0, 3, 0, 3))
      )
    )
  )
  property("The dth power of a d-by-d maxplus tropical distance matrix should contain all pairs longest d-step walks") {
    forAll(samples) { (dimension, values, mD, walks) =>
      val matrix = MaxPlusTropicalSquareMatrix[Double](dimension, values)
      var matrixPower = matrix
      for (_ <- 1 until dimension) matrixPower *= matrix
      for {
        i <- 0 until dimension
        j <- 0 until dimension
      } {
        matrixPower(i, j) should ===(mD(i)(j))
        matrixPower.walk(i, j).foreach(_ should ===(walks(i)(j)))
      }
    }
  }
}

org.scalatest.run(matrixspec)