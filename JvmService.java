package wang.wangby.testcase.service;

import wang.wangby.testcase.model.JvmInfo;
import wang.wangby.testcase.model.vo.Usage;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

public class JvmService {
    public static final String OLD="PS Old Gen";
    public static final String METASPACE="Metaspace";
    public static final String SURVIVOR="PS Survivor Space";
    public static final String EDEN="PS Eden Space";


    public JvmInfo getMemroy(){
        JvmInfo jvmInfo=new JvmInfo();
        List<MemoryPoolMXBean> memoryPoolMXBeans= ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean memory:memoryPoolMXBeans){
            if(EDEN.equalsIgnoreCase(memory.getName())){
                jvmInfo.setEden(toUsage(memory.getUsage()));
            }else if(OLD.equalsIgnoreCase(memory.getName())){
                jvmInfo.setOld(toUsage(memory.getUsage()));
            }else if(SURVIVOR.equalsIgnoreCase(memory.getName())){
                jvmInfo.setSurvivor(toUsage(memory.getUsage()));
            }else if(METASPACE.equalsIgnoreCase(memory.getName())){
                jvmInfo.setMetaspace(toUsage(memory.getUsage()));
            }
        }
        return jvmInfo;
    }

    private Usage toUsage(MemoryUsage memoryUsage) {
        Usage usage=new Usage();
        usage.setCommitted(memoryUsage.getCommitted());
        usage.setMax(memoryUsage.getMax());
        usage.setUsed(memoryUsage.getUsed());
        usage.setInit(memoryUsage.getInit());
        return usage;
    }
}
