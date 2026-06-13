package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker_equipment")
public class WorkerEquipment extends BaseEntity {

    private Long workerId;

    private Long equipmentId;

    private String equipmentType;

    private String equipmentName;

    private Integer quantity;

    private BigDecimal depositAmount;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate receiveDate;

    private Long receiveUserId;

    private String receiveUserName;

    private String equipmentStatus;

    private String remark;
}
