package com.uniplore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.uniplore.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author yf
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
