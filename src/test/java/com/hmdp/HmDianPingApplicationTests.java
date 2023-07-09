package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    void testSaveShop() throws InterruptedException {
//        shopService.saveShop2Redis(1L, 10L);
        Shop shop = shopService.getById(2L);

        cacheClient.setWithLogicExpire(RedisConstants.CACHE_SHOP_KEY + 2L, shop, 10L, TimeUnit.SECONDS);

    }

}
