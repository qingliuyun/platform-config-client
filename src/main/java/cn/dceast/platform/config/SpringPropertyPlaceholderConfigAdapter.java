package cn.dceast.platform.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.util.Properties;

/**
 * spring 根据环境变量加载属性配置
 *
 * @author zhang
 */
public class SpringPropertyPlaceholderConfigAdapter extends PropertyPlaceholderConfigurer {

    //加载的properties配置文件
    private String[] loadProperties;

    @Override
    protected Properties mergeProperties() throws IOException {
        CommonConfigAdapter commonConfigAdapter = new CommonConfigAdapter(loadProperties);
        return commonConfigAdapter.readPropertiesByEnvironment();
    }

    public String[] getLoadProperties() {
        return loadProperties;
    }

    public void setLoadProperties(String[] loadProperties) {
        this.loadProperties = loadProperties;
    }

}
