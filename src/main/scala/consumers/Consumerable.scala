package consumers

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.scaladsl.Source
import config.AppConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.Future

trait Consumerable extends Actor with ActorLogging with AppConfig {
  import consumers.LoggingConsumer._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }


  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")
      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }
  }

  protected def createSource(groupId: String, topic: String)(implicit system: ActorSystem): Source[ConsumerMessage.CommittableMessage[Array[Byte], String], Consumer.Control] = {
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(s"${kafkaConfig.uri}:${kafkaConfig.port}")
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
  }

  protected def processMessage(msg: Message): Future[Message] = {
    log.info(s"Consumed message: ${msg.record.value()}")
    Future.successful(msg)
  }
}

object LoggingConsumer {
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}