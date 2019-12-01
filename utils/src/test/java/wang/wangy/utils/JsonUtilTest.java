package wang.wangy.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JsonUtilTest {

    @Test
    public void toString1() {
        Map map=new HashMap<>();
        map.put("string","a");
        map.put("int",12345);
        map.put("long",Long.MAX_VALUE);
        log.debug(new JsonUtil().toString(map));
    }
}