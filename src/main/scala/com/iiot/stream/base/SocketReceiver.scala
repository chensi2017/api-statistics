package com.iiot.stream.base

import java.io.InputStream
import java.net.ServerSocket

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.slf4j.LoggerFactory

class SocketReceiver(port:Int) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2){
  val log = LoggerFactory.getLogger(classOf[SocketReceiver])
  var server:ServerSocket = _

  override def onStart(): Unit = {
    log.info("初始化socket服务器...")
    server = new ServerSocket(port)
    log.info(s"初始化socket服务器成功！端口号:${port};")
    while (true) {
      new SocketResponse(server.accept(),this).start()
      println("有连接连入....")
    }
  }

  override def onStop(): Unit = {
    if(server!=null){
      server.close()
    }
  }

}
