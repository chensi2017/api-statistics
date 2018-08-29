package com.iiot.stream.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.{HTLogAnalysisTool, OtherTools, RedisOperation, TimeTools}
import org.apache.spark.streaming.dstream.DStream
import com.iiot.stream.bean.Log
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object HTInputDStreamFormat {
  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def kafkaInputDstreamFormat(stream:DStream[(String,String)],zkAddr:String): DStream[(String,Item)] ={
    stream.mapPartitions(it=>{
      val arr:ArrayBuffer[(String,Item)]=ArrayBuffer()
      val moduleMap = new mutable.HashMap[String,Int]()
      val time = TimeTools.getToday()
      it.foreach(r=>{
        val log = mapper.readValue(r._2,classOf[Log])
        if(!OtherTools.checkEmpty(log.modular)) {
          moduleMap.put(log.modular, moduleMap.getOrElse(log.modular, 0) + 1)
        }
        if("openAPI".equals(log.logSource)){
          val item = OtherTools.transLogToItem(log)
          arr.+=((TimeTools.formatTime(item.requestTime),item))
        }
      })
      val jedis = RedisOperation.getInstance(zkAddr).getResource()
      val pl = jedis.pipelined()
      moduleMap.foreach(m=>{
        pl.hincrBy("ht:log:module:"+time,m._1,m._2)
      })
      pl.sync()
      pl.close()
      jedis.close()
      arr.iterator
    })
  }

  //该方法为socket版日志格式处理
  @deprecated
  def inputDStreamFormat(stream:DStream[(String,String)]):DStream[(String,Item)]={
    stream.mapPartitions(iter=>{
      val arr:ArrayBuffer[(String,Item)]=ArrayBuffer()
      iter.map(s=>{
        try {
          val date = s._2.substring(0,10);
          var item = mapper.readValue(s._2.substring(s._2.indexOf("{"), s._2.length), classOf[Item])
          item.reqUrl = HTLogAnalysisTool.formateReqUrl(item.reqUrl,item.method)
          arr.+=((date,item))
        }catch {
          case e:Exception=>null
        }
      })
      arr.iterator
    })
  }
}
