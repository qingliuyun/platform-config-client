package cn.dceast.platform.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * 普通java读取配置中心
 *
 * @author owen
 */
public class CommonConfigAdapter {

    private Logger log = LoggerFactory.getLogger(CommonConfigAdapter.class);

    private String[] loadProperties;

    private static final String DEFAULT_PROPERTIES_FILE = "application.properties";

    public CommonConfigAdapter() {

    }

    public CommonConfigAdapter(String[] loadProperties) {
        this.loadProperties = loadProperties;
    }

    /**
     * 根据环境变量，读取属性配置文件
     *
     * @return Properties
     */
    public Properties readPropertiesByEnvironment() {
        String adapterConfigPlace = System.getenv(Constants.CONFIG_CENTER_ENABLE);
        log.info(String.format("The value of environment 'CONFIG_CENTER_ENABLE' is : %s", adapterConfigPlace));
        /**
         * if the environment CONFIG_CENTER_ENABLE is not true, read properties from config file
         */
        if (!"true".equalsIgnoreCase(adapterConfigPlace)) {
            scanProperties();
            return readProperties();
        }else{
            /**
             * load properties from config center
             */
            Properties prop = ConfigHandler.loadFromConfigCenter();

            if (prop == null) {
                throw new RuntimeException("The config of config center is not exists!");
            }

            return prop;
        }
    }

    /**
     * 如果loadProperties为空，加载classpath下面所有的properties文件
     */
    private void scanProperties() {
        if (loadProperties != null && loadProperties.length > 0) {
            return;
        }

        String parentPath = null;
        try {
            parentPath = URLDecoder.decode(this.getClass().getClassLoader().getResource(".").getPath(), Constants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        log.info(String.format("Scan dir:%s", parentPath));

        File file = new File(parentPath);


        Collection<File> collection = FileUtils.listFiles(file, FileFilterUtils.suffixFileFilter(".properties"), null);
        if (collection == null || collection.size() == 0) {
            return;
        }

        Iterator<File> iterator = collection.iterator();
        loadProperties = new String[collection.size()];
        int i = 0;
        while (iterator.hasNext()) {
            File tempFile = iterator.next();
            loadProperties[i++] = tempFile.getName();
            log.info(String.format("load file:%s", tempFile.getName()));
        }
    }

    /**
     * 读取bean中配置的properties文件, 如果没有则读取application.properties
     *
     * @return
     */
    private Properties readProperties() {
        Properties properties = new Properties();
        if (loadProperties != null && loadProperties.length > 0) {
            for (int i = 0; i < loadProperties.length; i++) {
                Properties temp = readPropertiesFromFile(loadProperties[i], Constants.DEFAULT_ENCODING);
                copyProperties(temp, properties);
            }
        } else {
            try {
                copyProperties(readPropertiesFromFile(DEFAULT_PROPERTIES_FILE, Constants.DEFAULT_ENCODING), properties);
            } catch (Exception e) {
                log.warn("Read the default config file 'application.properties', but it is not exists!");
            }
        }

        return properties;
    }

    /**
     * 从文件中读取属性配置
     *
     * @param fileName
     * @param encoding
     * @return
     */
    private Properties readPropertiesFromFile(String fileName, String encoding) {
        InputStream inputStream = null;
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                throw new IllegalArgumentException("Properties file not found in classpath: " + fileName);
            }
            Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, encoding));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file.", e);
        } finally {
            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将src的属性复制给dest
     *
     * @param src
     * @param dest
     * @return
     */
    private Properties copyProperties(Properties src, final Properties dest) {
        if (src == null || src.isEmpty()) {
            return dest;
        }

        src.forEach((k,v) -> dest.put(k,v));
        return dest;
    }

    public String[] getLoadProperties() {
        return loadProperties;
    }

    public void setLoadProperties(String[] loadProperties) {
        this.loadProperties = loadProperties;
    }

}
