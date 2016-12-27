package config

import com.typesafe.config.Config

class AppConfig(load: Config) {

  val kafkaConfig = new KafkaConfig(load)
}

class KafkaConfig(config: Config) {

  private val appConfig = config.getConfig("kafka-config")

  def uri = appConfig.getString("uri")

  def port = appConfig.getInt("port")
}