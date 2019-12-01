package wang.wangy.utils.thread.job;

/**
 * 当时任务,容器启动时会查找所有实现Job接口的类,并启动计划任务
 * */
public interface Job {
    JobConfig jobConfig();
}
