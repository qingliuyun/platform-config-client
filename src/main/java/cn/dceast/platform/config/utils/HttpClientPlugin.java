package cn.dceast.platform.config.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by owen on 2017/5/4.
 */
public class HttpClientPlugin {
    private static Logger logger = LoggerFactory.getLogger(HttpClientPlugin.class);

    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 120000;
    private static CloseableHttpClient httpClient = null;
    public static final String DEFAULT_ENCODING="utf-8";
    public static final int DEFAULT_MAX_CONNECTIONS=100;
    public static final String DEFAULT_CONTENT_TYPE="application/json";

    static {
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connMgr.setMaxTotal(DEFAULT_MAX_CONNECTIONS);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        // 在提交请求之前 测试连接是否可用
        configBuilder.setStaleConnectionCheckEnabled(true);
        requestConfig = configBuilder.build();
    }

    /**
     * 生产HttpClient实例
     * 公开，静态的工厂方法，需要使用时才去创建该单体
     */
    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.custom().setConnectionManager(connMgr).build();
        }
        return httpClient;
    }

    /**
     * POST请求
     * 1. 接收json参数
     * 2. 可传入header参数
     * @param apiUrl
     * @param jsonStr
     * @return
     */
    public static String doPost(String apiUrl, String jsonStr, Map<String,Object> header, String encoding) {
        String result = "";
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(requestConfig);

        if(encoding==null || "".equals(encoding.trim())){
            encoding=DEFAULT_ENCODING;
        }

        /**
         * 1.设置请求体
         */
        StringEntity reqEntity = new StringEntity(jsonStr,encoding);//解决中文乱码问题
        reqEntity.setContentEncoding(encoding);

        if(header.get("content-type")==null || "".equals(header.get("content-type"))){
            reqEntity.setContentType(DEFAULT_CONTENT_TYPE);
        }

        httpPost.setEntity(reqEntity);


        /**
         * 2. 设置header参数
         */
        httpPost.setHeader("Accept", DEFAULT_CONTENT_TYPE);
        if(header!=null){
            Iterator<String> iter=header.keySet().iterator();
            while(iter.hasNext()){
                String key=iter.next();
                httpPost.setHeader(key,header.get(key)==null?"":header.get(key).toString());
            }
        }

        try {

            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            logger.info(String.format("调用http接口 :%s,返回状态码 :%s", apiUrl,statusCode));

            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, encoding);
        } catch (IOException e) {
            logger.error("调用接口异常: " + e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(httpPost!=null){
                httpPost.releaseConnection();
            }
        }
        return result;
    }

    /**
     *
     * @param url
     * @param httpConnectionTimeout
     * @param headers
     * @param encoding
     * @return
     */
    public static String doGet(String url, int httpConnectionTimeout, Header[] headers, String encoding){
        CloseableHttpClient httpClient = getHttpClient();
        HttpGet httpget = null;
        CloseableHttpResponse response = null;
        try {
            URL urlObject = new URL(url);
            URI uri = new URI(urlObject.getProtocol(), null, urlObject.getHost(), urlObject.getPort(),
                    urlObject.getPath(), urlObject.getQuery(), null);
            httpget = new HttpGet(uri);

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(httpConnectionTimeout)
                    .setConnectTimeout(httpConnectionTimeout)
                    .build();

            httpget.setConfig(requestConfig);


            //设置http header信息
            if(headers != null && headers.length != 0){
                httpget.setHeaders(headers);
            }

            response = httpClient.execute(httpget);

            if(encoding==null || "".equals(encoding.trim())){
                encoding=DEFAULT_ENCODING;
            }

            return EntityUtils.toString(response.getEntity(), encoding);
        } catch (ConnectTimeoutException e) {
            logger.error("http connection time out", e);
            throw new RuntimeException("http connection time out", e);
        } catch (UnsupportedEncodingException e) {
            logger.error("unsupported encoding exception", e);
            throw new RuntimeException("unsupported encoding exception", e);
        } catch (ClientProtocolException e) {
            logger.error("client protocol exception", e);
            throw new RuntimeException("client protocol exception", e);
        } catch (IOException e) {
            logger.error("io exception", e);
            throw new RuntimeException("io exception", e);
        } catch (URISyntaxException e) {
            logger.error("URI syntax exception", e);
            throw new RuntimeException("URI syntax exception", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.warn("close response error", e);
            }
            try {
                if (httpget != null) {
                    httpget.releaseConnection();
                }
            } catch (Exception e) {
                logger.warn("release http connection error", e);
            }
        }
    }
}