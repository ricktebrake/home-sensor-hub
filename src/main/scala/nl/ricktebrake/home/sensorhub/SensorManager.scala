package nl.ricktebrake.home.sensorhub

import akka.Done
import akka.actor.{Actor, Props}
import akka.stream.alpakka.mqtt.MqttMessage
import akka.stream.scaladsl.Source
import nl.ricktebrake.home.sensorhub.SensorType.SensorType

import scala.concurrent.Future

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
    case RegisterSensor(deviceId, sensorType) =>
    //registeredSensors += deviceId -> sensorType
  }
}
