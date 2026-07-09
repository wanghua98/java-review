package com.uniplore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uniplore.pojo.StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生Mapper
 *
 * @author 杨锋
 */
@Mapper
public interface StudentMapper extends BaseMapper<StudentDO> {
}
