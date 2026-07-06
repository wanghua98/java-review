package com.uniplore.service.impl;

import cn.hutool.Hutool;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.uniplore.pojo.StudentVO;
import com.uniplore.result.Result;
import com.uniplore.pojo.StudentDO;
import com.uniplore.mapper.StudentMapper;
import com.uniplore.service.StudentService;
import com.uniplore.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * StudentService实现类
 *
 * @author 杨锋
 * @date 2023/04/05
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, StudentDO>
        implements StudentService {

    /**
     * 缓存工具类
     */
    private final CacheUtil cacheUtil;

    /**
     * 获取所有学生信息
     *
     * @return 学生信息
     */
    @Override
    public Result<List<StudentDO>> getStudent() {
        return Result.success(list());
    }

    /**
     * 根据id获取学生信息
     *
     * @param id 学生id
     * @return 学生信息
     */
    @Override
    public Result<StudentVO> getStudentById(Long id) {
        // 从缓存中获取学生信息，如果缓存中不存在则从数据库中获取并放入缓存
        StudentDO studentDO = cacheUtil.getCacheWithMutex("student", id, this::getById, StudentDO.class, 10L, TimeUnit.MINUTES);
        // 如果学生信息不存在，则返回错误信息
        if (studentDO == null) {
            return Result.error(404, "Student not found", null);
        }
        //使用hutool进行复制
        StudentVO studentVO = new StudentVO();
        BeanUtil.copyProperties(studentDO, studentVO);

        return Result.success(studentVO);
    }

    /**
     * 根据学号删除学生信息
     *
     * @param id 学生学号
     * @return 删除结果
     */
    @Override
    public Result<String> removeByStudentNo(String id) {
        //根据学号查询是否存在
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<StudentDO>();
        queryWrapper.eq(StudentDO::getStuId, id);
        List<StudentDO> studentDO = list(queryWrapper);
        if (studentDO == null || studentDO.isEmpty()) {
            return Result.error(404, "Student not found", null);
        }
        List<Long> ids = studentDO.stream().map(StudentDO::getId).toList();
        boolean result = removeBatchByIds(ids);
        return Result.success(result ? "Student deleted successfully" : "Student deletion failed");
    }
}
