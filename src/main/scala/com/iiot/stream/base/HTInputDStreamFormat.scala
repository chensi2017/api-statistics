package com.iiot.stream.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.HTLogAnalysisTool
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable.ArrayBuffer

object HTInputDStreamFormat {
  def inputDStreamFormat(stream:DStream[(String,String)]):DStream[(String,Item)]={
    stream.mapPartitions(iter=>{
      val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
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
