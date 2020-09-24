package com.leyou.rabbmq;

import com.leyou.sevice.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class Listener {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    /**
     * 监听 item.insert,item.update 路由的信息
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.create.web.queue",durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU.ITEM.EXCHANGE",
                    type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = {"item.insert","item.update"}
    ))
    public void listenInsert(Long id){

        if (id == null){
            return;
        }
        System.out.println("创建新的静态页面，新增或覆盖");
        //创建新的静态页面，新增或覆盖
        this.goodsHtmlService.creatHtml(id);
    }

    /**
     * 删除缓存页面
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.delete.web.queue",durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU.ITEM.EXCHANGE",
                    type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = {"item.delete"}

    ))
    public void listenDelete(Long id){

        if (id == null){
            return;
        }
        //删除缓存页面
        //1.缓存页面文件
        File file = new File("D:\\JAVA\\leyou\\tool\\nginx-1.14.0\\html\\item\\", id +".html");
        //删除
        file.deleteOnExit();

    }

}
