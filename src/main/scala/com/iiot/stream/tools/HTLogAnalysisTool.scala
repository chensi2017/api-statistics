package com.iiot.stream.tools

import com.iiot.stream.HTLogAnalysisContext.{km, zkClent}
import com.iiot.stream.bean.Item
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext

object HTLogAnalysisTool {
  def initKafkaParamters(ipAndPort:String,path:String="/conf_htiiot/spark_streaming"):Map[String,String]={
    val zk = zkClent.getConfingFromZk(ipAndPort, 30000)
    var configs = zkClent.getAll(zk, "/conf_htiiot/spark_streamming")
    Map("metadata.broker.list" -> configs.getProperty("kafka.broker.list"),
      "group.id" -> "spark-test11",//先写死，等后期在zk上分配节点
      "zookeeper.connect" -> configs.getProperty("zookeeper.list"),
      "auto.offset.reset" -> configs.getProperty("auto.offset.reset"),
      "queued.max.message.chunks" -> configs.getProperty("queued.max.message.chunks"),
      "fetch.message.max.bytes" -> configs.getProperty("fetch.message.max.bytes"),
      "num.consumer.fetchers" -> configs.getProperty("num.consumer.fetchers"),
      "socket.receive.buffer.bytes" -> configs.getProperty("socket.receive.buffer.bytes"))
  }
  def initSparkConf(): SparkConf ={
    new SparkConf()
      .setAppName("InterfaceLogAnalysis")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrationRequired", "true")
      .set("spark.executor.extraJavaOptions","-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCTimeStamps")
      .set("spark.streaming.stopGracefullyOnShutdown","true") //当执行kill命令优雅的关闭job
      //      .set("spark.shuffle.service.enabled","true")
      //      .set("spark.dynamicAllocation.enabled","true")//动态分配executor
      .registerKryoClasses(Array(classOf[Array[Item]],classOf[Item]))
  }

  def createStream(scc: StreamingContext, topics: Set[String]) = {
    km.createDirectStream(scc,topics)
  }

  def formateReqUrl(reqUrl:String,method:String):String={
    //    println(reqUrl)
    val strings = reqUrl.split("/")
    var str:String = "/"+strings(1)
    for(i<- 2 until strings.length){
      if(strings(i).matches(".*\\d.*")){
        str = str + "/{id}"
      }else {
        str = str + "/" + strings(i)
      }
    }
    str = method+str
    str
  }
}
