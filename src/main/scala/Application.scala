import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import config.Loggable
import consumers.LoggingConsumer
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import producers.RandomNumberWriter
import reactivekafka.RandomNumberWriter.Run

object Application extends App with Loggable {
  implicit val system: ActorSystem = ActorSystem("reactive-kafka")
  logger.info("Starting embedded Kafka")
  implicit val embeddedKafkaConfig = EmbeddedKafkaConfig(9095, 2185)
  EmbeddedKafka.start()
  logger.info("Embedded Kafka ready")
  implicit val materializer = ActorMaterializer.create(system)
  val writer = system.actorOf(Props(new RandomNumberWriter))
  Thread.sleep(2000)
  // for production systems topic auto creation should be disabled
  val loggingConsumer = system.actorOf(Props(new LoggingConsumer))
//  writer ! Run
}
