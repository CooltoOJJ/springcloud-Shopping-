package com.ojj.service;

import com.leyou.common.utils.NumberUtils;
import com.ojj.mapper.UserMapper;
import com.ojj.user.pojo.User;
import com.ojj.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "user:verify:";

    /**
     * 检验用户是否可用
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {

        User user = new User();

        if (type == 1){
            user.setUsername(data);
        }else if(type == 2){
            user.setPhone(data);
        }else {
            return null;
        }

        return this.userMapper.selectCount(user) == 0;
    }

    /**
     * 生成验证码，发送短信，code存放redis
     * @param phone
     */
    public void buildverificationCode(String phone) {

        //验证输入电话号码
        if(StringUtils.isBlank(phone)){
            return;
        }
        //生成验证码
        String code = NumberUtils.generateCode(6);

        //发送短信，RabbitMQ
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE","verifycode.sms",msg);

        //存放验证码到redis,设置过期时间30秒
        this.redisTemplate.opsForValue().set(KEY_PREFIX + phone,code,5, TimeUnit.MINUTES);


    }

    /**
     * 注册用户
     * @param user
     * @param code 验证码
     */
    public void regsiterUser(User user, String code) {

        //验证码比对
        String redisCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(redisCode,code)){
            return;
        }

        //生成盐，
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //密码加盐
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));


        //新增用户信息
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insertSelective(user);

        this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
    }


    public User queryUser(String username, String password) {
        //根据名字查询用户
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        if (user == null){
            return null;
        }

        //比较密码
        //得到user盐，结输入密码加盐后比对
        password = CodecUtils.md5Hex(password,user.getSalt());

        if (StringUtils.equals(user.getPassword(),password)){

            return user;
        }

        return null;
    }
}
