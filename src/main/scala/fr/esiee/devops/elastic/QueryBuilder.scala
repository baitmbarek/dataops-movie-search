package fr.esiee.devops.elastic

import com.sksamuel.elastic4s.requests.searches.aggs.AbstractAggregation
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.queries.matches.MultiMatchQueryBuilderType
import fr.esiee.devops.domain.{Filter, SearchRequest}

object QueryBuilder {

  import com.sksamuel.elastic4s.ElasticDsl._

  private val filterKeywordAssociation = Map(
    "genres" -> "genres.name.keyword",
    "keywords" -> "keywords.name.keyword",
    "status" -> "status.keyword",
    "vote_average" -> "vote_average",
    "runtime" -> "runtime",
    "decade" -> "decade.keyword"
  )

  val aggregatesQuery: Iterable[AbstractAggregation] = Seq(
    termsAgg("genres", filterKeywordAssociation("genres")).size(20),
    termsAgg("keywords", filterKeywordAssociation("keywords")).size(25),
    termsAgg("status", filterKeywordAssociation("status")).size(10),
    rangeAgg("vote_average", filterKeywordAssociation("vote_average")).ranges((0.0,5.0), (5.0,6.0), (6.0,7.0), (7.0,8.0), (8.0,10.0)),
    rangeAgg("runtime", filterKeywordAssociation("runtime")).range("< 30 min", 0.0, 30.0).range("30 - 60 min", 30.0, 60.0).range("60 - 90 min", 60.0, 90.0).range("1h30 - 2h30", 90.0, 150.0).range("> 2h30", 150.0, 600.0),
    termsAgg("decade", filterKeywordAssociation("decade")).size(20)
  )

  private def filterToElasticFilter(filter: Filter): Query = {
    (filter.value, filter.from, filter.to) match {
      case (Some(v), _, _) => termQuery(filterKeywordAssociation(filter.key), v)
      case (_, Some(f), Some(t)) => rangeQuery(filterKeywordAssociation(filter.key)).gte(f).lt(t)
      case (_, Some(f), _) => rangeQuery(filterKeywordAssociation(filter.key)).gte(f)
      case (_, _, Some(t)) => rangeQuery(filterKeywordAssociation(filter.key)).lt(t)
    }
  }

  def buildSearchQuery(searchRequest: SearchRequest): Query = {
    val filters = searchRequest.filters.map(filterToElasticFilter) ++ searchRequest.search.split(" ").map{word =>
      multiMatchQuery(word)
        .fields("title", "cast.character", "cast.name", "crew.name", "genres.name", "keywords.name")
        .matchType(MultiMatchQueryBuilderType.PHRASE_PREFIX)
    }

    boolQuery().must(filters)
  }

  def buildSuggestQuery(searchRequest: SearchRequest): Query = {
    val filters = searchRequest.filters.map(filterToElasticFilter) :+ multiMatchQuery(searchRequest.search)
        .fields("title", "cast.name", "crew.name")
        .matchType(MultiMatchQueryBuilderType.PHRASE_PREFIX)

    boolQuery().must(filters)
  }

}
