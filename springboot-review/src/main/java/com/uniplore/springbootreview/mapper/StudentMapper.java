package com.uniplore.springbootreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uniplore.springbootreview.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
