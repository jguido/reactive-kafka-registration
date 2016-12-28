package example

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfig

object Application extends App  {
  implicit val system: ActorSystem = ActorSystem("reactive-kafka")
  implicit val materializer = ActorMaterializer.create(system)

  private val load: Config = ConfigFactory.load("application").getConfig("app")
  val appConfig = new AppConfig(load)

  val writer = system.actorOf(Props(new RandomNumberProducer(appConfig)))
  Thread.sleep(2000)

  val loggingConsumer = system.actorOf(Props(new LoggingConsumer(appConfig)))
  writer ! Run
}
