package com.interview;

/**
 * @ClassName: {NAME}
 * @Auther: why
 * @Date: 2023/07/16 13 50
 * @Version: v1.0
 */
public class Dianzan {
    /**
     * 完善点赞功能
     * 需求:
     * ·同一个用户只能点赞一次，再次点击则取消点赞
     * ·如果当前用户已经点赞，则点赞按钮高亮显示（前端已实现，判断字段Blog类的isLike属性)
     * 实现步骤:
     * 给Blog类中添加一个isLike字段，标示是否被当前用户点赞
     * 修改点赞功能，利用Redis的set集合判断是否点赞过，未点赞过则点赞数+1，已点赞过则点赞数-1
     * ③修改根据id查询Blog的业务，判断当前登录用户是否点赞过，赋值给isLike字段
     * ④修改分页查询Blog业务，判断当前登录用户是否点赞过，赋值给isLike字段
     */
}
