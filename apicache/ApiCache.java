package wang.wangby.apicache;

import wang.wangby.apicache.cacheserver.CacheServer;
import wang.wangby.model.request.RequestState;

public class ApiCache {

	private CacheServer cacheServer;
	private String requestId;
	private String stateKey;
	private String finishKey;

	public ApiCache(CacheServer cacheServer, String requestId, String methodName) {
		this.cacheServer = cacheServer;
		this.requestId = requestId;
		this.stateKey = methodName + ":state";
		this.finishKey = methodName + ":" + "finish:" + requestId;
	}

	// 当前请求状态
	public Integer getState() {
		return (Integer) cacheServer.mapGet(stateKey, requestId);
	}

	// 设置状态,如果已经有了,就返回false
	public boolean setState(RequestState state) {
		return cacheServer.mapPutIfAbsent(stateKey, requestId, state.ordinal());
	}

	public Object getResult() {
		return cacheServer.get(finishKey);
		
	}

	public boolean putResult(Object value) {
		return cacheServer.putIfAbsent(finishKey, value);
	}

	//将任务设为执行中
	public void setRunning() {
		 cacheServer.mapPut(stateKey, requestId, RequestState.RUNNING.ordinal());
	}

	//设置任务完成
	public void setFinish(Object result,String requestId) {
		cacheServer.execute(ser->{
			ser.mapPut(stateKey, requestId, RequestState.FINISH.ordinal());
			ser.put(finishKey, result);
		});
		
	}
}
