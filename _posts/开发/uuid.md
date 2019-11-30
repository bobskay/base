---
title: UUID生成
date: 2019-01-17 10:46:17
categories: [code]
tags: [UUID,分布式]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

每毫秒最多生成4096个，最多允许机器数1024

起始时间如果设置为2019-1-1，最多可以用到2086-09-06

```java
package wang.wangby.utils;

import java.nio.charset.Charset;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.wangby.annotation.persistence.Id;

/**
 * 主键生成器 0正负标识, 1-40当前毫秒值,41-52同一毫秒可产生的序列号, 53-63机器编号,
 * 010001101011110010001111000100111011 000000001110 0000000000
 */
public class IdWorker {
    private static IdWorker INSTANCE = new IdWorker(0);
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final int machineBits = 10;// 机器位数
    public static final int seqBits = 12;// 序列号位数
    public static final int timeShift = machineBits + seqBits;// 时间往左移动位数
    public static final long maxSeq = -1L ^ (-1L << seqBits);// 最大允许序列号(4095)
    public static final long maxMachine = -1L ^ (-1L << machineBits);// 最大允许机器号(1023),最小0
    // 为了防止超过long的最大值,时间戳生成后减去这个值
    private static final long begin = new DateTime("2019-01-01").getTime();
    private int machineCode;
    private long lastTime;
    private long sequence = 1;// 因为0无法向左做位移操作,序列号从1开始

    /**
     * @param machineCode 机器编号
     */
    public IdWorker(int machineCode) {
        this.machineCode = machineCode;
        if (machineCode > maxMachine) {
            throw new RuntimeException("机器编号不能大于" + maxMachine);
        }
        if ((currentTimeMillis() - begin) << timeShift < 0) {
            long max = (Long.MAX_VALUE >> timeShift) + begin;// 2086-09-06
            throw new RuntimeException("当前时间超过" + new DateTime(max) + ",生成器已无法使用");
        }
        if (machineCode != 0) {
            log.info("主键生成器创建完毕,当前机器号:" + machineCode);
        }
    }

    public synchronized long nextId() {
        long time = currentTimeMillis() - begin;
        if (time != lastTime) {
            lastTime = time;
            sequence = 1;
        } else {
            sequence++;
        }
        if (sequence > maxSeq) {
            throw new RuntimeException("当前不支持同一毫秒内生成主键超过:" + maxSeq);
        }
        //时间向左移动22位+机器向左移动12位+机器的10位
        return (time << timeShift) | (sequence << machineBits) | machineCode;
    }

    // 获取当前时间单独弄一个方法,方便测试时覆盖
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    // 通过生成的id获取id生成时间,去掉后面的22位
    public static long getTimestamp(long id) {
        return begin + (id >>> timeShift);
    }

    public static long createByTime(long time) {
        time = time - begin;
        return (time << timeShift) | (0 << machineBits) | 0;
    }

    // 通过生成的id获得机器编号,取最后10位
    public static int getMachineNo(long id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < machineBits; i++) {
            sb.append(id & 1);
            id = id >>> 1;
        }
        return Integer.parseInt(sb.reverse().toString(), 2);
    }

    public static int getMachineNo(String id) {
        return getMachineNo(toLong(id));
    }

    public static long nextLong() {
        return INSTANCE.nextId();
    }

    public static String nextString() {
        long l = INSTANCE.nextId();
        return Long.toString(l,Character.MAX_RADIX);
    }

    public static Long toLong(String uuid){
       return Long.parseLong(uuid, Character.MAX_RADIX);
    }
}
```



<!--more-->