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
    "tcp://localhost:1883", // (1)
    "test-scala-client", // (2)
    new MemoryPersistence // (3)
  )

  val topic = "test/device"

  val mqttSource: Source[MqttMessage, Future[Done]] =
    MqttSource.atMostOnce(
      connectionSettings.withClientId(clientId = "test-control"),
      MqttSubscriptions(Map(topic -> MqttQoS.AtLeastOnce)),
      bufferSize = 8
    )

mqttSource.map(m => m.payload.utf8String).


  val sink: Sink[MqttMessage, Future[Done]] =
    MqttSink(connectionSettings, MqttQoS.AtLeastOnce)
  private val value: Future[Done] = mqttSource.runWith(sink)

  value.onComplete(_ => system.terminate())


}
