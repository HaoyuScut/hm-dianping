package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.FOLLOW_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    /**
     * 关注或取关
     *
     * @param followUserId
     * @param isFollow
     * @return
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //1.获取当前登录的用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            //用户未登录，无需查询是否点赞
            return Result.fail("用户未登录");
        }
        Long userId = UserHolder.getUser().getId();
        String key = FOLLOW_KEY + userId;
        //2.判断是关注还是取关
        if (isFollow) {
            //3.关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if(isSuccess) {
                //把关注用户的id放入redis的集合中 sadd userId followUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }

        } else {
            //4.取关，删除数据 delete from tb_follow where userId = ? and followId = ?
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, followUserId);
            boolean isSuccess = remove(queryWrapper);
            if(isSuccess) {
                //把关注用户的id从redis中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }

        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //1.查询是否关注
        //1.获取当前登录的用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            //用户未登录，无需查询是否点赞
            return Result.fail("用户未登录");
        }
        //2.查询是否关注 select count(*) from tb_follow where
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, followUserId);
        int count = count(queryWrapper);
        //3.判断
        return Result.ok(count > 0);

    }

    @Override
    public Result followCommons(Long id) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key = FOLLOW_KEY + userId;
        //2.求交集
        String key2 = FOLLOW_KEY + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if(intersect == null || intersect.isEmpty()) {
            //无交集
            return Result.ok(Collections.emptyList());
        }

        //3.解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        //4.查询用户
        List<User> users = userService.listByIds(ids);
        List<UserDTO> userDTOS = users.stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        return Result.ok(userDTOS);
    }
}
