import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 将redis中的apilog处理的结果存到mysql中
 */
public class RedisToMysql {
    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("192.168.0.79",6379);
        jedis.auth("HTdata");
        Set<String> timesKey = jedis.keys("api_log_filter*");
        Map<String,Map<String,String>> map = new HashMap<>();
        for(String key:timesKey){
                Map<String, String> resultMap = jedis.hgetAll(key);
                map.put(key,resultMap);
        }
//        System.out.println(timesKey.size());
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost/test?serverTimezone=UTC";
        Connection connection = DriverManager.getConnection(url,"root","root");
        Statement sta = connection.createStatement();
        for(Map.Entry<String,Map<String,String>> entry:map.entrySet()){
            String key = entry.getKey();
            String date = key.substring(key.indexOf("2"),key.length());
            Map<String, String> value = entry.getValue();
            for(Map.Entry<String,String> e:value.entrySet()){
                if(key.matches("api_log_filter_times_[0-9]{4}-[0-9]{2}-[0-9]{2}")){
                    int i = sta.executeUpdate("update apilogfilter set times='" + e.getValue() + "' where requrl='" + e.getKey() + "' and date='"+date+"';");
                    if(i==0){
                        sta.execute("insert into apilogfilter(date,requrl,times) values('"+date+"','"+e.getKey()+"','"+e.getValue()+"') ");
                    }
                }
                if(key.matches("api_log_filter_usetime_[0-9]{4}-[0-9]{2}-[0-9]{2}")){
                    int i = sta.executeUpdate("update apilogfilter set usetime='" + e.getValue() + "' where requrl='" + e.getKey() + "' and date='"+date+"';");
                    if(i==0){
                        sta.execute("insert into apilogfilter(date,requrl,usetime) values('"+date+"','"+e.getKey()+"','"+e.getValue()+"') ");
                    }
                }
                if(key.matches("api_log_filter_times_perminute_[0-9]{4}-[0-9]{2}-[0-9]{2}")){
                    int i = sta.executeUpdate("update apilogfilter set times_perminute='" + e.getValue() + "' where requrl='" + e.getKey() + "' and date='"+date+"';");
                    if(i==0){
                        sta.execute("insert into apilogfilter(date,requrl,times_perminute) values('"+date+"','"+e.getKey()+"','"+e.getValue()+"') ");
                    }
                }
            }
        }
        sta.close();
        connection.close();
    }
}
