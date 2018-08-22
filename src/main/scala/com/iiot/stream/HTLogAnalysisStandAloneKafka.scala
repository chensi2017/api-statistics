package com.iiot.stream

import com.iiot.offline.util.ProUtil
import com.iiot.stream.base.{HTBatchStatistics, HTInputDStreamFormat, HTWindowStatistics}
import com.iiot.stream.tools.{HTLogAnalysisTool, OtherTools}
import org.apache.spark.SparkConf
import org.apache.spark.custom.KafkaManager
import org.apache.spark.streaming.{Duration, StreamingContext}

//单机模式数据源kafka
object HTLogAnalysisStandAloneKafka {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME","hdfs")
    val map = OtherTools.toScalaMap(ProUtil.getInstance().getMap)
    val zkAddr = map.get("log.zookeeper.host").get
    val ssc = new StreamingContext(new SparkConf().setMaster("local[4]").setAppName("LogAnalysisStandAloneKafka"),Duration(3000))
    val km = new KafkaManager(map)
    val kafkaStream = HTLogAnalysisTool.createStream(ssc,Set(map.get("topic").get),km)
    //statistic module number and return openAPI stream
    val itemStream = HTInputDStreamFormat.kafkaInputDstreamFormat(kafkaStream,zkAddr)
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

    kafkaStream.foreachRDD(rdd=>{
      km.updateOffsets(rdd)
    })
    ssc.start()
    ssc.awaitTermination()
  }

}