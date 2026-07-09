package com.uniplore.pojo;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 学生视图对象
 *
 * @author 杨锋
 * @date 2026/07/06
 */
@Data
public class StudentVO {
    /**
     * id
     */
    private Long id;
    /**
     * 学号
     */
    private String stuId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 生日
     */
    private LocalDate birthday;
    /**
     * 更新时间
     */
    private LocalTime updateTime;

}
