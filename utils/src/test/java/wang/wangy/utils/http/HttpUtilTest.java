package wang.wangy.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class HttpUtilTest  {

    @Test
    public void getTest() throws IOException {
        String s = HttpUtil.get(Config.TESTURL);
        log.debug("httpGet结果\n" + s);
    }
}
