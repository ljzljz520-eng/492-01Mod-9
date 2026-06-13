package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.dto.EquipmentReturnDTO;
import com.scaffolding.dto.ResignationApplicationDTO;
import com.scaffolding.entity.*;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.ResignationApplicationMapper;
import com.scaffolding.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ResignationApplicationServiceImpl extends ServiceImpl<ResignationApplicationMapper, ResignationApplication> implements ResignationApplicationService {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private WorkerEquipmentService workerEquipmentService;

    @Autowired
    private EquipmentReturnService equipmentReturnService;

    @Autowired
    private EquipmentService equipmentService;

    @Override
    public Page<ResignationApplication> pageQuery(Long current, Long size, String workerName, Long enterpriseId, Long projectId, String applicationStatus, String resignationType) {
        Page<ResignationApplication> page = new Page<>(current, size);
        LambdaQueryWrapper<ResignationApplication> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(workerName)) {
            wrapper.like(ResignationApplication::getWorkerName, workerName);
        }
        if (enterpriseId != null) {
            wrapper.eq(ResignationApplication::getEnterpriseId, enterpriseId);
        }
        if (projectId != null) {
            wrapper.eq(ResignationApplication::getCurrentProjectId, projectId);
        }
        if (StringUtils.hasText(applicationStatus)) {
            wrapper.eq(ResignationApplication::getApplicationStatus, applicationStatus);
        }
        if (StringUtils.hasText(resignationType)) {
            wrapper.eq(ResignationApplication::getResignationType, resignationType);
        }

        wrapper.orderByDesc(ResignationApplication::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResignationApplication createApplication(ResignationApplicationDTO dto, Long userId, String userName) {
        Worker worker = workerService.getById(dto.getWorkerId());
        if (worker == null) {
            throw new BusinessException("工人不存在");
        }
        if (!"on_job".equals(worker.getWorkerStatus())) {
            throw new BusinessException("工人状态不支持离职申请");
        }

        Project currentProject = projectService.getById(worker.getProjectId());
        if (currentProject == null) {
            throw new BusinessException("工人当前项目不存在");
        }

        if ("transfer".equals(dto.getResignationType())) {
            if (dto.getTargetProjectId() == null) {
                throw new BusinessException("转项目必须选择目标项目");
            }
            Project targetProject = projectService.getById(dto.getTargetProjectId());
            if (targetProject == null) {
                throw new BusinessException("目标项目不存在");
            }
            if (!targetProject.getEnterpriseId().equals(worker.getEnterpriseId())) {
                throw new BusinessException("只能在同一企业内转项目");
            }
        }

        List<WorkerEquipment> activeEquipments = workerEquipmentService.getActiveEquipment(dto.getWorkerId());
        if (activeEquipments.isEmpty()) {
            throw new BusinessException("该工人没有领用装备，无需走归还流程");
        }

        BigDecimal totalDeposit = activeEquipments.stream()
                .map(e -> e.getDepositAmount().multiply(BigDecimal.valueOf(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ResignationApplication application = new ResignationApplication();
        application.setApplicationNo(generateApplicationNo());
        application.setWorkerId(worker.getId());
        application.setWorkerName(worker.getWorkerName());
        application.setCurrentProjectId(currentProject.getId());
        application.setCurrentProjectName(currentProject.getProjectName());
        application.setEnterpriseId(worker.getEnterpriseId());
        application.setResignationType(dto.getResignationType());
        application.setLastWorkDate(dto.getLastWorkDate());
        application.setTotalDeposit(totalDeposit);
        application.setDeductAmount(BigDecimal.ZERO);
        application.setRefundAmount(totalDeposit);
        application.setApplicationStatus("pending");
        application.setResponsibilityNote(dto.getResponsibilityNote());
        application.setPhotoIds(dto.getPhotoIds());

        if ("transfer".equals(dto.getResignationType()) && dto.getTargetProjectId() != null) {
            Project targetProject = projectService.getById(dto.getTargetProjectId());
            application.setTargetProjectId(targetProject.getId());
            application.setTargetProjectName(targetProject.getProjectName());
        }

        this.save(application);

        for (WorkerEquipment we : activeEquipments) {
            EquipmentReturn equipmentReturn = new EquipmentReturn();
            equipmentReturn.setResignationId(application.getId());
            equipmentReturn.setWorkerEquipmentId(we.getId());
            equipmentReturn.setEquipmentId(we.getEquipmentId());
            equipmentReturn.setEquipmentType(we.getEquipmentType());
            equipmentReturn.setEquipmentName(we.getEquipmentName());
            equipmentReturn.setQuantity(we.getQuantity());
            equipmentReturn.setDepositAmount(we.getDepositAmount());
            equipmentReturn.setReturnStatus("returned");
            equipmentReturn.setReturnCondition("good");
            equipmentReturn.setDeductAmount(BigDecimal.ZERO);
            equipmentReturnService.save(equipmentReturn);
        }

        worker.setWorkerStatus("off_job");
        workerService.updateById(worker);

        return application;
    }

    @Override
    public ResignationApplication getDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public List<EquipmentReturn> getEquipmentReturns(Long resignationId) {
        return equipmentReturnService.getByResignationId(resignationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void teamLeaderCheck(Long id, ResignationApplicationDTO dto, Long userId, String userName) {
        ResignationApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"pending".equals(application.getApplicationStatus())) {
            throw new BusinessException("申请状态不允许检查");
        }
        if (dto.getEquipmentReturns() == null || dto.getEquipmentReturns().isEmpty()) {
            throw new BusinessException("请填写装备归还明细");
        }

        BigDecimal totalDeduct = BigDecimal.ZERO;
        boolean hasPhotos = false;
        boolean hasResponsibility = false;

        for (EquipmentReturnDTO returnDTO : dto.getEquipmentReturns()) {
            EquipmentReturn equipmentReturn = equipmentReturnService.getById(
                    returnDTO.getWorkerEquipmentId() != null ? returnDTO.getWorkerEquipmentId() : returnDTO.getId()
            );
            if (equipmentReturn == null) {
                throw new BusinessException("装备归还记录不存在");
            }

            equipmentReturn.setReturnStatus(returnDTO.getReturnStatus());
            equipmentReturn.setReturnCondition(returnDTO.getReturnCondition());
            equipmentReturn.setDeductAmount(returnDTO.getDeductAmount() != null ? returnDTO.getDeductAmount() : BigDecimal.ZERO);
            equipmentReturn.setPhotoIds(returnDTO.getPhotoIds());
            equipmentReturn.setResponsibilityNote(returnDTO.getResponsibilityNote());
            equipmentReturn.setRemark(returnDTO.getRemark());
            equipmentReturnService.updateById(equipmentReturn);

            if (returnDTO.getDeductAmount() != null) {
                totalDeduct = totalDeduct.add(returnDTO.getDeductAmount());
            }

            if (StringUtils.hasText(returnDTO.getPhotoIds())) {
                hasPhotos = true;
            }
            if (StringUtils.hasText(returnDTO.getResponsibilityNote())) {
                hasResponsibility = true;
            }

            if (!"returned".equals(returnDTO.getReturnStatus()) && !"retained".equals(returnDTO.getReturnStatus())) {
                if (!hasPhotos) {
                    throw new BusinessException("装备" + equipmentReturn.getEquipmentName() + "缺失或损坏时必须上传照片");
                }
                if (!hasResponsibility) {
                    throw new BusinessException("装备" + equipmentReturn.getEquipmentName() + "缺失或损坏时必须填写责任说明");
                }
            }
        }

        application.setDeductAmount(totalDeduct);
        application.setRefundAmount(application.getTotalDeposit().subtract(totalDeduct));
        application.setTeamLeaderId(userId);
        application.setTeamLeaderName(userName);
        application.setCheckTime(LocalDateTime.now());
        application.setCheckRemark(dto.getCheckRemark());
        application.setPhotoIds(dto.getPhotoIds());
        application.setResponsibilityNote(dto.getResponsibilityNote());
        application.setApplicationStatus("pending_finance");
        this.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void financeAudit(Long id, ResignationApplicationDTO dto, Long userId, String userName) {
        ResignationApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"pending_finance".equals(application.getApplicationStatus())) {
            throw new BusinessException("申请状态不允许财务审核");
        }

        List<EquipmentReturn> returns = equipmentReturnService.getByResignationId(id);
        boolean hasMissingOrDamaged = returns.stream()
                .anyMatch(r -> "lost".equals(r.getReturnStatus()) || "damaged".equals(r.getReturnStatus()));

        if (hasMissingOrDamaged) {
            boolean allHasPhotos = returns.stream()
                    .filter(r -> "lost".equals(r.getReturnStatus()) || "damaged".equals(r.getReturnStatus()))
                    .allMatch(r -> StringUtils.hasText(r.getPhotoIds()));
            boolean allHasResponsibility = returns.stream()
                    .filter(r -> "lost".equals(r.getReturnStatus()) || "damaged".equals(r.getReturnStatus()))
                    .allMatch(r -> StringUtils.hasText(r.getResponsibilityNote()));

            if (!allHasPhotos) {
                throw new BusinessException("存在缺失或损坏装备未上传照片，财务无法审核");
            }
            if (!allHasResponsibility) {
                throw new BusinessException("存在缺失或损坏装备未填写责任说明，财务无法审核");
            }
        }

        application.setFinanceUserId(userId);
        application.setFinanceUserName(userName);
        application.setFinanceAuditTime(LocalDateTime.now());
        application.setFinanceRemark(dto.getFinanceRemark());
        application.setApplicationStatus("checked");
        this.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmRefund(Long id, BigDecimal actualRefundAmount, String refundVoucher, Long userId, String userName) {
        ResignationApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"checked".equals(application.getApplicationStatus())) {
            throw new BusinessException("申请状态不允许退款");
        }

        application.setActualRefundAmount(actualRefundAmount);
        application.setRefundVoucher(refundVoucher);
        application.setRefundTime(LocalDateTime.now());
        application.setFinanceUserId(userId);
        application.setFinanceUserName(userName);
        application.setApplicationStatus("refunded");
        this.updateById(application);

        List<EquipmentReturn> returns = equipmentReturnService.getByResignationId(id);
        for (EquipmentReturn returnItem : returns) {
            WorkerEquipment workerEquipment = workerEquipmentService.getById(returnItem.getWorkerEquipmentId());
            if (workerEquipment != null) {
                if ("returned".equals(returnItem.getReturnStatus())) {
                    workerEquipment.setEquipmentStatus("returned");
                    Equipment equipment = equipmentService.getById(returnItem.getEquipmentId());
                    if (equipment != null) {
                        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + returnItem.getQuantity());
                        equipmentService.updateById(equipment);
                    }
                } else if ("retained".equals(returnItem.getReturnStatus())) {
                    workerEquipment.setEquipmentStatus("retained");
                } else if ("lost".equals(returnItem.getReturnStatus()) || "damaged".equals(returnItem.getReturnStatus())) {
                    workerEquipment.setEquipmentStatus(returnItem.getReturnStatus());
                }
                workerEquipmentService.updateById(workerEquipment);
            }
        }

        Worker worker = workerService.getById(application.getWorkerId());
        if (worker != null) {
            if ("transfer".equals(application.getResignationType())) {
                worker.setProjectId(application.getTargetProjectId());
                worker.setWorkerStatus("transferred");
            } else {
                worker.setWorkerStatus("resigned");
            }
            workerService.updateById(worker);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApplication(Long id, String rejectReason, Long userId, String userName) {
        ResignationApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        application.setApplicationStatus("rejected");
        application.setFinanceRemark(rejectReason);
        application.setFinanceUserId(userId);
        application.setFinanceUserName(userName);
        application.setFinanceAuditTime(LocalDateTime.now());
        this.updateById(application);

        Worker worker = workerService.getById(application.getWorkerId());
        if (worker != null) {
            worker.setWorkerStatus("on_job");
            workerService.updateById(worker);
        }
    }

    private String generateApplicationNo() {
        String prefix = "RSG" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = this.count(new LambdaQueryWrapper<ResignationApplication>()
                .likeRight(ResignationApplication::getApplicationNo, prefix));
        return prefix + String.format("%04d", count + 1);
    }
}
