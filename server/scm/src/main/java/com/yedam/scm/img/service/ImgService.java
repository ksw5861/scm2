  package com.yedam.scm.img.service;

import java.io.IOException;

import org.springframework.core.io.Resource;

public interface ImgService {

  Resource getDefectImage(String inLogId) throws IOException;
}
