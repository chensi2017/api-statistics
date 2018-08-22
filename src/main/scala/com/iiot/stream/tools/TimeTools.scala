package com.iiot.stream.tools

import java.util.Date
import java.text.SimpleDateFormat

object TimeTools {
  private val sdf = new SimpleDateFormat("yyyy-MM-dd")
  private val sdf2 = new SimpleDateFormat("yyyyMMdd")
  def formatTime(ts:Long): String ={
    sdf.format(new Date(ts))
  }
  def getToday(): String ={
    sdf2.format(new Date())
  }
}
