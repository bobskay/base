package wang.wangby.apicache;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import lombok.extern.slf4j.Slf4j;
import wang.wangby.apicache.cacheserver.CacheServer;
import wang.wangby.apicache.custom.ApiMock;
import wang.wangby.exception.Message;
import wang.wangby.model.request.RequestState;
import wang.wangby.utils.StringUtil;

@Slf4j
public class ApiIntercept {
	private ProceedingJoinPoint pjp;
	private CacheServer cacheServer;
	private Method method;
	private String name;
	private Object[] args;

	public ApiIntercept(ProceedingJoinPoint pjp, CacheServer cacheServer) {
		this.pjp = pjp;
		Signature sig = pjp.getSignature();
		MethodSignature msig = (MethodSignature) sig;
		this.method = msig.getMethod();
		this.args = pjp.getArgs();
		this.cacheServer=cacheServer;
		name = method.getDeclaringClass().getSimpleName()+":"+method.getName();
		log.debug("收到请求:" + name);
	}

	public Object process(ApiMock mock) throws Throwable {
		String requestId = mock.getRequestId(method, args);
		if (StringUtil.isEmpty(requestId)) {
			log.debug("没有requestId,走普通流程");
			return pjp.proceed();
		}
		ApiCache cache = new ApiCache(cacheServer, requestId, name);
		Integer state = cache.getState();
		if (state == null) {
			if (cache.setState(RequestState.PREPARE)) {
				state = RequestState.PREPARE.ordinal();
			} else {
				state = cache.getState();
			}
		}
		if (state == RequestState.RUNNING.ordinal()) {
			return mock.getRunningResult(method);
		}
		if (state == RequestState.FINISH.ordinal()) {
			return cache.getResult();
		}

		boolean putSuccess = cache.putResult(0);
		// 放入不成功,说明别的线程正在执行
		if (!putSuccess) {
			return mock.getRunningResult(method);
		}

		cache.setRunning();

		try {
			Object resp = pjp.proceed();
			cache.setFinish(resp,requestId);
			return resp;
		} catch (Exception ex) {
			if (!(ex instanceof Message)) {
				log.error("执行controller(" + name + ")出错:" + ex.getMessage(), ex);
			}
			return mock.getExceptionResult(method,ex);
		}

	}
	
}
