package com.iiot.stream.base

import java.io.{BufferedReader, DataInputStream, InputStreamReader, Reader}
import java.net.Socket

import scala.util.control.Breaks

class SocketResponse(socket: Socket, receiver: SocketReceiver) extends Thread{
  override def run(): Unit = {
    var in:DataInputStream = null
    var read:Reader = null
    var br:BufferedReader = null

    try{
      in = new DataInputStream(socket.getInputStream)
      read = new InputStreamReader(in)
      br = new BufferedReader(read)
      var s:String = null
      val loop = new Breaks
      loop.breakable{
        while(true){
          s = br.readLine()
          if(s==null)
            loop.break()
          else
            receiver.store(s)
        }
      }
    }catch {
      case e:Exception=>{
        if(in!=null){
          in.close()
        }
        if(read!=null){
          read.close()
        }
        if(br!=null){
          br.close()
        }
      }
    }
  }
}
