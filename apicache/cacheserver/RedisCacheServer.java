package wang.wangby.apicache.cacheserver;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


public class RedisCacheServer implements CacheServer{

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@Override
	public void mapPut(String key, String hashKey, Object value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
	}

	@Override
	public boolean mapPutIfAbsent(String key, String keyhashKey, Object value) {
		return  redisTemplate.opsForHash().putIfAbsent(key, keyhashKey, value);
	}

	@Override
	public Object mapGet(String key, String hashKey) {
		return redisTemplate.opsForHash().get(key, hashKey);
	}

	@Override
	public void put(String key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	@Override
	public boolean putIfAbsent(String key, Object value) {

		return redisTemplate.opsForValue().setIfAbsent(key, value);
	}

	@Override
	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public void execute(Consumer<CacheServer> command) {
		command.accept(this);
	}

	
}
