package com.uniplore.springbootreview.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.springbootreview.Result.Result;
import com.uniplore.springbootreview.entity.Student;

import java.util.List;

public interface StudentService extends IService<Student> {

    Result<List<Student>> getStudent();

    Result<Student> getStudentById(Long id);
}
