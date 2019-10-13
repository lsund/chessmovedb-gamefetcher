package com.github.lsund.gamefetcher

import java.util.Properties
import scalaj.http._
import scala.sys.process._
import java.nio.file.{Paths, Files}, java.nio.charset.StandardCharsets
import com.github.lsund.pgnparser._
import better.files._, better.files.File._
import org.apache.kafka.clients.producer._

class Message(key: String, value: String) {
  // TODO there must be a better way to do a record
  def key(): String = {
    return key
  }
  def value(): String = {
    return value
  }
}

object Main extends App {

  def makeKafkaProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    return new KafkaProducer[String, String](props)
  }

  def produceMessage(
      producer: KafkaProducer[String, String],
      topic: String,
      message: Message
  ): Unit = {
    val record =
      new ProducerRecord[String, String](topic, message.key(), message.value())
    try {
      producer.send(record)
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    producer.close()
  }

  val gameid = "q6NY3GkB"
  // val apiToken: String = "pass lichess/api-token".!!.trim
  // val authorizationHeaderValue: String = "Bearer " + apiToken
  // val request: HttpRequest = Http("https://lichess.org/game/export/" + gameid)
  //   .header("Authorization", authorizationHeaderValue)
  // val response: HttpResponse[String] = request.asString
  val dataDir = "data".toFile.createIfNotExists(true)
  val pgnFile = "data/" + gameid + ".pgn"
  val outfile = "data/" + gameid + ".json"
  // Files.write(
  //   Paths.get(pgnFile),
  //   response.body.getBytes(StandardCharsets.UTF_8)
  // )
  val json = ParseRunner.generateJson(pgnFile)
  val producer = makeKafkaProducer()
  produceMessage(producer, "game", new Message(gameid, json))
}
