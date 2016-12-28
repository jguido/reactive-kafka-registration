package producers

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.{Done, NotUsed}
import broker.ActorBroker
import config.AppConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Future

trait Producerable extends ActorBroker {
  val config: AppConfig
  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(s"${config.kafkaConfig.uri}:${config.kafkaConfig.port}")

  def running(): Receive = {
    case Stop =>
      log.info("Stopping Kafka producer stream and actor")
      context.stop(self)
  }

  def sendToSink(message: String): Unit = {
    log.info(s"Attempting to produce message on topic $topicName")
    val kafkaSink = Producer.plainSink(producerSettings)

    val stringToProducerRecord: ProducerRecord[Array[Byte], String] = new ProducerRecord[Array[Byte], String](topicName, message)
    val (a, future): (NotUsed, Future[Done]) = Source.fromFuture(Future(stringToProducerRecord))
      .toMat(kafkaSink)(Keep.both)
      .run()
    future.onFailure {
      case ex =>
        log.error("Stream failed due to error, restarting", ex)
        throw ex
    }
    context.become(running())
    log.info(s"Writer now running, writing random numbers to topic $topicName")
  }


  case object Stop
}
