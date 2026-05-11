package com.kaoyan.platform.service;

import com.kaoyan.platform.dto.LoginDTO;
import com.kaoyan.platform.dto.RegisterDTO;
import com.kaoyan.platform.vo.LoginVO;

public interface AuthService {
    
    LoginVO login(LoginDTO loginDTO);
    
    void register(RegisterDTO registerDTO);
    
    void logout();
}
