package wang.wangy.utils.thread.job;

import lombok.Data;
import wang.wangby.annotation.Remark;

import java.util.concurrent.TimeUnit;

@Data
public class JobConfig {
    @Remark("任务创建后多久执行")
    private int delay;
    @Remark("执行间隔")
    private int period;
    @Remark("时间单位")
    private TimeUnit timeUnit=TimeUnit.SECONDS;
    @Remark("具体执行的任务")
    private Runnable runnable;
    @Remark("任务名称,每个虚拟机内唯一")
    private String name;
}
