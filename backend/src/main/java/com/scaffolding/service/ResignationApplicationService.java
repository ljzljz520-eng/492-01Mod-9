package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.dto.ResignationApplicationDTO;
import com.scaffolding.entity.ResignationApplication;
import com.scaffolding.entity.EquipmentReturn;

import java.math.BigDecimal;
import java.util.List;

public interface ResignationApplicationService extends IService<ResignationApplication> {

    Page<ResignationApplication> pageQuery(Long current, Long size, String workerName, Long enterpriseId, Long projectId, String applicationStatus, String resignationType);

    ResignationApplication createApplication(ResignationApplicationDTO dto, Long userId, String userName);

    ResignationApplication getDetail(Long id);

    List<EquipmentReturn> getEquipmentReturns(Long resignationId);

    void teamLeaderCheck(Long id, ResignationApplicationDTO dto, Long userId, String userName);

    void financeAudit(Long id, ResignationApplicationDTO dto, Long userId, String userName);

    void confirmRefund(Long id, BigDecimal actualRefundAmount, String refundVoucher, Long userId, String userName);

    void rejectApplication(Long id, String rejectReason, Long userId, String userName);
}
