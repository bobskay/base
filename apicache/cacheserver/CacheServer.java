package wang.wangby.apicache.cacheserver;

import java.util.function.Consumer;

/**保存缓存的服务器*/
public interface CacheServer {

	/**放入值到map*/
	public void mapPut(String name, String key, Object value);

	/**如果map里没有就放入*/
	public boolean mapPutIfAbsent(String name, String key, Object value);

	/**从map获取值*/
	public Object mapGet(String name, String key);

	/**将数据放到缓存*/
	public void put(String key, Object value);

	/**如果数据不存在就访日*/
	public boolean putIfAbsent(String key, Object value);

	/**通过key获得数据*/
	public Object get(String key);
	
	//在一个事务里执行
	public void execute(Consumer<CacheServer> command);
}
