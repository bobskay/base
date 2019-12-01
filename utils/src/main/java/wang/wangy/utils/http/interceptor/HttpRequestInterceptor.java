package wang.wangy.utils.http.interceptor;

import org.apache.http.client.methods.HttpUriRequest;

//http请求拦截器
public interface HttpRequestInterceptor {
    void intercept(HttpUriRequest httpUriRequest);

    //请求头加上json标识
    static HttpRequestInterceptor jsonRequestInterceptor(){
        return request->{
          request.addHeader("Content-Type","application/json; charset=utf-8");
        };
    }

    //请求头加上权限验证的token
    static HttpRequestInterceptor bearer(String token){
        return request->{
            request.addHeader("Authorization","Bearer "+token);
        };
    }
}
