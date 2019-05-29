package hubmodel.supply.graph

import hubmodel.mgmt.ControlDevices
import hubmodel.mgmt.flowgate.{BinaryGate, FlowGate}
import hubmodel.mgmt.flowsep.FlowSeparator
import hubmodel.ped.PedestrianNOMAD
import hubmodel.tools.cells.Rectangle

abstract class GraphContainer(protected val flowGates: Iterable[FlowGate],
                              protected val binaryGates: Iterable[BinaryGate],
                              protected val movingWalkways: Iterable[MovingWalkway],
                              protected val flowSeparators: Iterable[FlowSeparator[_, _]]) {

  def vertexMapNew: Map[String, Rectangle]

  def edges: Set[MyEdge]

  def processIntermediateArrival(ped: PedestrianNOMAD): Unit

  type T <: GraphContainer

  /** Creates a deep copy of the graph to use elsewhere.
    *
    * TODO: This needs to be improved when the edge weights are updated.
    *
    * @param devices new set of devices to use.
    * @return deep copy of graph
    */
  def deepCopy(devices: ControlDevices): T

  /** Creates a copy of the graph with the fraction of people using this alternative.
    *
    * @param devices            new set of devices to use
    * @param populationFraction fraction to use the alternate graph
    * @return
    */
  def deepCopy2AlternateGraphs(devices: ControlDevices, populationFraction: Double): T

}
