package wang.wangy.utils.thread.job;

import lombok.Data;
import wang.wangby.annotation.Remark;

@Data
@Remark("计划任务信息")
public class JobInfo {

    private long initialDelayMs;
    private long periodMs;
    private String name;
    private boolean running;
    private long lastBegin;
    private long lastEnd;
    private Integer count=0;//已经执行次数
    private String createInfo;

    public void begin() {
        count++;
        lastBegin = System.currentTimeMillis();
    }

    public void end() {
        lastEnd =  System.currentTimeMillis();
    }

}
