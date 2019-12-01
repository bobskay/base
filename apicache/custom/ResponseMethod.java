package wang.wangby.apicache.custom;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.AnnotationUtils;

import lombok.extern.slf4j.Slf4j;
import wang.wangby.annotation.Api;
import wang.wangby.apicache.ApiIntercept;
import wang.wangby.apicache.cacheserver.CacheServer;
import wang.wangby.exception.Message;
import wang.wangby.model.request.Request;
import wang.wangby.model.request.Response;
import wang.wangby.utils.BeanUtil;
import wang.wangby.utils.StringUtil;

//默认配置的要拦截的方法,客户端应该根据实际情况自己配
@Aspect
@Slf4j
public class ResponseMethod implements ApiMock {
	CacheServer cacheServer;

	public void setCacheServer(CacheServer cacheServer) {
		this.cacheServer = cacheServer;
	}

	@Pointcut("@annotation(wang.wangby.annotation.Api) && execution(wang.wangby.model.Response *.*(..))")
	private void apiMethod() {
	}

	@Around("apiMethod()")
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		return new ApiIntercept(pjp, cacheServer).process(this);
	}

	@Override
	public Object getRunningResult(Method method) {
		Response resp = Response.fail("处理中请稍后");
		return resp;
	}

	@Override
	public Object getExceptionResult(Method method, Exception ex) {
		String name = method.getDeclaringClass().getName() + "." + method.getName();
		if (!(ex instanceof Message)) {
			log.error("执行controller(" + name + ")出错:" + ex.getMessage(), ex);
		}
		Response resp = Response.fail("执行方法" + name + "出现异常:" + ex.getMessage());
		return resp;
	}

	@Override
	public String getRequestId(Method method, Object[] args) {
		return getApiRequestId(method,args);
	}

	// 获得请求唯一标识
	private String getApiRequestId(Method method, Object[] args) {
		Api api = AnnotationUtils.getAnnotation(method, Api.class);
		String idName = "";
		if (api != null) {
			idName = api.requestId();
		}
		if (StringUtil.isEmpty(idName)) {
			// 如果参数是Request,可以不用配置requestId
			if (args[0] instanceof Request) {
				return ((Request) args[0]).getRequestId();
			} else {
				return null;
			}
		}
		String[] names = idName.split("\\.");
		Parameter[] pas = method.getParameters();
		for (int i = 0; i < pas.length; i++) {
			if (pas[i].getName().equals(names[0])) {
				return getRequestId(args[i], names);
			}
		}
		log.warn("{}requestId配置错误:{}", method.getName(), idName);
		return null;
	}

	private String getRequestId(Object obj, String[] names) {
		if (obj == null) {
			return null;
		}
		// 第一个name是自己
		if (names.length == 1) {
			return obj == null ? null : obj + "";
		}
		for (int i = 1; i < names.length; i++) {
			obj = BeanUtil.get(obj, names[i]);
		}
		return obj == null ? null : obj + "";
	}

}
