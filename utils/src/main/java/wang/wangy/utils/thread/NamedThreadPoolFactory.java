package wang.wangy.utils.thread;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

//自定义线程名称
public class NamedThreadPoolFactory implements ThreadFactory {
    private final AtomicLong threadIndex = new AtomicLong(0);
    private final String threadNamePrefix;

    public NamedThreadPoolFactory(final String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, threadNamePrefix + this.threadIndex.incrementAndGet());

    }
}