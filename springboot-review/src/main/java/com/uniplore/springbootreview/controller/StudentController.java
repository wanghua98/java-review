package com.uniplore.springbootreview.controller;

import com.uniplore.springbootreview.Result.Result;
import com.uniplore.springbootreview.entity.Student;
import com.uniplore.springbootreview.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "student", description = "student controller")
@RequiredArgsConstructor
@RestController("/api")
public class StudentController {

    private final StudentService studentServiceImpl;


    /*
     * 获取全部学生信息
     * @return List<Student>
     */
    @Operation(summary = "获取学生信息", description = "获取学生信息")
    @GetMapping("/student")
    public Result<List<Student>> getStudent() {
        return studentServiceImpl.getStudent();
    }

    /**
     * 获取单个学生信息
     * @return Student
     */
    @Operation(summary = "获取学生信息", description = "获取学生信息")
    @GetMapping("/student/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        return studentServiceImpl.getStudentById(id);
    }


    /*
     * 插入学生信息
     */
    @Operation(summary = "插入学生信息", description = "插入学生信息")
    @PostMapping("/student")
    public Result<String> insertStudent(Student student) {
        return Result.success(studentServiceImpl.save(student) ? "success" : "fail");
    }

    /*
     * 删除学生信息
     */
    @Operation(summary = "删除学生信息", description = "删除学生信息")
    @DeleteMapping("/student")
    public Result<String> deleteStudent(Student student) {
        return Result.success(studentServiceImpl.removeById(student) ? "success" : "fail");
    }

    /*
     * 更新学生信息
     */
    @Operation(summary = "更新学生信息", description = "更新学生信息")
    @PutMapping("/student")
    public Result<String> updateStudent(Student student) {
        return Result.success(studentServiceImpl.updateById(student) ? "success" : "fail");
    }


}
