package hubmodel.DES

import hubmodel.Time
import hubmodel.ped.{PedestrianNOMAD, PedestrianSim}

class NOMADGraphSimulator(start: Time, end: Time) extends PedestrianDES[PedestrianNOMAD](start, end) {
  override def run(): Unit = println("test")

}