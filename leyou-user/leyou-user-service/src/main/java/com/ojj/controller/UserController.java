package com.ojj.controller;

import com.ojj.service.UserService;
import com.ojj.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 验证信息是否可用
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("data")String data, @PathVariable("type")Integer type){

        Boolean result = this.userService.checkData(data,type);

        if (result == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);

    }

    /**
     * 生成短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> buildVerificationCode(@RequestParam("phone") String phone){

        this.userService.buildverificationCode(phone);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 注册用户
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> registerUser(@Valid User user, @RequestParam("code")String code){

        this.userService.regsiterUser(user,code);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     *验证用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUserByNameandPassword(@RequestParam("username")String username, @RequestParam("password")String password){

        User user = this.userService.queryUser(username,password);

        if (user == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }


}
