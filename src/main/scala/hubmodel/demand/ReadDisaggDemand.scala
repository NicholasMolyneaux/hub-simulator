package hubmodel.demand

import hubmodel.NewTime
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.io.BufferedSource

class ReadDisaggDemand(fileName: String) {

  private val _pedestrians: Vector[PedestrianJSON] = {

    val source: BufferedSource = scala.io.Source.fromFile(fileName)
    val input: JsValue = Json.parse(try source.mkString finally source.close)

    input.validate[Vector[PedestrianJSON]] match {
      case s: JsSuccess[Vector[PedestrianJSON]] => s.get
      case e: JsError => throw new Error("Error while parsing disaggregate pedestrian: " + JsError.toJson(e).toString())
    }
  }

  val pedestrians: Iterable[(String, String, NewTime)] = this._pedestrians.map(p => (p.oZone, p.dZone, NewTime(p.entryTime)))

}