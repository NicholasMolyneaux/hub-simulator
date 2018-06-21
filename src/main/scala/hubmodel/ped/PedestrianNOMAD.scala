package hubmodel.ped

import java.util

import hubmodel.{Acceleration, Direction, Position, Time}
import hubmodel.TimeNumeric.mkOrderingOps
import hubmodel.supply.continuous.Wall
import hubmodel.tools.cells.Rectangle
import myscala.math.vector.{Vector2D, ZeroVector2D}
import nl.tudelft.pedestrians.agents.WalkingBehavior
import nl.tudelft.pedestrians.collection.InfluenceAreaReturnPedData
import nomad.operational.InfluenceAreaReturnObsData
import javax.vecmath.Vector3d

class PedestrianNOMAD(oZone: Rectangle, dZone: Rectangle, entryTime: Time, posO: Position, route: List[Rectangle]) extends PedestrianSim(oZone, dZone, entryTime, posO, route) {

  def isVariableStep: Boolean = {true}
  var isolationTypePed: Int = 0
  var isolationTypeObs: Int = 0
  val isInvisible: Boolean = false
  var isolationTimeObs: Double = entryTime.value
  var isolationTimePed: Double = entryTime.value
  val AT: Double = 0.5
  val infAreaMaxExtObs: Double = 1.0
  var desiredDirection: Vector2D = new ZeroVector2D
  val tau: Double = 0.15
  val isStochastic: Boolean = false
  val noise: Double = 0.0001
  var acceleration: Acceleration = new ZeroVector2D
  var nextPosition: Vector2D = new ZeroVector2D
  var nextVelocity: Vector2D = new ZeroVector2D
  val aw: Double = 10
  val s0: Double = 0.26
  val getRadius: Double = this.r

  def updatePreviousPositionAndSpeed(t: Time): Unit = { this.addHistory(t) }

  /** Computes the direction based on the current position and the target position
    *
    * @param pos  current position
    * @param goal target position
    * @return normalized directiondesiredDirection
    */
  protected def computeDesiredDirection(pos: Position, goal: Position): Direction = {
    (goal - pos) / (goal - pos).norm
  }

  protected def computePathFollowingComponent(p: PedestrianSim): Acceleration = {
    val tau: Double = 0.62
    (computeDesiredDirection(p.currentPosition, p.currentDestination) * p.freeFlowVel - p.currentVelocity) / tau
  }

  def   updateDesiredSpeed(): Unit = {
    this.desiredDirection =  computeDesiredDirection(this.currentPosition, this.currentDestination) //computePathFollowingComponent(this).normalized
  }

}