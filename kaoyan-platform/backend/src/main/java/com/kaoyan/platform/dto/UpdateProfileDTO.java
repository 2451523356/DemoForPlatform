package com.kaoyan.platform.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String avatar;
    
    private String bio;
}

