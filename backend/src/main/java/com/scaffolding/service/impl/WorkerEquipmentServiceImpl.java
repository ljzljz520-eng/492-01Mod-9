package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Equipment;
import com.scaffolding.entity.WorkerEquipment;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.WorkerEquipmentMapper;
import com.scaffolding.service.EquipmentService;
import com.scaffolding.service.WorkerEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WorkerEquipmentServiceImpl extends ServiceImpl<WorkerEquipmentMapper, WorkerEquipment> implements WorkerEquipmentService {

    @Autowired
    private EquipmentService equipmentService;

    @Override
    public Page<WorkerEquipment> pageQuery(Long current, Long size, Long workerId, String equipmentStatus) {
        Page<WorkerEquipment> page = new Page<>(current, size);
        LambdaQueryWrapper<WorkerEquipment> wrapper = new LambdaQueryWrapper<>();

        if (workerId != null) {
            wrapper.eq(WorkerEquipment::getWorkerId, workerId);
        }
        if (equipmentStatus != null) {
            wrapper.eq(WorkerEquipment::getEquipmentStatus, equipmentStatus);
        }

        wrapper.orderByDesc(WorkerEquipment::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<WorkerEquipment> getByWorkerId(Long workerId) {
        LambdaQueryWrapper<WorkerEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkerEquipment::getWorkerId, workerId)
               .orderByDesc(WorkerEquipment::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<WorkerEquipment> getActiveEquipment(Long workerId) {
        LambdaQueryWrapper<WorkerEquipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkerEquipment::getWorkerId, workerId)
               .in(WorkerEquipment::getEquipmentStatus, "in_use", "retained")
               .orderByAsc(WorkerEquipment::getEquipmentType);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distributeEquipment(Long workerId, Long equipmentId, Long userId, String userName) {
        Equipment equipment = equipmentService.getById(equipmentId);
        if (equipment == null) {
            throw new BusinessException("装备不存在");
        }
        if (equipment.getAvailableQuantity() <= 0) {
            throw new BusinessException("装备库存不足");
        }

        WorkerEquipment workerEquipment = new WorkerEquipment();
        workerEquipment.setWorkerId(workerId);
        workerEquipment.setEquipmentId(equipmentId);
        workerEquipment.setEquipmentType(equipment.getEquipmentType());
        workerEquipment.setEquipmentName(equipment.getEquipmentName());
        workerEquipment.setQuantity(1);
        workerEquipment.setDepositAmount(equipment.getDepositAmount());
        workerEquipment.setReceiveDate(LocalDate.now());
        workerEquipment.setReceiveUserId(userId);
        workerEquipment.setReceiveUserName(userName);
        workerEquipment.setEquipmentStatus("in_use");
        this.save(workerEquipment);

        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - 1);
        equipmentService.updateById(equipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnEquipment(Long id) {
        WorkerEquipment workerEquipment = this.getById(id);
        if (workerEquipment == null) {
            throw new BusinessException("领用记录不存在");
        }

        workerEquipment.setEquipmentStatus("returned");
        this.updateById(workerEquipment);

        Equipment equipment = equipmentService.getById(workerEquipment.getEquipmentId());
        if (equipment != null) {
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + 1);
            equipmentService.updateById(equipment);
        }
    }
}
