package fr.esiee.devops.domain

import com.fasterxml.jackson.databind.JsonNode

import scala.jdk.CollectionConverters._

case class SearchRequest(search: String, filters: Seq[Filter])

object SearchRequest{
  def fromJson(node: JsonNode): SearchRequest = SearchRequest(
    search = node.path("search").asText(""),
    filters = node.path("filters").elements.asScala.toSeq.map{n =>
      Filter(
        key = n.path("key").textValue,
        value = Option(n.path("value").textValue()),
        from = Option(n.path("from").textValue()),
        to = Option(n.path("to").textValue())
      )
    }
  )
}