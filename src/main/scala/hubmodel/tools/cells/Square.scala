package hubmodel.tools.cells

import hubmodel.{Position, generateUUID}
import myscala.math.vector.Vector2D

class Square(name: String, A: Position, B: Position, C: Position, D: Position) extends Rectangle(name, A, B, C, D) {

  def this(center: Position, side: Double) {
    this(
      generateUUID,
      center + Vector2D(-0.5 * side, -0.5 * side),
      center + Vector2D(0.5 * side, -0.5 * side),
      center + Vector2D(0.5 * side, 0.5 * side),
      center + Vector2D(-0.5 * side, 0.5 * side)
    )
  }
}