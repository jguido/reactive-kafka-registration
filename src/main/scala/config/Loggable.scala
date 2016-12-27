package config

import org.slf4j.LoggerFactory

trait Loggable {

  val logger = LoggerFactory.getLogger(getClass)

  def logInfo(msg: String): Unit = {
    logger.info(msg)
  }

  def logError(msg: String): Unit = {
    logger.error(msg)
  }
}
