package wang.wangby.apicache.custom;

import java.lang.reflect.Method;

/** 设置mock异常处理规则 */
public interface ApiMock {

	/**
	 * 当其他服务器正在执行方法的时候,接口调用的返回值
	 * @param method 请求的方法
	 * */ 
	public Object getRunningResult(Method method);

	/**
	 * 当调用方法出现异常后,接口的返回值
	 * 
	 * @param method 请求的方法
	 * @param ex     异常信息
	 */
	public Object getExceptionResult(Method method, Exception ex);

	/**
	 * 获得请求的唯一标识
	 * 
	 * @param method 请求方法
	 * @param args   方法参数
	 */
	public String getRequestId(Method method, Object[] args);
}
