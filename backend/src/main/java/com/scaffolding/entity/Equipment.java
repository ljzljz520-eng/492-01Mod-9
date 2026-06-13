package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("equipment")
public class Equipment extends BaseEntity {

    private String equipmentType;

    private String equipmentName;

    private String equipmentCode;

    private String specification;

    private String unit;

    private BigDecimal depositAmount;

    private Integer totalQuantity;

    private Integer availableQuantity;

    private String remark;
}
