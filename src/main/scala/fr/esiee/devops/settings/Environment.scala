package fr.esiee.devops.settings

case class Environment(httpPort: Int, elasticUrl: String, elasticIndex: String)

object Environment {

  import pureconfig._
  import pureconfig.generic.auto._
  val live: Environment = ConfigSource.default.loadOrThrow[Environment]

}
