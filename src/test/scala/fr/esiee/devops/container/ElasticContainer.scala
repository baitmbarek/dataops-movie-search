package fr.esiee.devops.container

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Index}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.indexes.IndexRequest
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.utility.DockerImageName

import scala.io.Source

object ElasticContainer {

  //TODO: Create an Elasticsearch container and expose the port 9200
  // You can refer to https://www.testcontainers.org/modules/elasticsearch/
  val container: ElasticsearchContainer = ???

  /**
   * This method starts the container and initializes an index with some documents
   * @return
   */
  def init = {
    container.start()
    val data: List[String] = {
      val source = Source.fromFile(getClass.getResource("/data/movies-sample.json").getPath)
      val r = source.getLines.toList
      source.close
      r
    }

    import com.sksamuel.elastic4s.ElasticDsl._
    val client = ElasticClient(JavaClient(ElasticProperties(s"http://${container.getHttpHostAddress}")))
    client.execute{
      bulk(
        data.map{record =>
          IndexRequest(Index("myindex"), source = Some(record))
        }
      ).refreshImmediately
    }.await
  }


}
