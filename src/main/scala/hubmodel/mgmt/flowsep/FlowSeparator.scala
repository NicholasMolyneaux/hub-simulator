package hubmodel.mgmt.flowsep

import hubmodel.DES.{Action, NOMADGraphSimulator}
import hubmodel.mgmt._
import hubmodel.ped.PedestrianNOMAD
import hubmodel.supply.continuous.{MovableWall, SINGLELINE, Wall}
import hubmodel.supply.graph.MyEdge
import hubmodel.tools.Time
import hubmodel.tools.cells.RectangleModifiable
import hubmodel.tools.exceptions.IllegalFlowSeparatorPosition
import hubmodel.{FLOW_SEPARATOR_SPEED, FLOW_SEPARATOR_UPDATE, Position, generateUUID}

import scala.util.{Failure, Success, Try}

/**
  * Dynamic separators of the pedestrian flows. These devices measures some KPI (usually the flow) and then allocates
  * part of the corridor to each direction.
  *
  * Both extreme positions must corrispond to each other. Using flow separators will modify the graph hence this
  * information is stored here and will be processed by the graph builder. The list of zones to be deleted is also
  * specified. The connectivity of these deleted zones will also be removed.
  *
  * @param startA                 extreme position of start
  * @param startB                 other extreme position of start
  * @param endA                   extreme position of end
  * @param endB                   other extrem position of end
  * @param inflowLinesStart       lines across which pedestrian flow is mesured for the start
  * @param inflowLinesEnd         lines across which pedestrian flow is mesured for the end
  * @param associatedZonesStart   zones to add in the graph for this separator
  * @param associatedZonesEnd     zones to add in the graph for this separator
  * @param associatedConnectivity connectivity of the new zones
  * @param overridenZones         zones to delete
  * @param function               function linking the KPI to the position of the separator.
  * @tparam T measurement type
  * @tparam U output type
  */
class FlowSeparator[T <: Measurement, U <: SeparatorPositionFraction](val startA: Position,
                                                                      val startB: Position,
                                                                      val endA: Position,
                                                                      val endB: Position,
                                                                      val inflowLinesStart: Iterable[FlowLine],
                                                                      val inflowLinesEnd: Iterable[FlowLine],
                                                                      val associatedZonesStart: Iterable[RectangleModifiable],
                                                                      val associatedZonesEnd: Iterable[RectangleModifiable],
                                                                      val associatedConnectivity: Iterable[MyEdge],
                                                                      val overridenZones: Iterable[String],
                                                                      val function: FunctionalForm[T, U]) extends ControlDeviceComponent {


  /**
    * Constructor for building the [[FlowSeparator]] from the collection of parameters stored
    * in [[FlowSeparatorParameters]].
    *
    * @param params new [[FlowSeparator]] from the parameters
    */
  /*def this(params: FlowSeparatorParameters[T, U]) {
    this(
      params.startA,
      params.startB,
      params.endA,
      params.endB,
      params.inflowLinesStart.map(_.toFlowLine),
      params.inflowLinesEnd.map(_.toFlowLine),
      params.associatedZonesStart,
      params.associatedZonesEnd,
      params.associatedConnectivity,
      params.overriddenZones,
      params.function
    )
  }*/

  /** Unique identifier
    *
    */
  val ID: String = generateUUID

  // Default start position of the separator. The average of the extreme positions of each ends.
  var start: Position = startA + (startB - startA) * 0.5
  var end: Position = endA + (endB - endA) * 0.5

  /** The position isn't instantaniously moved, hence this is the desired position.
    *
    */
  private var currentTargetPosition: (Position, Position) = (start, end)

  // History of the positions of the flow separator.
  private val positionHistory: collection.mutable.ArrayBuffer[(Time, Position, Position)] = collection.mutable.ArrayBuffer()

  // History of the measured flows.
  private val flowHistoryNew: collection.mutable.ArrayBuffer[(Time, BidirectionalFlow)] = collection.mutable.ArrayBuffer()

  // History of the computed fraction between the each incoming flows.
  private val fractionHistory: collection.mutable.ArrayBuffer[(Time, Double)] = collection.mutable.ArrayBuffer((Time(0.0), 0.5))

  // Indicator if the separator is planned to move. This means the queue of events doesn't need to be searched.
  var movingWallEventIsInserted: Boolean = false

  /**
    * Indicates if the wall should move to it's new position or not. This
    *
    * @param updateStep
    * @return
    */
  def flowSeparatorNeedsToMove(updateStep: Time): Boolean = {
    (this.start - this.currentTargetPosition._1).norm > FLOW_SEPARATOR_SPEED * updateStep.value && (this.end - this.currentTargetPosition._2).norm > FLOW_SEPARATOR_SPEED * updateStep.value
  }

  def getPositionHistory: IndexedSeq[(Time, Position, Position)] = positionHistory.toVector

  def getFractionHistory: IndexedSeq[(Time, Double)] = fractionHistory.toVector

  def initializePositionHistory(startTime: Time): Unit = {
    positionHistory.append((startTime, start, end))
  }

  def updateWallTargetPosition(time: Time): Unit = {

    flowHistoryNew.append((time, BidirectionalFlow(this.inflowLinesStart.map(_.getPedestrianFlow).sum, this.inflowLinesEnd.map(_.getPedestrianFlow).sum)))

    function match {
      case default: FunctionalFormFlowSeparator => {
        val frac: SeparatorPositionFraction = Try(default.f(flowHistoryNew.last._2)) match {
          case Success(s) => s
          case Failure(f) => f match {
            case outsideBounds: IllegalFlowSeparatorPosition => {
              if (outsideBounds.position > 1.0) {
                SeparatorPositionFraction(1.0)
              }
              else if (outsideBounds.position < 0.0) {
                SeparatorPositionFraction(0.0)
              }
              else {
                throw new Exception("This case should never happen")
              }
            }
            case illegalArg: IllegalArgumentException => {
              println("Warning ! Error when computing flow separator fraction, defaulting to 0.5")
              SeparatorPositionFraction(0.5)
            }
            case _ => throw f
          }
        }


        if (math.abs(frac.r - this.fractionHistory.last._2) / this.fractionHistory.last._2 > FLOW_SEPARATOR_UPDATE) {
          this.currentTargetPosition = (this.startA + (this.startB - this.startA) * frac.r, this.endA + (this.endB - this.endA) * frac.r)
          this.associatedZonesStart.foreach(_.setTargetPosition(frac.r))
          this.associatedZonesEnd.foreach(_.setTargetPosition(frac.r))
        }
        this.fractionHistory.append((time, frac.r))

      }
      case _ => throw new NotImplementedError("This does not make sense for flow separators")
    }
  }

  def getWall: Wall = {
    new MovableWall("movable wall", this.start, this.end, SINGLELINE)
  }

  class MoveFlowSeperator[V <: PedestrianNOMAD](sim: NOMADGraphSimulator[V]) extends Action {


    override def execute(): Unit = {
      if ((currentTargetPosition._1 - start).norm > 0.0) {
        start = start + (currentTargetPosition._1 - start).normalized * (FLOW_SEPARATOR_SPEED * sim.sf_dt.value.toDouble)
        associatedZonesStart.foreach(_.moveRectangle(sim.sf_dt))
      }

      if ((currentTargetPosition._2 - end).norm > 0.0) {
        end = end + (currentTargetPosition._2 - end).normalized * (FLOW_SEPARATOR_SPEED * sim.sf_dt.value.toDouble)
        associatedZonesEnd.foreach(_.moveRectangle(sim.sf_dt))
      }

      positionHistory.append((sim.currentTime, start, end))

      if (flowSeparatorNeedsToMove(sim.sf_dt)) {
        sim.insertEventWithDelay(sim.sf_dt)(new MoveFlowSeperator(sim))
        movingWallEventIsInserted = true
      } else {
        //start = currentTargetPosition._1
        //end = currentTargetPosition._2
        movingWallEventIsInserted = false
      }
    }
  }

  /**
    * Deeply copies this [[FlowSeparator]] to make a new one.
    *
    * @return deep copy of the current component
    */
  override def deepCopy: FlowSeparator[T, U] = new FlowSeparator[T, U](
    this.startA,
    this.startB,
    this.endA,
    this.endB,
    this.inflowLinesStart.map(_.deepCopy),
    this.inflowLinesEnd.map(_.deepCopy),
    this.associatedZonesStart.map(_.deepCopy),
    this.associatedZonesEnd.map(_.deepCopy),
    this.associatedConnectivity.map(_.deepCopy),
    this.overridenZones,
    this.function
  )

  /**
    *
    * @param newFunction new function to pass to the flow separator
    * @tparam V any measurment is possible
    * @tparam W an output compatible with the flow separator is required
    * @return deep copy of the current component with a different function form
    */
  def deepCopy[V <: Measurement, W <: SeparatorPositionFraction](newFunction: FunctionalForm[V, W]): FlowSeparator[V, W] = new FlowSeparator[V, W](
    this.startA,
    this.startB,
    this.endA,
    this.endB,
    this.inflowLinesStart.map(_.deepCopy),
    this.inflowLinesEnd.map(_.deepCopy),
    this.associatedZonesStart.map(_.deepCopy),
    this.associatedZonesEnd.map(_.deepCopy),
    this.associatedConnectivity.map(_.deepCopy),
    this.overridenZones,
    newFunction
  )
}
