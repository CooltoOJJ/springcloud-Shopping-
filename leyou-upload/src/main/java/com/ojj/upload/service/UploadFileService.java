package com.ojj.upload.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

    String uploadImag(MultipartFile file);


}
