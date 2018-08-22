package com.iiot.stream.bean

import java.sql.Timestamp
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class Item (allowed:String,
                 date:String,
                 method:String,
                 var reqUrl:String,
                 requestTime:Long,
                 responseTime:Long,
                 srcIP:String,
                 srcPort:Int,
                 useTime:Long)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Log (tenantId:String,
                operatorId:String,
                ip:String,
                url:String,
                createTime:Long,
                logSource:String,
                modular:String,
                moduleFunction:String,
                operateType:String,
                referer:String,
                success:Int,
                failureDesc:String,
                queryString:String,
                responseTimeLength:Long,
                requestMethod:String
               )