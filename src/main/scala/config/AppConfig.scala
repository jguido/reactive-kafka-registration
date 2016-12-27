package config

import com.typesafe.config.{Config, ConfigFactory}

trait AppConfig {
  private val load: Config = ConfigFactory.load()
  val kafkaConfig = new KafkaConfig(load)
}
