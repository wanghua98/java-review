package com.uniplore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工类
 *
 * @author dao
 */
@Data
@AllArgsConstructor
public class Employee {
    private String name;
    private int salary;
    private int year;
    private int month;
    private int day;

    public LocalDate getHireDate() {
        return LocalDate.of(this.year, this.month, this.day);
    }
}

