package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Worker;
import com.scaffolding.mapper.WorkerMapper;
import com.scaffolding.service.WorkerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {

    @Override
    public Page<Worker> pageQuery(Long current, Long size, String workerName, String phone, Long enterpriseId, Long projectId, String workerStatus) {
        Page<Worker> page = new Page<>(current, size);
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(workerName)) {
            wrapper.like(Worker::getWorkerName, workerName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(Worker::getPhone, phone);
        }
        if (enterpriseId != null) {
            wrapper.eq(Worker::getEnterpriseId, enterpriseId);
        }
        if (projectId != null) {
            wrapper.eq(Worker::getProjectId, projectId);
        }
        if (StringUtils.hasText(workerStatus)) {
            wrapper.eq(Worker::getWorkerStatus, workerStatus);
        }

        wrapper.orderByDesc(Worker::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Worker getByIdCard(String idCard) {
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Worker::getIdCard, idCard);
        return this.getOne(wrapper);
    }
}
