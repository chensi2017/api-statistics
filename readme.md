**部署方式**
data-source:Kafka
1.解压项目zip包
2.进入bin/conf目录,配置config.properties文件
3.进入bin目录执行命令: ./loganalysis >/dev/null 2>&1 &
===================================================================================
**项目运行情况检查**
执行命令后会在bin目录下产生out.log日志文件,日志文件中会有sparkUI的端口号

Redis中的key
按天计算(logSource:openAPI)

> * 各个接口累计访问次数         api_log_filter_times_yyyy-MM-dd
> * 每个接口每分钟最大访问次数   api_log_filter_times_perminute_yyyy-MM-dd
> * 每个接口响应总时长           api_log_filter_usetime_yyyy-MM-dd

模块访问统计
key: ht:log:module:<20180822> field: <module>
===================================================================================
**History**
2018.8.29
修复HTInputDStreamFormat模块计算值的双倍问题
二进制包增添readme.md文件至bin/conf目录下
将文档从根目录移动到document文件夹下