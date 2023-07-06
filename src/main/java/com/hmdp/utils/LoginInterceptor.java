package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: {NAME}
 * @Auther: why
 * @Date: 2023/07/05 16 18
 * @Version: v1.0
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判断是否需要去拦截(ThreadLocal中是否有该用户)
        UserDTO userDTO = UserHolder.getUser();
        if(userDTO == null) {
            //没有，需要拦截
            response.setStatus(401);
            //拦截
            return false;
        }
        //有用户放行
        return true;
    }


}
