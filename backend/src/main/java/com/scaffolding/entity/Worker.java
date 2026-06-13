package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker")
public class Worker extends BaseEntity {

    private String workerName;

    private String idCard;

    private String phone;

    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthDate;

    private String address;

    private String emergencyContact;

    private String emergencyPhone;

    private Long enterpriseId;

    private Long projectId;

    private String workerStatus;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate hireDate;

    private BigDecimal depositAmount;

    private Integer depositPaid;

    private String remark;
}
