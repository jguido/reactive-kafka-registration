package producers

import akka.actor.Cancellable
import akka.kafka.ProducerSettings
import akka.stream.{ActorMaterializer, Materializer}
import broker.ActorBroker
import config.AppConfig
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

trait Producerable extends ActorBroker {
  val config: AppConfig
  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(s"${config.kafkaConfig.uri}:${config.kafkaConfig.port}")

  def running(control: Cancellable): Receive = {
    case Stop =>
      log.info("Stopping Kafka producer stream and actor")
      control.cancel()
      context.stop(self)
  }

  case object Stop
}
