package com.scaffolding.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EquipmentReturnDTO {

    private Long id;

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
