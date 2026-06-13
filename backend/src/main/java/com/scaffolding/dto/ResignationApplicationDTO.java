package com.scaffolding.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ResignationApplicationDTO {

    private Long workerId;

    private String resignationType;

    private Long targetProjectId;

    private LocalDate lastWorkDate;

    private String responsibilityNote;

    private String photoIds;

    private String checkRemark;

    private String financeRemark;

    private List<EquipmentReturnDTO> equipmentReturns;
}
