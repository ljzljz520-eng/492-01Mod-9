package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Equipment;

public interface EquipmentService extends IService<Equipment> {

    Page<Equipment> pageQuery(Long current, Long size, String equipmentName, String equipmentType);
}
