package com.iiot.offline

import java.sql.Timestamp

import com.iiot.offline.util.ReqUrlFormat
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions._
/*
 *本类用于离线分析接口日志文件
 */
object  HTLogOffLineAnalysis{
  def main(args: Array[String]): Unit = {
    val Array(warehousedir)=args
    System.setProperty("org.spark.warehouse.dir",warehousedir)
    System.setProperty("HADOOP_USER_NAME","hdfs")
    val spark = SparkSession.builder().config(new SparkConf()
//      .setMaster("local[5]")
//      .set("spark.sql.streaming.checkpointLocation","file:///d:/checkpoint")
      ).appName("HTLogOffLineAnalysis").getOrCreate()
    val name="2018-05-15"
    var df =spark.read.text(s"file:///d:/filter/filter.log.${name}").toDF()
    import spark.implicits._
    //格式化DataSet
    //(method+reqUrl,timestamp,usetime,ip)
    var ds:Dataset[(String,Timestamp,Long,String)] = df.selectExpr("cast(value as string)").as[String].map(x=>{
      try {
        val regex = """method":"([A-Z]+).*reqUrl":"((/[0-9\.a-zA-Z]*)+).*requestTime":([0-9]+).*srcIP":"((([0-9]+\.){3}[0-9]+))".*useTime":([0-9]+)""".r
        val r = regex.findFirstMatchIn(x).map(item=>{
          //(method,reqUrl,timestamp,srcIp,useTime)
          (item group 1,item group 2,item group 4,item group 6,item group 8)
        })
        val t = r.get
        //(method+reqUrl,timestamp,useTime,srcIp)
        (ReqUrlFormat.formateReqUrl(t._2,t._1),new Timestamp(t._3.toLong),t._5.toLong,t._4)
      }catch {
        case e:Exception=> ("",null,0,"")
      }
    })
    ds.cache()
    //按小时统计访问最多的接口
    val res = ds.groupBy(window($"_2","60 minutes","60 minutes").as("time"),$"_1".as("reqUrl")).count().orderBy($"time").createOrReplaceTempView("temp")
    spark.sql("select t.time,t1.reqUrl,t.max from (select time,max(count) max from temp group by time) t left join temp t1 on t.max=t1.count and t.time=t1.time").show(100,false)
    //按小时统计IP访问次数
    val res2 = ds.groupBy(window($"_2","60 minutes","60 minutes").as("time"),$"_4".as("ip")).count().orderBy($"time",$"count" desc).show(100,false)
    //按分钟统计访问次数最多的接口
    val res3 = ds.groupBy(window($"_2","1 minute","1 minute") as "time",$"_1" as "reqUrl").count().createOrReplaceTempView("temp3")
    spark.sql("select temp3.time,temp3.requrl,t.maxcount from (select time,max(count) as maxcount from temp3 group by time) t left join temp3 on t.time=temp3.time and t.maxcount=temp3.count").orderBy($"time",$"count").show(100,false)

  }

}