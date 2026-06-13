package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.EquipmentReturn;
import com.scaffolding.mapper.EquipmentReturnMapper;
import com.scaffolding.service.EquipmentReturnService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentReturnServiceImpl extends ServiceImpl<EquipmentReturnMapper, EquipmentReturn> implements EquipmentReturnService {

    @Override
    public List<EquipmentReturn> getByResignationId(Long resignationId) {
        LambdaQueryWrapper<EquipmentReturn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EquipmentReturn::getResignationId, resignationId)
               .orderByAsc(EquipmentReturn::getEquipmentType);
        return this.list(wrapper);
    }
}
