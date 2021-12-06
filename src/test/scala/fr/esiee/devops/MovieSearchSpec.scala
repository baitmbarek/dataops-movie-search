package fr.esiee.devops

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fr.esiee.devops.container.ElasticContainer
import fr.esiee.devops.domain.{Filter, SearchRequest}
import fr.esiee.devops.endpoints.SearchEndpoints
import fr.esiee.devops.settings.Environment

import scala.jdk.CollectionConverters._

class MovieSearchSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val jsonMapper = new ObjectMapper().registerModule(DefaultScalaModule)
  ElasticContainer.init

  val environment = Environment.live.copy(
    elasticUrl = s"http://localhost:${ElasticContainer.container.getMappedPort(9200)}"
  )
  val routes = new SearchEndpoints(environment).routes

  "MovieSearch service" should {

    "return aggregates" in {
      Get("/aggs") ~> routes ~> check {
        val jsonResponse = jsonMapper.reader.readTree(responseAs[String])

        jsonResponse.path("genres").path("buckets").elements.asScala.size should be > 1
        jsonResponse.path("genres").path("buckets").elements.asScala.flatMap(agg => agg.fieldNames().asScala).toSet shouldBe Set("key", "doc_count")
        jsonResponse.path("vote_average").path("buckets").elements.asScala.size should be > 1
        jsonResponse.path("vote_average").path("buckets").elements.asScala.flatMap(agg => agg.fieldNames().asScala).toSet shouldBe Set("key", "doc_count", "from", "to")

      }
    }

    "offer relevant suggestions" in {
      val request = jsonMapper.writer.writeValueAsString(SearchRequest(search = "Ava", filters = Seq()))
      Post("/suggest", request) ~> routes ~> check {
        val jsonResponse = jsonMapper.reader.readTree(responseAs[String])

        jsonResponse.isArray shouldBe true
        jsonResponse.isEmpty shouldBe false
        val suggestions = jsonResponse.elements.asScala.flatMap(n => n.fields.asScala.flatMap{e => e.getValue.elements.asScala})
        suggestions.forall(_.isTextual) shouldBe true
        suggestions.forall(_.asText("") contains "Ava") shouldBe true

      }
    }

    "retrieve matching movies" in {
      val request = jsonMapper.writer.writeValueAsString(SearchRequest(search = "Avatar", filters = Seq(Filter("genres", Some("Fantasy"), None, None))))
      Post("/search", request) ~> routes ~> check {
        val jsonResponse = jsonMapper.reader.readTree(responseAs[String])

        jsonResponse.path("total").path("value").asInt(0) should be > 0
        jsonResponse.path("hits").elements.asScala.exists(_.path("title").asText("") == "Avatar") shouldBe true
        jsonResponse.path("hits").elements.asScala.forall(_.path("genres").elements.asScala.exists(_.path("name").asText("") == "Fantasy")) shouldBe true

      }
    }

  }

}
