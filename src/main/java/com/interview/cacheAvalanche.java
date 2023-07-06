package com.interview;

/**
 * @ClassName: {NAME}
 * @Auther: why
 * @Date: 2023/07/06 22 41
 * @Version: v1.0
 * 缓存雪崩
 */
public class cacheAvalanche {

    /**
     * 缓存雪崩是指在同一时段大量的缓存key同时失效或Redis服务宕机
     * 导致大量请求到达数据库，带来巨大压力
     *
     * 解决方案：
     * 1.给不同的Key的TTL添加随机值
     * 2.利用Redis集群提高服务的可用性 - 哨兵机制
     * 3.给缓存业务添加降级限流策略
     * 4.给业务添加多级缓存 - 在多个层级进行缓存 nginx JVM redis 数据库
     */
}
