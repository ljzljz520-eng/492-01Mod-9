package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Enterprise;

public interface EnterpriseService extends IService<Enterprise> {

    Page<Enterprise> pageQuery(Long current, Long size, String enterpriseName);
}
