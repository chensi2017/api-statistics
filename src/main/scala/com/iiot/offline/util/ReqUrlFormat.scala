package com.iiot.offline.util

object ReqUrlFormat {
  def formateReqUrl(reqUrl:String,method:String):String={
    //    println(reqUrl)
    val strings = reqUrl.split("/")
    var str:String = "/"+strings(1)
    for(i<- 2 until strings.length){
      if(strings(i).matches(".*\\d.*")){
        str = str + "/{id}"
      }else {
        str = str + "/" + strings(i)
      }
    }
    str = method+str
    str
  }
}
