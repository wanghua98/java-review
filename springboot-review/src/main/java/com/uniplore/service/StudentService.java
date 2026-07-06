package com.uniplore.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.StudentVO;
import com.uniplore.result.Result;
import com.uniplore.pojo.StudentDO;

import java.util.List;

/**
 * StudentService接口
 *
 * @author 杨锋
 * @date 2026/0706
 */
public interface StudentService extends IService<StudentDO> {

    /**
     * 获取所有学生信息
     *
     * @return 所有学生信息
     */
    Result<List<StudentDO>> getStudent();

    /**
     * 根据id获取学生信息
     *
     * @param id 学生id
     * @return 学生信息
     */
    Result<StudentVO> getStudentById(Long id);

    /**
     * 根据学号删除学生信息
     *
     * @param id 学生学号
     * @return 删除结果
     */
    Result<String> removeByStudentNo(String id);
}
