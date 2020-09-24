package com.ojj.upload.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.ojj.upload.service.UploadFileService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadFileSerivceImpl implements UploadFileService {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    private static final List<String> CONTENT_TYPE = Arrays.asList("image/jpeg","image/gif","image/png");
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileSerivceImpl.class);

    @Override
    public String uploadImag(MultipartFile file) {

        //验证图片类型
        String originalFilename = file.getOriginalFilename();
        System.out.println("验证图片类型"+originalFilename);
        try {

            String contentType = file.getContentType();
            if (!CONTENT_TYPE.contains(contentType)){
                LOGGER.info("文件格式错误：{}",originalFilename);
                return null;
            }
            //验证图片内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage==null){
                LOGGER.info("文件内容不合法：{}",originalFilename);
                return null;
            }

            //保存到服务器
          //  file.transferTo(new File("D:\\JAVA\\leyou\\image\\"+originalFilename));
            String s = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath path = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), s, null);

            // return "http://image.leyou.com/"+originalFilename;
            return "http://image.leyou.com/"+path.getFullPath();

        } catch (IOException e) {
            LOGGER.info("服务器内部错误：{}",originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
