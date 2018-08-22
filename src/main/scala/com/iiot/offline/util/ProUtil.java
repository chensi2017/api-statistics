package com.iiot.offline.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProUtil {
    private static Logger log = Logger.getLogger(ProUtil.class);
    private Properties properties;
    public static final String DefaultConfigPath = "conf/config.properties";
    private static volatile ProUtil cfg = null;

    private final static String ERR_MSG = "Can not aquire value from config file: ";

    private ProUtil() {
        properties = new Properties();
        InputStream is = null;
        try {
            try {
                is = new FileInputStream(DefaultConfigPath);
            }catch (Exception e){
                log.error("Can not read config from ./conf/config.properties!!!trying to read from resource path...");
                is = ProUtil.class.getResourceAsStream("/conf/config.properties");
            }
            BufferedReader bf = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            properties.load(bf);
        } catch (Exception e) {
            log.error("Can't read the properties file from resource path!!!");
            log.error(e,e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void set(String key,String value){
        properties.setProperty(key,value);
    }

    public void store() throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(DefaultConfigPath);
            properties.store(fw,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }catch (IOException e){
            log.error(e,e);
            throw e;
        }finally {
            fw.close();
        }

    }


    public static ProUtil getInstance() {
        if (cfg == null) {
            synchronized (ProUtil.class) {
                if (cfg == null) {
                    cfg = new ProUtil();
                }
            }
        }
        return cfg;
    }

    /**
     * Retun a value for certain key.
     * @param key
     * @return value
     */
    public String getValue(String key) {
        if (!properties.containsKey(key))
            return null;
        String value = properties.getProperty(key);
        if (value == null) {
            System.out.println(ERR_MSG + ":" + key);
        }

        return value;
    }

    public String getValueOrElse(String key,String elseValue){
        if (!properties.containsKey(key))
            return null;
        String value = properties.getProperty(key);
        if (value == null) {
            value = elseValue;
        }
        return value;
    }

    public Properties getPro(){
        return properties;
    }

    public Map<String,String> getMap(){return new HashMap<String,String>((Map)properties);}

    public void printInfo(){
        Enumeration en = properties.propertyNames();
        while(en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = properties.getProperty(strKey);
            System.out.println(strKey + "=" + strValue);
        }
    }

}