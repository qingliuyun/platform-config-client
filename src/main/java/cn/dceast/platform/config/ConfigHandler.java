package cn.dceast.platform.config;

import cn.dceast.platform.config.utils.HttpClientPlugin;
import cn.dceast.platform.config.utils.RtnData;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.Properties;

/**
 * Created by yangzhec on 2016/12/12.
 * 这个类是从配置中心拉取配置的工具类
 */
public class ConfigHandler {
    public static String CONFIG_MAP_URL= "%s/api/configs.do?env=%s&projectName=%s&profileName=%s";
    public static Properties loadFromConfigCenter(){
        //下面的环境变量在部署的时候都写在容器的环境变量中，可以直接使用，
        // 如果是debug环境，那么就从命令行参数中读取这些配置
        String configCenterHost, env, projectName, profileName, secretKey;
        boolean debug = (System.getenv("CONFIG_CENTER_HOST") == null);
        if(debug){
            //如果是在调试环境下
            configCenterHost = System.getProperty("CONFIG_CENTER_HOST");
            env = System.getProperty("CONFIG_ENV");
            projectName = System.getProperty("PROJECT_NAME");
            profileName = System.getProperty("PROFILE_NAME");
            secretKey = System.getProperty("SECRET_KEY");
        }else{
            configCenterHost = System.getenv("CONFIG_CENTER_HOST");
            env = System.getenv("CONFIG_ENV");
            projectName = System.getenv("PROJECT_NAME");
            profileName = System.getenv("PROFILE_NAME");
            secretKey = System.getenv("SECRET_KEY");
        }

        String url = String.format(CONFIG_MAP_URL, new Object[]{configCenterHost, env, projectName, profileName});
        Header[] headers = new Header[]{new BasicHeader("secretKey", secretKey)};
        String response = HttpClientPlugin.doGet(url, 3000, headers, null);
        RtnData rtnData = JSONObject.parseObject(response, RtnData.class);
        Properties properties= new Properties();
        if(rtnData.getStatus().equals("ERROR")) {
            throw new RuntimeException(String.format("配置中心服务接口异常:%s", new Object[]{rtnData.getMessage()}));
        } else {
            if(rtnData.getResult()!=null){
                JSONObject confMap = (JSONObject) rtnData.getResult();
                confMap.forEach((k,v) -> properties.put(k,v));
            }
        }
        return properties;
    }

}