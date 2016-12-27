package broker

trait RandomNumberActorBroker extends ActorBroker{
  override protected def topicName: String = "RandomNumbers"

  override protected def consumerName: String = "LoggingConsumer"
}
