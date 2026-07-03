package com.uniplore.springbootreview.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.springbootreview.Result.Result;
import com.uniplore.springbootreview.entity.Student;
import com.uniplore.springbootreview.mapper.StudentMapper;
import com.uniplore.springbootreview.service.StudentService;
import com.uniplore.springbootreview.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
        implements StudentService {

    private final CacheUtil cacheUtil;

    @Override
    public Result<List<Student>> getStudent() {
        return Result.success(list());
    }

    @Override
    public Result<Student> getStudentById(Long id) {

        Student student = cacheUtil.getCacheWithMutex("student", id, this::getById, Student.class, 10L, TimeUnit.MINUTES);
        if (student == null) {
            return Result.error(404, "Student not found", student);
        }
        return Result.success(student);
    }
}
