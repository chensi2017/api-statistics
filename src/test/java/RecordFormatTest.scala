object RecordFormatTest {
  def main(args: Array[String]): Unit = {
    val s = "/1.1/person/123ww%E5%B0%B1%E5%95%A5%E9%83%"
    println(formateReqUrl(s))
  }
  def formateReqUrl(reqUrl:String):String={
    val strings = reqUrl.split("/")
//    println(strings.length)
//    println(2 until strings.length)
    var str:String = "/"+strings(1)
    /*strings.foreach(s=>{
      if(s.matches(".*\\d.*") && !s.equals("1.1")){
        str = str + "/{id}"
      }else if(s!=""){
        str = str + "/" + s
      }
    })*/
    for(i<- 2 until strings.length){
      println(i)
      if(strings(i).matches(".*\\d.*")){
        str = str + "/{id}"
      }else{
        str = str + "/" + strings(i)
      }
    }
    str
  }
}
