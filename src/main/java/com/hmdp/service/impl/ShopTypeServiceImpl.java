package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KET;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        //1.查询redis中有无数据
        String key = CACHE_SHOPTYPE_KET;
        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);
        //2.如果有返回数据
        if (StrUtil.isNotBlank(shopTypeJson)) {
            List<ShopType> shopTypes = JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(shopTypes);
        }
        //3.如果没有，查询数据库中数据
        List<ShopType> shopTypes = query().orderByAsc("sort").list();

        //4.如果没有，返回错误值
        if(CollectionUtil.isEmpty(shopTypes)) {
            return Result.fail("店铺列表不存在!");
        }

        //4.如果有，写入redis缓存
        String jsonStr = JSONUtil.toJsonStr(shopTypes);
        stringRedisTemplate.opsForValue().set(key, jsonStr);

        //6.返回值
        return Result.ok(shopTypes);
    }
}
