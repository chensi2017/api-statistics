package com.iiot.stream.tools

import com.iiot.stream.bean.{Item, Log}

object OtherTools {

  def checkEmpty(modular: String): Boolean = {
    "".equals(modular)||"null".equals(modular)||modular==null
  }

  def toScalaMap(map:java.util.Map[String,String]) ={
    var mapS:Map[String,String] = Map()
    val iter = map.keySet().iterator()
    while (iter.hasNext){
      val key = iter.next()
      mapS += (key -> map.get(key))
    }
    mapS
  }

  def transLogToItem(log:Log): Item ={
    new Item(null,log.createTime.toString,log.requestMethod,log.url,log.createTime,-1,log.ip,-1,log.responseTimeLength)
  }

}
