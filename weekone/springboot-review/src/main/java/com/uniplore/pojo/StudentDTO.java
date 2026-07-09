package com.uniplore.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 学生数据传输对象
 *
 * @author 杨锋
 * @date 2026/07/06
 */
@Data
public class StudentDTO {
    /**
     * ID
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthday;
}
