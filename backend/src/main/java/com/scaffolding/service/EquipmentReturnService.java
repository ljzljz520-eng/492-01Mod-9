package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.EquipmentReturn;

import java.util.List;

public interface EquipmentReturnService extends IService<EquipmentReturn> {

    List<EquipmentReturn> getByResignationId(Long resignationId);
}
