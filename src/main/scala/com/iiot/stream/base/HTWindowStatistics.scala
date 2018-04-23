package com.iiot.stream.base
import java.text.SimpleDateFormat
import java.util.Date
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.RedisOperation
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import redis.clients.jedis.{Jedis, Pipeline, Response}

import scala.collection.mutable

class HTWindowStatistics extends Serializable {
  def windowStatistics(itemStream:DStream[(String,Item)],zkAddrBro:Broadcast[String]): Unit ={

    itemStream.map(x=>(x._2.reqUrl,1)).reduceByKeyAndWindow((a:Int,b:Int)=>a+b,Seconds(60),Seconds(60)).foreachRDD(
      rdd=>{rdd.foreachPartition(iter=>{
        var jedis:Jedis = null
        var pl:Pipeline = null
        try{
          val sdf = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
          val key = s"api_log_filter_times_perminute_$sdf"
          jedis = RedisOperation.getInstance(zkAddrBro.value).getResource()
          pl = jedis.pipelined()
          val redisMap = new mutable.HashMap[String,Response[String]]
          val originMap = new mutable.HashMap[String,Int]()
          iter.foreach(record=>{
            val filed = record._1
            val value = record._2
            originMap.put(filed,value)
            redisMap.put(filed,pl.hget(key,filed))
          })
          pl.sync()
          originMap.foreach(entry=>{
            if(redisMap.get(entry._1).get.get()==null||entry._2 > redisMap.get(entry._1).get.get().toInt){
              pl.hset(key,entry._1,entry._2.toString)
            }
          })
          pl.sync
        }catch {
          case e:Exception=>e.printStackTrace()
        }finally {
          try{
            pl.close()
            jedis.close()
          }catch {
            case e:Exception=>e.printStackTrace()
          }
        }
      })}
    )
  }
}
