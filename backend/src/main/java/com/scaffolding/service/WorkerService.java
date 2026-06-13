package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Worker;

public interface WorkerService extends IService<Worker> {

    Page<Worker> pageQuery(Long current, Long size, String workerName, String phone, Long enterpriseId, Long projectId, String workerStatus);

    Worker getByIdCard(String idCard);
}
