## 本工程为oss平台提供的配置中心的客户端，主要目的为了部署在平台上的应用可以从配置中心获取配置。
## 支持OSS平台配置中心、本地properties文件。默认读取工程中的properties文件，最少要有一个application.properties

## 客户端集成
#### Spring boot工程，只需要将此工程jar包放入工程即可。pom.xml 中添加如下依赖
    <dependency>
        <groupId>cn.dceast.platform</groupId>
        <artifactId>platform-config-client</artifactId>
        <version>2.3.0</version>
    </dependency>

#### 普通Spring工程，需要额外按照如下方式配置bean
    <bean id="propertyPlaceholderConfigAdapter" class="cn.dceast.platform.config.adapter.SpringPropertyPlaceholderConfigAdapter">
        <property name="loadProperties">
            <list>
                <value>application.properties</value>
                <value>cas.properties</value>
            </list>
        </property>
    </bean>
    
#### 普通java web工程
  使用 CommonConfigAdapter类。此类型readPropertiesByEnvironment()方法可以根据环境变量决定是读取本地properties文件还是读取配置中心。
  此方法默认读取application.properties。可以自定义设置此类的loadProperties属性。

#### 对于静态资源中，需要替换属性变量 在web.xml中如下配置
    <context-param>
        <param-name>replaceFiles</param-name>
        <param-value>1.js,2.js,3.css</param-value>
    </context-param>
    <context-param>
        <param-name>replaceFileStrs</param-name>
        <param-value>co.test.helper</param-value>
    </context-param>
    <context-param>
        <param-name>replaceProperties</param-name>
        <param-value>application.properties,cas.properties</param-value>
    </context-param>
  
    <listener>
        <listener-class>cn.dceast.platform.config.adapter.listener.ReplaceStaticResourceListener</listener-class>
    </listener>
    
    replaceFiles：需要替换的静态文件清单。
    replaceFileStrs：需要替换的属性。
    replaceProperties：默认替换的属性值的来源。

####  应用详情页面设置环境变量
      *  CONFIG_CENTER_ENABLE = true 开启从配置中心读配置
      *  CONFIG_CENTER_HOST = "http://221.178.232.93:86/oss-config-center" 配置中心的host
      *  CONFIG_PROJECT_NAME = "demo-huajian"  项目名称
      *  CONFIG_PROFILE_NAME = "profile03"  配置组名称
      *  CONFIG_SECRET_KEY = "rh2p6w6sgas" 配置组安全key

