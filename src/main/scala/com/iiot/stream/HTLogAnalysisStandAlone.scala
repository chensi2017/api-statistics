package com.iiot.stream

import com.iiot.stream.base.{HTBatchStatistics, HTInputDStreamFormat, HTWindowStatistics, SocketReceiver}
import com.iiot.stream.tools.ZookeeperClient
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Duration, StreamingContext}

object HTLogAnalysisStandAlone {
  val zkClent = new ZookeeperClient
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME","hdfs")

    val Array(zkAddr,port) = args

    val ssc = new StreamingContext(new SparkConf().setMaster("local[4]").setAppName("LogAnalysisStandAlone"),Duration(3000))

//    val sockStream = ssc.socketTextStream(host,port.toInt).map(str=>("",str))
    val sockStream = ssc.receiverStream(new SocketReceiver(port.toInt)).map(str=>("",str))

//    sockStream.print()

    //transform stream
    val itemStream = HTInputDStreamFormat.inputDStreamFormat(sockStream)

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

    ssc.start()
    ssc.awaitTermination()
  }

}
