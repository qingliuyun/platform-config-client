package cn.dceast.platform.config.listener;

import cn.dceast.platform.config.CommonConfigAdapter;
import cn.dceast.platform.config.Constants;
import cn.dceast.platform.config.utils.StaticResourceProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 替换静态资源中的配置属性
 *
 * @author zhang
 */
public class ReplaceStaticResourceListener implements ServletContextListener {

    private Logger log = LoggerFactory.getLogger(ReplaceStaticResourceListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String replaceFiles = sce.getServletContext().getInitParameter("replaceFiles");//替换的文件

        if (replaceFiles == null || "".equals(replaceFiles.trim())) {
            return;
        }

        String splitStr = ",";//分隔符
        String[] files = replaceFiles.split(splitStr);
        Map<String, String> replacement = getReplacement(sce);

        String parentPath = null;
        try {
            parentPath = URLDecoder.decode(this.getClass().getClassLoader().getResource("../../").getPath(), Constants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (String fileName : files) {
            if (fileName == null || "".equals(fileName.trim())) {
                continue;
            }

            fileName = fileName.trim();

            if (fileName.startsWith(System.getProperty("file.separator"))) {
                fileName = fileName.substring(1);
            }

            log.info(String.format("Replace file '%s' by '%s'", parentPath + fileName, replacement));
            StaticResourceProcessorUtil.processor(parentPath + fileName, replacement);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private Map<String, String> getReplacement(ServletContextEvent sce) {
        String replaceFileStr = sce.getServletContext().getInitParameter("replaceFileStrs"); //替换的属性字段
        //属性文件，替换属性值的来源。默认读取properties中的值
        String replaceProperties = sce.getServletContext().getInitParameter("replaceProperties");

        if (replaceFileStr == null || "".equals(replaceFileStr.trim())) {
            return null;
        }

        String splitStr = ",";//分隔符
        String[] attributes = replaceFileStr.split(splitStr);

        //读取配置
        CommonConfigAdapter ccfa = new CommonConfigAdapter();
        if (replaceProperties != null && !"".equals(replaceProperties)) {
            ccfa.setLoadProperties(replaceProperties.split(splitStr));
        }

        Properties prop = ccfa.readPropertiesByEnvironment();
        Map<String, String> replacement = new HashMap<>();

        for (String attr : attributes) {
            if (attr == null || "".equals(attr.trim())) {
                continue;
            }

            attr = attr.trim();
            String value = prop.getProperty(attr);
            attr = attr.replaceAll("\\.", "\\\\\\.");
            replacement.put("\\$\\{" + attr + "\\}", value == null ? "" : value);
        }

        return replacement;
    }

}
