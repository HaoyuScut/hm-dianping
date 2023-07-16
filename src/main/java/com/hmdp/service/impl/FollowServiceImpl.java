package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {


    /**
     * 关注或取关
     * @param followId
     * @param isFollow
     * @return
     */
    @Override
    public Result follow(Long followId, Boolean isFollow) {
        //1.获取当前登录的用户
        UserDTO user = UserHolder.getUser();
        if(user == null) {
            //用户未登录，无需查询是否点赞
            return Result.fail("用户未登录");
        }
        Long userId = UserHolder.getUser().getId();
        //2.判断是关注还是取关
        if (isFollow) {
            //3.关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followId);
            save(follow);

        } else {
            //4.取关，删除数据 delete from tb_follow where userId = ? and followId = ?
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId,followId);
            remove(queryWrapper);
        }

        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //1.查询是否关注
        //1.获取当前登录的用户
        UserDTO user = UserHolder.getUser();
        if(user == null) {
            //用户未登录，无需查询是否点赞
            return Result.fail("用户未登录");
        }
        //2.查询是否关注 select count(*) from tb_follow where
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId,followUserId);
        int count = count(queryWrapper);
        //3.判断
        return Result.ok(count > 0);

    }
}
