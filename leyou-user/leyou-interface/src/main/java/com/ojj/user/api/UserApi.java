package com.ojj.user.api;

import com.ojj.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {

    /**
     *验证用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public User queryUserByNameandPassword(@RequestParam("username")String username, @RequestParam("password")String password);
}
