package broker

import akka.actor.{Actor, ActorLogging}

trait ActorBroker extends Actor with ActorLogging {
  protected def consumerName: String
  protected def topicName: String
}
