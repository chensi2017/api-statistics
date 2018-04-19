package com.iiot.stream.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.iiot.stream.bean.Item
import com.iiot.stream.tools.HTLogAnalysisTool
import org.apache.spark.streaming.dstream.DStream

object HTInputDStreamFormat {
  def inputDStreamFormat(stream:DStream[(String,String)]):DStream[Item]={
    stream.mapPartitions(iter=>{
      val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
      var arr=Array[Item]()
      iter.map(s=>{
        try {
          var item = mapper.readValue(s._2.substring(s._2.indexOf("{"), s._2.length), classOf[Item])
          item.reqUrl = HTLogAnalysisTool.formateReqUrl(item.reqUrl,item.method)
          item
        }catch {
          case e:Exception=>null
        }
      })
    }).filter(item=>{
      if(item!=null){
        true
      }else{
        false
      }
    })
  }
}
