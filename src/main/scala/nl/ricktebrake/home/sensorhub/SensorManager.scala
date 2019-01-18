package nl.ricktebrake.home.sensorhub

import akka.actor.{Actor, Props}
import nl.ricktebrake.home.sensorhub.SensorType.SensorType

object SensorType extends Enumeration {
  type SensorType = Value
  val MOTION, TEMPERATURE, HUMIDITY, SOIL = Value
}

case class RegisterSensor(sensorId: String, sensorType: SensorType)

object SensorManager {
  def props(): Props = Props(new SensorManager())
}

class SensorManager() extends Actor {

  //val registeredSensors : Map[String, SensorType]


  override def receive: Receive = {
    case payload =>
      println(payload)
  }
}
