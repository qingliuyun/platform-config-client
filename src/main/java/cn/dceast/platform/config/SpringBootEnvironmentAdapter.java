package cn.dceast.platform.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 环境适配器，此类适合spring boot
 * 默认读取本地配置文件，如果环境变量中CONFIG_CENTER_ENABLE=true
 *
 * @author zhang
 */
public class SpringBootEnvironmentAdapter implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        String str = "PAASConfigs";

        CommonConfigAdapter commonConfigAdapter = new CommonConfigAdapter();
        Properties prop = commonConfigAdapter.readPropertiesByEnvironment();

        Map<String, Object> map = new HashMap<>();
        prop.forEach((key, value) -> map.put((String)key, (String)value));

        MapPropertySource mps = new MapPropertySource(str, map);
        MutablePropertySources sources = configurableEnvironment.getPropertySources();
        String name = findPropertySource(sources);
        if (sources.contains(name)) {
            sources.addBefore(name, mps);
        } else {
            sources.addFirst(mps);
        }
    }

    private String findPropertySource(MutablePropertySources sources) {
        return "PAAS.CONFIG";
    }

}
