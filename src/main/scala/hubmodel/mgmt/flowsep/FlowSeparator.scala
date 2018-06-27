package hubmodel.mgmt.flowsep

import hubmodel.supply.continuous.{MovableWall, SINGLELINE, Wall}
import hubmodel.supply.graph.MyEdge
import hubmodel.tools.cells.RectangleModifiable
import hubmodel.{Position, Time, generateUUID}

class FlowSeparator(val startA: Position,
                    val startB: Position,
                    val endA: Position,
                    val endB: Position,
                    val inflowLinesStart: Iterable[FlowLine],
                    val inflowLinesEnd: Iterable[FlowLine],
                    val associatedZonesStart: Iterable[RectangleModifiable],
                    val associatedZonesEnd: Iterable[RectangleModifiable],
                    val associatedConnectivity: Iterable[MyEdge],
                    val overridenZones: Iterable[String]) {

  val ID: String = generateUUID

  var start: Position = startA + (startB - startA)*0.5
  var end: Position = endA + (endB - endA)*0.5

  private val positionHistory: collection.mutable.ArrayBuffer[(Time, Position, Position)] = collection.mutable.ArrayBuffer()

  private val flowHistory: collection.mutable.ArrayBuffer[(Time, Int, Int)] = collection.mutable.ArrayBuffer()

  def getPositionHistory: IndexedSeq[(Time, Position, Position)] = positionHistory.toVector

  def getFlowHistory: IndexedSeq[(Time, Int, Int)] = flowHistory.toVector


  def initializePositionHistory(startTime: Time): Unit = {
    positionHistory.append((startTime, start, end))
  }

  def updateWallPosition(time: Time): Unit = {

    flowHistory.append((time, this.inflowLinesStart.map(_.getPedestrianFlow).sum, this.inflowLinesEnd.map(_.getPedestrianFlow).sum))

    val rawFrac: Double = if (flowHistory.last._2 == 0 && flowHistory.last._3 == 0)  { 0.5 }
    else {  flowHistory.last._2.toDouble / (flowHistory.last._2.toDouble + flowHistory.last._3.toDouble) }

    val frac: Double = if (rawFrac < 0.0 ) 0.0
    else if (rawFrac.isNaN || rawFrac.isInfinite) 1.0
    else if (rawFrac > 0.9) 1.0
    else rawFrac


    this.start = startA + (startB - startA) * frac
    this.end = endA + (endB - endA) * frac
    positionHistory.append((time, this.start, this.end))
    println(this.ID, frac, start, end)
  }

  def getWall: Wall = {
    new MovableWall("movable wall", this.start, this.end, SINGLELINE)
  }

}