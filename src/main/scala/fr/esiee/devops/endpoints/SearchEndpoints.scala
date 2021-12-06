package fr.esiee.devops.endpoints

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.sksamuel.elastic4s.requests.searches.queries.matches.MatchAllQuery
import fr.esiee.devops.domain.SearchRequest
import fr.esiee.devops.elastic.{ESClient, QueryBuilder}
import fr.esiee.devops.settings.Environment

class SearchEndpoints(environment: Environment) {

  private val esClient = new ESClient(environment)
  private val (reader, writer) = {
    val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
    (mapper.reader, mapper.writer)
  }

  private val staticPaths = pathSingleSlash {
    getFromResource("static/index.html")
  } ~ pathPrefix("static" / Segments) { uri =>
    getFromResource(s"static/${uri.mkString("/")}")
  }

  private val getAggregatesPath = path("aggs"){
    val query = MatchAllQuery()
    val aggs = QueryBuilder.aggregatesQuery
    val result = esClient.makeSearch(query, aggs, 0)
    complete(StatusCode.int2StatusCode(200), writer.writeValueAsString(result.result.aggregations.data))
  }

  private val searchPath = path("search"){
    post{
      entity(as[String]){bodyStr =>
        val searchRequest = {
          val reqBody = reader.readTree(bodyStr)
          SearchRequest.fromJson(reqBody)
        }
        val query = QueryBuilder.buildSearchQuery(searchRequest)
        val result = esClient.makeSearch(query, Iterable.empty, 10, Seq("title", "cast.character", "cast.name", "crew.name"))

        val resp = s"""{ "total": {"value": ${result.result.totalHits}}, "hits": ${writer.writeValueAsString(result.result.hits.hits.map(_.sourceAsMap))} }"""
        complete(StatusCode.int2StatusCode(200), resp)
      }
    }
  }

  private val suggestPath = path("suggest") {
    post {
      entity(as[String]) { bodyStr =>
        val searchRequest = {
          val reqBody = reader.readTree(bodyStr)
          SearchRequest.fromJson(reqBody)
        }
        val query = QueryBuilder.buildSuggestQuery(searchRequest)
        val result = esClient.makeSearch(query, Iterable.empty, 8, Seq("title", "cast.character", "cast.name", "crew.name"), false)

        val highlights = result.result.hits.hits.map(_.highlight)
        complete(StatusCode.int2StatusCode(200), writer.writeValueAsString(highlights))
      }
    }
  }

  val routes = staticPaths ~ getAggregatesPath ~ suggestPath ~ searchPath


}
