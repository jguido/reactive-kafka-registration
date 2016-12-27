package example

import broker.RandomNumberActorBroker
import config.AppConfig
import consumers.Consumerable


class LoggingConsumer(val config: AppConfig) extends Consumerable with RandomNumberActorBroker{
}
