package nl.ricktebrake.home.sensorhub

import akka.Done
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.stream.scaladsl._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent._

object HomeSensorHub extends App {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  val connectionSettings = MqttConnectionSettings(
    "tcp://test.mosquitto.org:1883", // (1)
    "test-scala-client", // (2)
    new MemoryPersistence // (3)
  )

  val topic = "/#"

  val mqttSource: Source[MqttMessage, Future[Done]] =
    MqttSource.atMostOnce(
      connectionSettings,
      MqttSubscriptions(Map(topic -> MqttQoS.AtLeastOnce)),
      bufferSize = 8
    )

  mqttSource runForeach(message => println(message.topic + " - " + message.payload.utf8String))






}
