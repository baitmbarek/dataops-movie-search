package fr.esiee.devops.elastic

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.searches.HighlightField
import com.sksamuel.elastic4s.requests.searches.aggs.AbstractAggregation
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fr.esiee.devops.settings.Environment

class ESClient(config: Environment) {

  import com.sksamuel.elastic4s.ElasticDsl._

  private val client = ElasticClient(JavaClient(ElasticProperties(config.elasticUrl)))

  def makeSearch(query: Query, aggregations: Iterable[AbstractAggregation], size: Int, highlight: Seq[String] = Seq.empty, fetchSource: Boolean = true) = {
    client.execute{
      search(config.elasticIndex).query(query).aggs(aggregations).size(size).highlighting(highlight.map(hf => HighlightField(hf))).fetchSource(fetchSource)
    }.await
  }

}
