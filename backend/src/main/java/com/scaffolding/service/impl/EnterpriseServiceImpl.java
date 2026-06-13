package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Enterprise;
import com.scaffolding.mapper.EnterpriseMapper;
import com.scaffolding.service.EnterpriseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements EnterpriseService {

    @Override
    public Page<Enterprise> pageQuery(Long current, Long size, String enterpriseName) {
        Page<Enterprise> page = new Page<>(current, size);
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(enterpriseName)) {
            wrapper.like(Enterprise::getEnterpriseName, enterpriseName);
        }

        wrapper.orderByDesc(Enterprise::getCreateTime);
        return this.page(page, wrapper);
    }
}
