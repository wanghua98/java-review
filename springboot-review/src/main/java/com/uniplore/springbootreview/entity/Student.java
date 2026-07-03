package com.uniplore.springbootreview.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("student")
public class Student {
    private Integer id;
    private String stuId;
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private LocalTime updateTime;
    private LocalTime createTime;
}