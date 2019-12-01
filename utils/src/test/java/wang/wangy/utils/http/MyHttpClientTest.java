package wang.wangy.utils.http;

import lombok.extern.slf4j.Slf4j;
import wang.wangy.utils.http.interceptor.HttpRequestInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MyHttpClientTest {

    @org.junit.Test
    public void get() throws IOException {
        HttpConfig config=new HttpConfig();
        config.setSocketTimeout(1000);
        config.setConnectTimeout(1000);
        List<HttpRequestInterceptor> list=new ArrayList<>();
        list.add(HttpRequestInterceptor.jsonRequestInterceptor());
        MyHttpClient client=HttpUtil.createClient(config,Config.TESTURL,list);
        String s=client.get("/");
        log.debug(s.replace("\\n","\n"));
    }
}