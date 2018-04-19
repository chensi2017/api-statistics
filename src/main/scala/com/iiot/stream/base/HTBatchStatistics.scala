package com.iiot.stream.base
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.RedisOperation
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.DStream
import redis.clients.jedis.Jedis

import scala.collection.mutable

class HTBatchStatistics extends Serializable {
  def interfaceStatistics(itemStream:DStream[Item],zkAddrBro:Broadcast[String]): Unit ={
    itemStream.foreachRDD(rdd=>rdd.foreachPartition(it=>{
      val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
      //HashMap[reqUrl,Array[访问次数,访问时长]]
      var resultMap=new mutable.HashMap[String,Array[Long]]()
      it.foreach(item=>{
        if(item!=null) {
          val arr = resultMap.getOrElse(item.reqUrl, Array[Long](0, 0))
          arr(0) = arr(0) + 1
          arr(1) = arr(1) + item.useTime
          resultMap.put(item.reqUrl, arr)
        }
      })
      var jedis:Jedis = null
      try{
        jedis = RedisOperation.getInstance(zkAddrBro.value).getResource()
        resultMap.foreach(a=>{
          //        println(s"api:${a._1};;;times:${a._2(0)};;;usetime:${a._2(1)}")
          //update times and usetime
          jedis.hincrBy("api_log_filter_times",a._1,a._2(0))
          jedis.hincrBy("api_log_filter_usetime",a._1,a._2(1))

        })}catch {
        case e:Exception=>e.printStackTrace()
      }finally {
        try{
          jedis.close()
        }catch {
          case e:Exception=>e.printStackTrace()
        }
      }
    }))
  }
}
