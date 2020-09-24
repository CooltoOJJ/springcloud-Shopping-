package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 用户添加购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){

        this.cartService.addCart(cart);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询用户购物车
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCarts(){

        List<Cart> cartsList = this.cartService.queryCarts();

        if (CollectionUtils.isEmpty(cartsList)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(cartsList);
    }

    /**
     * 更新购物车数量
     * @param cart
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCartNum(@RequestBody Cart cart){
        this.cartService.updateCartNum(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCatrBySkuId(@PathVariable("skuId") String skuId){
        this.cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
