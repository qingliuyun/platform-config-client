package cn.dceast.platform.config.utils;

import cn.dceast.platform.config.Constants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 静态资源处理器
 *
 * @author zhang
 */
public class StaticResourceProcessorUtil {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceProcessorUtil.class);

    public static void processor(String fullFilePath, Map<String, String> replace) {
        File file = new File(fullFilePath);
        if (file.isDirectory()) {
            log.warn(String.format("The file %s is a directory!", fullFilePath));
            return;
        }

        if (!file.exists()) {
            log.warn(String.format("The file %s is not exists!", fullFilePath));
            return;
        }

        try {
            String src = FileUtils.readFileToString(file, Constants.DEFAULT_ENCODING);
            src = replaceAll(src, replace);
            FileUtils.write(file, src, Constants.DEFAULT_ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String replaceAll(String src, Map<String, String> replace) {
        if (src == null || "".equals(src.trim()) || replace == null || replace.size() == 0) {
            return src;
        }

        Iterator<Entry<String, String>> iterator = replace.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            src = src.replaceAll(entry.getKey(), entry.getValue());
        }

        return src;
    }

}