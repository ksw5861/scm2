package com.yedam.scm.login.service;

import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;

public interface AppLoginService {

    LoginRes loginForApp(LoginDTO login);

}
