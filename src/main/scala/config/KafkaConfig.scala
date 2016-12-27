package config

import com.typesafe.config.Config

class KafkaConfig(config: Config) {

  private val appConfig = config.getConfig("kafka-config")

  def uri = appConfig.getString("uri")

  def port = appConfig.getInt("port")
}
