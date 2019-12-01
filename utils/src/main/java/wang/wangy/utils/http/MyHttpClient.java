package wang.wangy.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import wang.wangy.utils.http.interceptor.HttpRequestInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class MyHttpClient {
    CloseableHttpClient httpClient;
    String baseUrl;
    List<HttpRequestInterceptor> requestIntercepts;

    /**
     * @param httpConfig http配置
     */
    public MyHttpClient(HttpConfig httpConfig, String baseUrl, List<HttpRequestInterceptor> requestIntercepts) {
        this.httpClient = HttpUtil.createClient(httpConfig);
        if (baseUrl == null) {
            baseUrl = "";
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
        this.requestIntercepts = requestIntercepts;
    }

    /**
     * 创建reequest
     *
     * @param url           请求的url不包括前缀
     * @param requestMethod 请求的方法类型
     * @param body          请求体内容,可以为空
     */
    public HttpUriRequest createRequest(String url, HttpRequestMethod requestMethod, String body) {
        url = getUrl(url);
        return HttpUtil.createRequest(url, requestMethod, body, requestIntercepts);
    }

    public <T> T invoke(HttpUriRequest request, Function<CloseableHttpResponse, T> callback) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return callback.apply(response);
        }
    }

    public String invoke(String url, HttpRequestMethod method) throws IOException {
        url = getUrl(url);
        HttpUriRequest request = HttpUtil.createRequest(url, method, null, requestIntercepts);
        log.debug("准备{}请求:{}", method, url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return HttpUtil.responseToString(response);
        }
    }

    public String getString(HttpUriRequest request) throws IOException {
        Function<CloseableHttpResponse, String> callback = HttpUtil::responseToString;
        return invoke(request, callback);
    }

    public String get(String url) throws IOException {
        return invoke(url, HttpRequestMethod.GET);
    }

    //如果请求地址不是以htp开头就加上前缀
    public String getUrl(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        if (!url.startsWith("/")) {
            return baseUrl + "/" + url;
        }
        return baseUrl + url;
    }
}
