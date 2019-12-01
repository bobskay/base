package wang.wangy.utils.thread;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wang.wangby.log.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ThreadPool {
    @Getter
    private String name;
    //超过线程池最大值,失败次数
    private AtomicLong rejected = new AtomicLong();
    private AtomicLong errorCount = new AtomicLong();
    private ThreadPoolExecutor threadPool;
    @Getter
    private String creator;

    public ThreadPool(String name, int corePoolSiz) {
        this(name, corePoolSiz, corePoolSiz * 2, corePoolSiz, 30);
    }

    public ThreadPool(String name, int corePoolSize, int maximumPoolSize, int queue, int keepAliveTime) {
        this.creator = LogUtil.getExceptionText(new Exception("线程池创建信息"));
        this.name = "threadPool_" + name;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = null;
        if (queue == 0) {
            workQueue = new SynchronousQueue();
        } else {
            workQueue = new LinkedBlockingQueue(queue);
        }
        RejectedExecutionHandler rejectedExecutionHandler = (Runnable run, ThreadPoolExecutor executor) -> {
            rejected.incrementAndGet();
            log.error("线程池满了,丢弃当前任务:" + name + ":" + rejected);
        };
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new NamedThreadPoolFactory(name), rejectedExecutionHandler);
    }

    public void run(Runnable runnable) {
        threadPool.execute(() -> {
            try {
                runnable.run();
            } catch (RuntimeException ex) {
                ;
                errorCount.incrementAndGet();
                throw ex;
            }
        });
    }

    public Map<Object, Object> getDataMap() {
        Map map = new HashMap();
        //当前正在执行
        map.put("active", threadPool.getActiveCount());
        //已经完成的个数
        map.put("complete", threadPool.getCompletedTaskCount());
        //曾经最大线程池个数
        map.put("poolSize", threadPool.getPoolSize());
        map.put("task", threadPool.getTaskCount());
        map.put("queue", threadPool.getQueue().size());
        map.put("rejected", rejected.get());
        map.put("error", errorCount.get());
        return map;
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}
