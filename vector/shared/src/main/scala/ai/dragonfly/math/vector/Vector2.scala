package ai.dragonfly.math.vector

import ai.dragonfly.math.example.Demonstrable
import ai.dragonfly.math.*


/**
 * Created by clifton on 1/10/17.
 */

object Vector2 extends VectorCompanion[Vector2] with Demonstrable {

  inline given dimension: Int = 2

  override inline def validDimension(dimension: Int): Boolean = dimension == this.dimension

  override def apply(values:VectorValues): Vector2 = {
    if (values.length == 2) new Vector2(values)
    else throw UnsupportedVectorDimension(values.length)
  }

  def apply(x:Double, y:Double): Vector2 = Vector2(VectorValues(x, y))

  override def demo(implicit sb:StringBuilder = new StringBuilder()):StringBuilder = {
    for (deg <- Array[Double](10, 25, 33.333333, 45, 60, 75, 90)) {
      val i = Vector2(1, 0)
      i.rotateDegrees(deg)
      sb.append(s"${Vector2(1, 0)}.rotateDegrees($deg) -> $i\n")
    }
    sb
  }

  override def name: String = "Vector2"

}

case class Vector2 private (values:VectorValues) extends Vector {

  type VEC = Vector2

  inline def x:Double = values(0)
  inline def y:Double = values(1)

  inline def pseudoCross(v: Vector2): Double = x * v.y + y * v.x

  inline def rotateDegrees(degrees: Double): Vector2 = rotate(degreesToRadians(degrees))

  inline def rotate(radians: Double): Vector2 = {
    val cos = Math.cos( radians )
    val sin = Math.sin( radians )

    val x1 = x*cos - y*sin
    values(1) = x*sin + y*cos
    values(0) = x1

    this
  }

  override def copy(): VEC = Vector2(x, y)

  override def toString: String = s"《²↗〉${x}ᵢ ${y}ⱼ〉" // ₂⃗ ²↗ ↗²

}
