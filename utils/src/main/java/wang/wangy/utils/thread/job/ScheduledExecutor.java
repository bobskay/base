package wang.wangy.utils.thread.job;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wang.wangby.log.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ScheduledExecutor {
    ScheduledExecutorService service;
    @Getter
    JobInfo jobInfo;
    Runnable run;
    public ScheduledExecutor(JobConfig jobConfig){
        jobInfo=new JobInfo();
        jobInfo.setInitialDelayMs(jobConfig.getTimeUnit().toMillis(jobConfig.getDelay()));
        jobInfo.setPeriodMs(jobConfig.getTimeUnit().toMillis(jobConfig.getPeriod()));
        jobInfo.setRunning(true);
        jobInfo.setName(jobConfig.getName());
        jobInfo.setCreateInfo(LogUtil.getExceptionText(new Exception("调用者堆栈")));

        service = Executors.newSingleThreadScheduledExecutor(new ScheduledThreadFactory(jobConfig.getName()));
        service.scheduleAtFixedRate(() -> {
            try {
                run();
            } catch (Throwable e) {
                log.error("执行计划任务出错:"+e.getMessage(),e);
            }
        }, jobConfig.getDelay(), jobConfig.getPeriod(), jobConfig.getTimeUnit());

        run=jobConfig.getRunnable();
    }

    private void run(){
        if(!jobInfo.isRunning()){
            return;
        }
        jobInfo.begin();
        run.run();
        jobInfo.end();
    }

    //暂停任务
    public  void pause( ) {
        jobInfo.setRunning(false);
    }

    //恢复
    public  void resume() {
        jobInfo.setRunning(true);
    }

    public void shutdown() {
        service.shutdown();
    }

    public static class ScheduledThreadFactory implements ThreadFactory {
        private final AtomicLong threadIndex = new AtomicLong(0);
        private final String threadNamePrefix;

        public ScheduledThreadFactory(final String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "scheduled_"+threadNamePrefix + this.threadIndex.incrementAndGet());
        }
    }
}
