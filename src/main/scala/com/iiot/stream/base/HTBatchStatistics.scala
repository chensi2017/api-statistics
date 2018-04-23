package com.iiot.stream.base
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.RedisOperation
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.DStream
import redis.clients.jedis.{Jedis, Pipeline}

import scala.collection.mutable

class HTBatchStatistics extends Serializable {
  def interfaceStatistics(itemStream:DStream[(String,Item)],zkAddrBro:Broadcast[String]): Unit ={
    itemStream.foreachRDD(rdd=>rdd.foreachPartition(iter=>{
      //HashMap[reqUrl,Array[访问次数,访问时长]]
      var resultMap=new mutable.HashMap[String,Array[Long]]()
      iter.foreach(it=>{
        if(it!=null) {
          val key = it._1+"|"+it._2.reqUrl
          val arr = resultMap.getOrElse(key, Array[Long](0, 0))
          arr(0) = arr(0) + 1             //访问次数
          arr(1) = arr(1) + it._2.useTime  //访问时间
          resultMap.put(key, arr)
        }
      })
      var jedis:Jedis = null
      var pl:Pipeline = null
      try{
        jedis = RedisOperation.getInstance(zkAddrBro.value).getResource()
        pl = jedis.pipelined()
        resultMap.foreach(a=>{
          //        println(s"api:${a._1};;;times:${a._2(0)};;;usetime:${a._2(1)}")
          //update times and usetime
          val key = a._1.split("\\|")
          val date = key(0)
          val reqUrl = key(1)
          pl.hincrBy(s"api_log_filter_times_$date",reqUrl,a._2(0))
          pl.hincrBy(s"api_log_filter_usetime_$date",reqUrl,a._2(1))
        })
        pl.sync()
      }catch {
        case e:Exception=>e.printStackTrace()
      }finally {
        try{
          pl.close()
          jedis.close()
        }catch {
          case e:Exception=>
        }
      }
    }))
  }
}
