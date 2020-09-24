package com.leyou.sevice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private GoodsService goodsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    /**
     * 生成html静态页面
     * @param id
     */
    public void creatHtml(Long id){

        //获取页面数据
        Map<String, Object> loadData = goodsService.loadData(id);

        PrintWriter printWriter = null;
        try {
            //创建页面静态化上下文
            Context context = new Context();
            //把页面数据放入上下文
            context.setVariables(loadData);

            //创建输出流
            printWriter = new PrintWriter(new File("D:\\JAVA\\leyou\\tool\\nginx-1.14.0\\html\\item\\" + id + ".html"));

            //执行页面静态化
            templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            LOGGER.error("页面静态化出错：{}，"+ e, id);
        }finally {
            if (printWriter!=null){
                printWriter.close();
            }

        }

    }

}
