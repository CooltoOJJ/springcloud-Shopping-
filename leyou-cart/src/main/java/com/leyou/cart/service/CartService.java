package com.leyou.cart.service;

import com.leyou.cart.client.GoodClient;
import com.leyou.cart.filter.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodClient goodClient;

    private static final String KEY_USER = "user:cart:";

    /**
     * 增加购物车
     * @param cart
     */
    public void addCart(Cart cart) {

        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查看redis中该用户中的购物车信息
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_USER + userInfo.getId());

        //skuId
        String key = cart.getSkuId().toString();
        Integer num = cart.getNum();
        //是否有相同的sku
        if(hashOps.hasKey(key)){
            //如有相同,获取该key的值
            String cartJson = hashOps.get(key).toString();
            Cart cartValue = JsonUtils.parse(cartJson, Cart.class);//json字符串转换成对象
            cartValue.setNum(cartValue.getNum() + num);//设置数量
            hashOps.put(key,JsonUtils.serialize(cartValue));//重新覆盖sku
        }else {
            //没有相同
            cart.setUserId(userInfo.getId());
            //根据skuId查询商品信息
            Sku sku = this.goodClient.querySkuById(cart.getSkuId());
            if (sku !=null){
                cart.setSkuId(sku.getId());
                //cart.setNum(cart.getNum());
                cart.setImage(StringUtils.isNotBlank(sku.getImages()) ? sku.getImages().split(",")[0] : "");
                cart.setOwnSpec(sku.getOwnSpec());
                cart.setTitle(sku.getTitle());
                cart.setPrice(sku.getPrice());
            }
            //新增购物车商品到redis
            hashOps.put(key,JsonUtils.serialize(cart));
        }
    }

    /**
     * 查询用户购物车
     * @return
     */
    public List<Cart> queryCarts() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        if (!this.redisTemplate.hasKey((KEY_USER + userInfo.getId()))){
            return null;
        }

        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_USER + userInfo.getId());

        List<Object> carts = hashOperations.values();

        if (CollectionUtils.isEmpty(carts)){
            return null;
        }

        return carts.stream().map(cart -> JsonUtils.parse(cart.toString(),Cart.class)).collect(Collectors.toList());

    }

    /**
     * 更新数量
     * @param cart
     */
    public void updateCartNum(Cart cart) {

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        if (!this.redisTemplate.hasKey((KEY_USER + userInfo.getId()))){
            return ;
        }

        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_USER + userInfo.getId());

        String cartJson = hashOperations.get(cart.getSkuId().toString()).toString();

        Cart catrValue = JsonUtils.parse(cartJson, Cart.class);

        catrValue.setNum(num);

        hashOperations.put(cart.getSkuId().toString(),JsonUtils.serialize(catrValue));

    }

    public void deleteCart(String skuId) {

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        if (!this.redisTemplate.hasKey((KEY_USER + userInfo.getId()))){
            return ;
        }

        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_USER + userInfo.getId());


        hashOperations.delete(skuId);
    }
}
