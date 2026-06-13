package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Equipment;
import com.scaffolding.mapper.EquipmentMapper;
import com.scaffolding.service.EquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Override
    public Page<Equipment> pageQuery(Long current, Long size, String equipmentName, String equipmentType) {
        Page<Equipment> page = new Page<>(current, size);
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(equipmentName)) {
            wrapper.like(Equipment::getEquipmentName, equipmentName);
        }
        if (StringUtils.hasText(equipmentType)) {
            wrapper.eq(Equipment::getEquipmentType, equipmentType);
        }

        wrapper.orderByDesc(Equipment::getCreateTime);
        return this.page(page, wrapper);
    }
}
