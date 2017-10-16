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
    public static String CONFIG_MAP_URL = "%s/api/configs.do?projectName=%s&profileName=%s";

    /**
     * 从配置中心读取配置信息
     * @return Properties
     */
    public static Properties loadFromConfigCenter() {
        //下面的环境变量在部署的时候都写在容器的环境变量中，可以直接使用
        String configCenterHost = System.getenv("CONFIG_CENTER_HOST");
        //String env = System.getenv("CONFIG_ENV");
        String projectName = System.getenv("CONFIG_PROJECT_NAME");
        String profileName = System.getenv("CONFIG_PROFILE_NAME");
        String secretKey = System.getenv("CONFIG_SECRET_KEY");

        String url = String.format(CONFIG_MAP_URL, new Object[]{configCenterHost, projectName, profileName});
        Header[] headers = new Header[]{new BasicHeader("secretKey", secretKey)};
        String response = HttpClientPlugin.doGet(url, 3000, headers, null);
        RtnData rtnData = JSONObject.parseObject(response, RtnData.class);
        Properties properties = new Properties();
        if (rtnData.getStatus().equals("ERROR")) {
            throw new RuntimeException(String.format("配置中心服务接口异常:%s", new Object[]{rtnData.getMessage()}));
        } else {
            if (rtnData.getResult() != null) {
                JSONObject confMap = (JSONObject) rtnData.getResult();
                confMap.forEach((k, v) -> properties.put(k, v));
            }
        }
        return properties;
    }

}