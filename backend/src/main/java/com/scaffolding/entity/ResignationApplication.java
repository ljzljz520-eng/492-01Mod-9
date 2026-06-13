package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resignation_application")
public class ResignationApplication extends BaseEntity {

    private String applicationNo;

    private Long workerId;

    private String workerName;

    private Long currentProjectId;

    private String currentProjectName;

    private Long enterpriseId;

    private String resignationType;

    private Long targetProjectId;

    private String targetProjectName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate lastWorkDate;

    private BigDecimal totalDeposit;

    private BigDecimal deductAmount;

    private BigDecimal refundAmount;

    private BigDecimal actualRefundAmount;

    private String applicationStatus;

    private Long teamLeaderId;

    private String teamLeaderName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkTime;

    private String checkRemark;

    private Long financeUserId;

    private String financeUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime financeAuditTime;

    private String financeRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;

    private String refundVoucher;

    private String photoIds;

    private String responsibilityNote;
}
