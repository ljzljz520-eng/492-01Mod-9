package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.WorkerEquipment;

import java.util.List;

public interface WorkerEquipmentService extends IService<WorkerEquipment> {

    Page<WorkerEquipment> pageQuery(Long current, Long size, Long workerId, String equipmentStatus);

    List<WorkerEquipment> getByWorkerId(Long workerId);

    List<WorkerEquipment> getActiveEquipment(Long workerId);

    void distributeEquipment(Long workerId, Long equipmentId, Long userId, String userName);

    void returnEquipment(Long id);
}
