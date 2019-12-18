package hubmodel.ped

import hubmodel.ped.History.{CoordinateTracking, CoordinateGroup, HistoryContainer}
import hubmodel.ped.PedestrianTrajectory
import hubmodel.{Position}
import tools.Time
import myscala.math.vector.ZeroVector2D

import scala.collection.mutable

/** Empirical pedestrian which extends [[hubmodel.ped.PedestrianTrajectory]] with some extra members.
  *
  * @param ID              Unique ID (unique for a given day)
  * @param entryTime       time stamp of the fist occurence of the pedestrian
  */
class Pedestrian(val ID: Int, val entryTime: Time) extends PedestrianTrajectory {


  /* ---------- Members -----------*/

  // Unique identifier
  val UUID: String = java.util.UUID.randomUUID.toString

  // entrance point
  var originPosition: Position = new ZeroVector2D

  // entrance zone
  var originZone: Int = 0

  // destination point
  var destinationPosition: Position = new ZeroVector2D

  // destination zone
  var destinationZone: Int = 0

  // total travel time
  private lazy val travelTime: Time = exitTime - entryTime

  // total travel distance
  private lazy val travelDistance: Double =  {
    _historyPosition.dropRight(1).zip(_historyPosition.tail).foldRight(0.0)((v, acc) => acc + (v._2._2.pos - v._1._2.pos).norm)
  }

  // exit time
  lazy val exitTime: Time = Time(_historyPosition.foldRight(0.0)((xyt, acc) => acc.max(xyt._1.value.toDouble)))

  // average velocity
  private lazy val meanVelocity: Double = travelDistance/travelTime.value.toDouble


  // All trajectory points
  protected val _historyPositionUnsorted: collection.mutable.ArrayBuffer[(Time, HistoryContainer)] = collection.mutable.ArrayBuffer()

  // Sorted pedestrian trajectory
  protected lazy val _historyPosition: Vector[(Time, HistoryContainer)] = this._historyPositionUnsorted.toVector//.sortBy(_._1)

  /* ---------- Methods -----------*/

  def updatePositionHistory(t: tools.Time, pos: Position): Unit = {
    this._historyPositionUnsorted.append((t, CoordinateTracking(pos)))
  }

  def updatePositionHistory(t: tools.Time, pos: Position, group: Int): Unit = {
    this._historyPositionUnsorted.append((t, CoordinateGroup(pos, group)))
  }



}