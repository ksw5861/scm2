package com.yedam.scm.login.service;

public interface RecaptchaService {

  boolean verifyToken(String token);

}
