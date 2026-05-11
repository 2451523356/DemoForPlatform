package com.kaoyan.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaoyan.platform.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
