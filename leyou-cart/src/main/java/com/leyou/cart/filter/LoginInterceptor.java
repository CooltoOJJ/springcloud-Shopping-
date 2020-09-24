package com.leyou.cart.filter;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 为每个线程提供一个局部变量
     */
    private static final ThreadLocal<UserInfo> USERINFO = new ThreadLocal<>();

    /**
     * 前置方法，解析用户信息
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取Cookie中的token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        //获取token
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

        if (userInfo == null){
            return false;
        }

        USERINFO.set(userInfo);

        return true;
    }

    public static UserInfo getUserInfo(){
       return USERINFO.get();
    }

    /**
     * 后置方法，清空线程局部变量
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        USERINFO.remove();
    }
}
