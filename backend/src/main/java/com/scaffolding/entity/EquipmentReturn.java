package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment_return")
public class EquipmentReturn extends BaseEntity {

    private Long resignationId;

    private Long workerEquipmentId;

    private Long equipmentId;

    private String equipmentType;

    private String equipmentName;

    private Integer quantity;

    private BigDecimal depositAmount;

    private String returnStatus;

    private String returnCondition;

    private BigDecimal deductAmount;

    private String photoIds;

    private String responsibilityNote;

    private String remark;
}
