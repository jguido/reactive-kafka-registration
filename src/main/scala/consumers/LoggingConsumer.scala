package consumers

import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class LoggingConsumer(implicit mat: Materializer) extends Consumerable {
  import LoggingConsumer._

  override def receive: Receive = {
    case Start =>
      log.info("Initializing logging consumer")
      val (control, future) = createSource("loggingConsumer", RandomNumbers.Topic)(context.system)
        .mapAsync(10)(processMessage)
        .map(_.committableOffset)
        .groupedWithin(10, 15 seconds)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => batch.updated(elem) })
        .mapAsync(1)(_.commitScaladsl())
        .toMat(Sink.ignore)(Keep.both)
        .run()

      context.become(running(control))

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }

      log.info("Logging consumer started")
  }
}
