package wang.wangby.monitor.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wang.wangby.test.TestBase;
import wang.wangby.testcase.model.JvmInfo;
import wang.wangby.testcase.model.vo.Usage;
import wang.wangby.testcase.service.JvmService;
import wang.wangby.utils.StrBuilder;
import wang.wangby.utils.StringUtil;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
public class JvmServiceTest extends TestBase {


    @Test
    public void getJvm() throws InterruptedException {
        byte[] bytes=new byte[1024*1024*10];
        JvmInfo jvmInfo=new JvmService().getMemroy();
        log.debug("eden="+showUsage(jvmInfo.getEden())
                +",old="+showUsage(jvmInfo.getOld())
                +",survivor="+showUsage(jvmInfo.getSurvivor())
        );
    }

    private String showUsage(Usage usage){
        StrBuilder sb=new StrBuilder();
        sb.append("us:"+toM(usage.getUsed())+",");
        sb.append("max:"+toM(usage.getMax())+",");
        sb.append("cmit:"+toM(usage.getCommitted())+",");
        sb.append("init:"+toM(usage.getInit())+",");
        return sb.toString();
    }

    private String toM(long value){
        double d=value;
        d=d/1024/1024;
        String m= StringUtil.round(d)+"M";
        return m;
    }


    @Test
    public void printSummary() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        for (GarbageCollectorMXBean gb : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println(gb.getName() + ":" + gb.getCollectionCount() + ":" + gb.getCollectionTime());
        }
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
        System.out.printf("jvmName:%s %s %s%n", runtime.getVmName(), "version", runtime.getVmVersion());
        System.out.printf("jvmJavaVer:%s%n", System.getProperty("java.version"));
        System.out.printf("jvmVendor:%s%n", runtime.getVmVendor());
        System.out.printf("jvmUptime:%s%n", toDuration(runtime.getUptime()));
        System.out.printf("threadsLive:%d%n", threads.getThreadCount());
        System.out.printf("threadsDaemon:%d%n", threads.getDaemonThreadCount());
        System.out.printf("threadsPeak:%d%n", threads.getPeakThreadCount());
        System.out.printf("threadsTotal:%d%n", threads.getTotalStartedThreadCount());
        System.out.printf("heapCurr:%d%n", mem.getHeapMemoryUsage().getUsed() / Kb);
        System.out.printf("heapMax:%d%n", mem.getHeapMemoryUsage().getMax() / Kb);
        System.out.printf("heapCommitted:%d%n", mem.getHeapMemoryUsage().getCommitted() / Kb);
        System.out.printf("osName:%s %s %s%n", os.getName(), "version", os.getVersion());
        System.out.printf("osArch:%s%n", os.getArch());
        System.out.printf("osCores:%s%n", os.getAvailableProcessors());
        System.out.printf("clsCurrLoaded:%s%n", cl.getLoadedClassCount());
        System.out.printf("clsLoaded:%s%n", cl.getTotalLoadedClassCount());
        System.out.printf("clsUnloaded:%s%n", cl.getUnloadedClassCount());

    }

    private NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));
    private NumberFormat fmtD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));
    private final int Kb = 1024;
    protected String printSizeInKb(double size) {
        return fmtI.format((long) (size / 1024)) + " kbytes";
    }
    //转为持续时间
    protected String toDuration(double uptime) {
        uptime /= 1000;
        if (uptime < 60) {
            return fmtD.format(uptime) + " seconds";
        }
        uptime /= 60;
        if (uptime < 60) {
            long minutes = (long) uptime;
            String s = fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            return s;
        }
        uptime /= 60;
        if (uptime < 24) {
            long hours = (long) uptime;
            long minutes = (long) ((uptime - hours) * 60);
            String s = fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
            if (minutes != 0) {
                s += " " + fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            }
            return s;
        }
        uptime /= 24;
        long days = (long) uptime;
        long hours = (long) ((uptime - days) * 24);
        String s = fmtI.format(days) + (days > 1 ? " days" : " day");
        if (hours != 0) {
            s += " " + fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
        }
        return s;
    }


}