package consumers

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Keep, Sink, Source}
import broker.ActorBroker
import config.AppConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait Consumerable extends ActorBroker {

  val config: AppConfig
  implicit val materializer = ActorMaterializer()

  def buildConsumerSettings(implicit system: ActorSystem) = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(s"${config.kafkaConfig.uri}:${config.kafkaConfig.port}")

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing logging consumer")
      val (control, future) = createSource(consumerName, topicName)(context.system)
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

  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")
      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }
  }

  protected def createSource(groupId: String, topic: String)(implicit system: ActorSystem): Source[ConsumerMessage.CommittableMessage[Array[Byte], String], Consumer.Control] = {
    val consumerSettings = buildConsumerSettings
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
  }

  protected def processMessage(msg: Message): Future[Message] = {
    log.info(s"Consumed message: ${msg.record.value()}")
    Future.successful(msg)
  }

  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}