package wang.wangy.utils.thread;

import lombok.Data;
import wang.wangby.annotation.Remark;

//线程池配置信息
@Data
public class ThreadPoolConfig {
    @Remark("名称")
    private String name;
    @Remark("核心线程数")
    private int core;
    @Remark("最大线程数")
    private int max;
    @Remark("最大排队数")
    private int queueSize;
    @Remark("最长空闲时间")
    private int keepAliveSecond;

    public static ThreadPoolConfig newInstance(String name, int core){
        ThreadPoolConfig c=new ThreadPoolConfig();
        c.setCore(core);
        c.setName(name);
        c.max=core*2;
        c.queueSize=core;
        c.keepAliveSecond=30;
        return c;
    }
}
