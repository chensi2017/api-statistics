package com.iiot.stream

import com.iiot.stream.base.{HTBatchStatistics, HTInputDStreamFormat, HTWindowStatistics}
import com.iiot.stream.tools.{HTLogAnalysisTool, ZookeeperClient}
import org.apache.spark.custom.KafkaManager
import org.apache.spark.streaming.{Duration, StreamingContext}


object HTLogAnalysisContext {
  val zkClent = new ZookeeperClient
  var km:KafkaManager = _
  def main(args: Array[String]): Unit = {
    //acquire zk address
    if(args.length==0){
      println("ERROR:Please input zookeeper address!!!")
      return
    }
    val zkAddr = args(0)
    val ssc = new StreamingContext(HTLogAnalysisTool.initSparkConf(),Duration(3000))
    //create kafka stream
    val topic = Set("api.log.filter")
    val map = HTLogAnalysisTool.initKafkaParamters(args(0))
    km = new KafkaManager(map)
    val stream = HTLogAnalysisTool.createStream(ssc,topic)

    //transform stream
    val itemStream = HTInputDStreamFormat.inputDStreamFormat(stream)
    itemStream.cache()

    //broad zookeeper address
    val zkAddrBro = ssc.sparkContext.broadcast(zkAddr)

    //统计
    //1.各个接口累计访问次数; 2.每个接口响应总时长
    val batchStatistics = new HTBatchStatistics
    batchStatistics.interfaceStatistics(itemStream,zkAddrBro)
    //3.统计每个接口每分钟访问最大次数
    val windowStatistics = new HTWindowStatistics
    windowStatistics.windowStatistics(itemStream,zkAddrBro)

    //提交offset
    stream.foreachRDD(rdd=>{
      km.updateOffsets(rdd)
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
