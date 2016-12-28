package example

import akka.stream.scaladsl.Source
import broker.RandomNumberActorBroker
import config.AppConfig
import producers.Producerable

import scala.concurrent.duration.{Duration, _}
import scala.util.Random

/**
 * Generates random numbers and puts them to Kafka.
 */
class RandomNumberProducer(val config: AppConfig) extends Producerable with RandomNumberActorBroker {

  override def receive: Receive = {
    case Run =>
      Source.tick(Duration.Zero, 1.second, Unit).map(_ => sendToSink(Random.nextInt().toString))
  }
}
case object Run
