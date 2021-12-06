package fr.esiee.devops

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import fr.esiee.devops.endpoints.SearchEndpoints
import fr.esiee.devops.settings.Environment

import scala.concurrent.ExecutionContext

object Server extends App {
  val host = "0.0.0.0"

  val environment = Environment.live

  implicit val system: ActorSystem = ActorSystem("helloworld")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  println(s"Starting server on port ${environment.httpPort}")

  Http().newServerAt(host, environment.httpPort).bindFlow(new SearchEndpoints(environment).routes)

}
