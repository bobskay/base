package wang.wangy.utils.http;

import lombok.Data;
import org.apache.http.HttpHost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpConfig {
    //连接池相关
    //最大连接数
    private int maxTotal = 200;
    //默认的每个路由的最大连接数
    private int defaultMaxPerRoute = 100;
    //设置到某个路由的最大连接数，会覆盖defaultMaxPerRoute
    //new HttpRoute(new HttpHost("127.0.0.1", 80))
    private Map<HttpRoute, Integer> maxPerRoute = new HashMap<>();

    //socket配置
    //接收数据的等待超时时间，单位ms
    //private int soTimeout=500;
    //关闭Socket时，要么发送完所有数据，要么等待60s后，就关闭连接，此时socket.close()是阻塞的
    //如果设置了socket.setSoLinger(true, 0)，当调用socket.close()方法时，底层socket连接会立即关闭，
    // 此时HTTP响应结果有可能还未全部发送完毕
    //private int soLinger=60;
    //设置单个host的socket
    private Map<HttpHost, SocketConfig> socketConfig = new HashMap<>();

    //请求配置
    //连接超时
    private int connectTimeout = 2000;
    //读取超时
    private int socketTimeout = 5000;
    //从连接池获取链接超时
    private int connectionRequestTimeout = 500;
    //重试次数
    private int retryCount = 0;
    //是否重试
    private boolean retryEnabled = false;

    //使用https,如果keyStorePath为null就采取绕过证书的策略
    private String sslKeyStorePath;
    private String ssKeyStorepass;
}
