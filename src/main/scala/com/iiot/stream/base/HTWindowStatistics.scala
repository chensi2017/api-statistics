package com.iiot.stream.base
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.RedisOperation
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import redis.clients.jedis.Jedis

class HTWindowStatistics extends Serializable {
  def windowStatistics(itemStream:DStream[Item],zkAddrBro:Broadcast[String]): Unit ={
    itemStream.map(x=>(x.reqUrl,1)).reduceByKeyAndWindow((a:Int,b:Int)=>a+b,Seconds(60),Seconds(60)).foreachRDD(
      rdd=>{rdd.foreachPartition(iter=>{
        var jedis:Jedis = null
        try{
          jedis = RedisOperation.getInstance(zkAddrBro.value).getResource()
          //清除上一次时间窗口在redis中记录的数据
          jedis.del("api_log_filter_times_persecond")
          iter.foreach(record=>{
            jedis.hset("api_log_filter_times_persecond",record._1,record._2.toString)
          })
        }catch {
          case e:Exception=>e.printStackTrace()
        }finally {
          try{
            jedis.close()
          }catch {
            case e:Exception=>e.printStackTrace()
          }
        }
      })}
    )
  }
}
