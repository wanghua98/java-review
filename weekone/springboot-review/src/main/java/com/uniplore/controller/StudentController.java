package com.uniplore.controller;

import cn.hutool.core.bean.BeanUtil;
import com.uniplore.pojo.StudentDTO;
import com.uniplore.pojo.StudentVO;
import com.uniplore.result.Result;
import com.uniplore.pojo.StudentDO;
import com.uniplore.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 学生控制器
 *
 * @author 杨锋
 * @date 2026/7/6
 */
@Tag(name = "student", description = "student controller")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentServiceImpl;


    /**
     * 获取全部学生信息
     *
     * @return List<Student>
     */
    @Operation(summary = "获取学生信息", description = "获取全部学生信息")
    @GetMapping("/student")
    public Result<List<StudentDO>> getStudent() {
        return studentServiceImpl.getStudent();
    }

    /**
     * 根据id获取单个学生信息
     *
     * @return StudentVO 学生信息
     */
    @Operation(summary = "获取学生信息", description = "获取学生信息")
    @GetMapping("/student/{id}")
    public Result<StudentVO> getStudentById(@PathVariable Long id) {
        return studentServiceImpl.getStudentById(id);
    }

    /**
     * 插入学生信息
     *
     * @param student 学生信息
     * @return String
     */
    @Operation(summary = "插入学生信息", description = "插入学生信息")
    @PostMapping("/student")
    public Result<String> insertStudent(@RequestBody StudentDTO student) {
        return Result.success(studentServiceImpl.save(BeanUtil.copyProperties(student, StudentDO.class)) ? "success" : "fail");
    }

    /**
     * 根据id删除学生信息
     *
     * @param id 学生ID
     * @return String
     */
    @Operation(summary = "删除学生信息", description = "删除学生信息")
    @DeleteMapping("/student/{id}")
    public Result<String> deleteStudent(@PathVariable Long id) {
        return Result.success(studentServiceImpl.removeById(id) ? "success" : "fail");

    }

    /**
     * 根据学号删除学生信息
     *
     * @param id 学生学号
     * @return String
     */
    @Operation(summary = "删除学生信息", description = "根据学号删除学生信息")
    @DeleteMapping("/student/by-student-no")
    public Result<String> deleteStudentById(@RequestParam String id) {
        return studentServiceImpl.removeByStudentNo(id);
    }

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     * @return String
     */
    @Operation(summary = "更新学生信息", description = "更新学生信息")
    @PutMapping("/student")
    public Result<String> updateStudent(@RequestBody StudentDTO student) {
        return Result.success(studentServiceImpl.updateById(BeanUtil.copyProperties(student, StudentDO.class)) ? "success" : "fail");
    }


}
