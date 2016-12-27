package example

import akka.actor.Cancellable
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Keep, Source}
import broker.RandomNumberActorBroker
import config.AppConfig
import org.apache.kafka.clients.producer.ProducerRecord
import producers.Producerable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.util.Random

/**
 * Generates random numbers and puts them to Kafka.
 */
class RandomNumberWriter(val config: AppConfig) extends Producerable with RandomNumberActorBroker {

  override def receive: Receive = {
    case Run =>
      val tickSource: Source[String, Cancellable] = Source.tick(Duration.Zero, 1.second, Unit).map(_ => Random.nextInt().toString)

      log.info("Initializing writer")
      val kafkaSink = Producer.plainSink(producerSettings)

      val (control, future) = tickSource
        .map(new ProducerRecord[Array[Byte], String](topicName, _))
        .toMat(kafkaSink)(Keep.both)
        .run()
      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }
      context.become(running(control))
      log.info(s"Writer now running, writing random numbers to topic ${topicName}")
  }
}
case object Run
