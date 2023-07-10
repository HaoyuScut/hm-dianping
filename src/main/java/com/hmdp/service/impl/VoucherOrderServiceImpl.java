package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 * Redisson分布式锁原理:
 * ·可重入:利用hash结构记录线程id和重入次数
 * ·可重试:利用信号量和PubSub功能实现等待、唤醒，获取
 * 锁失败的重试机制
 * 超时续约:利用watchDog，每隔一段时间( releaseTime/ 3)，重置超时时间
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Result seckillVoucher(Long voucherId) {
        //1.查询
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        //2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            //尚未开始
            return Result.fail("秒杀尚未开始！");
        }
        //3.判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            //尚未开始
            return Result.fail("秒杀已经结束！");
        }
        //4.判断库存是否充足
        if (voucher.getStock() < 1) {
            // 库存不足
            return Result.fail("库存不足！");

        }

        Long userId = UserHolder.getUser().getId();

        //创建锁对象
//        SimpleRedisLock lock = new SimpleRedisLock(stringRedisTemplate, "order:" + userId);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //获取锁
//        boolean isLock = lock.tryLock(1200);
        //尝试获取锁，参数分别是:获取锁的最大等待时间（期间会重试)，锁自动释放时间，时间单位
        boolean isLock = lock.tryLock();
        //判断是否获取锁成功
        if(!isLock) {
            //获取锁失败，返回错误信息或重试
            return Result.fail("不允许重复下单！");
        }
        try {
            //获取代理对象（事务）
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }

        /*synchronized(userId.toString().intern()) {
            //获取代理对象（事务）
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }*/
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        //5.一人一单
        Long userId = UserHolder.getUser().getId();


        //5.1 查询订单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        //5.2 判断是否存在
        if (count > 0) {
            //用户已经购买过
            return Result.fail("用户已经购买过一次！");
        }

        //6.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1") //set stock = stock - 1
                .eq("voucher_id", voucherId)
//                .eq("stock", voucher.getStock()) //CAS
                .gt("stock", 0)
                .update();
        if (!success) {
            //扣减失败
            return Result.fail("库存不足！");
        }


        //6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //6.1 订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //6.2 用户id
        voucherOrder.setUserId(userId);
        //6.3 代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);

        return Result.ok(orderId);


    }
}
