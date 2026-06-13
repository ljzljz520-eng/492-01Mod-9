package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.WorkerEquipment;
import com.scaffolding.service.WorkerEquipmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workerEquipment")
@Api(tags = "工人装备管理")
public class WorkerEquipmentController {

    @Autowired
    private WorkerEquipmentService workerEquipmentService;

    @PostMapping("/distribute")
    @ApiOperation("发放装备")
    public Result<?> distribute(@RequestParam Long workerId, @RequestParam Long equipmentId, @RequestParam(required = false) Long userId, @RequestParam(required = false) String userName) {
        try {
            workerEquipmentService.distributeEquipment(workerId, equipmentId, userId, userName);
            return Result.success("发放成功");
        } catch (Exception e) {
            log.error("发放装备失败", e);
            return Result.error("发放失败：" + e.getMessage());
        }
    }

    @PostMapping("/return/{id}")
    @ApiOperation("归还装备")
    public Result<?> returnEquipment(@PathVariable Long id) {
        try {
            workerEquipmentService.returnEquipment(id);
            return Result.success("归还成功");
        } catch (Exception e) {
            log.error("归还装备失败", e);
            return Result.error("归还失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新领用记录")
    public Result<WorkerEquipment> update(@PathVariable Long id, @RequestBody WorkerEquipment workerEquipment) {
        try {
            workerEquipment.setId(id);
            workerEquipmentService.updateById(workerEquipment);
            return Result.success("更新成功", workerEquipment);
        } catch (Exception e) {
            log.error("更新领用记录失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除领用记录")
    public Result<?> delete(@PathVariable Long id) {
        try {
            workerEquipmentService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除领用记录失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询领用记录")
    public Result<WorkerEquipment> getById(@PathVariable Long id) {
        WorkerEquipment workerEquipment = workerEquipmentService.getById(id);
        if (workerEquipment == null) {
            return Result.error("领用记录不存在");
        }
        return Result.success(workerEquipment);
    }

    @GetMapping("/byWorker/{workerId}")
    @ApiOperation("查询工人的所有装备领用记录")
    public Result<List<WorkerEquipment>> getByWorkerId(@PathVariable Long workerId) {
        return Result.success(workerEquipmentService.getByWorkerId(workerId));
    }

    @GetMapping("/active/{workerId}")
    @ApiOperation("查询工人的在用装备")
    public Result<List<WorkerEquipment>> getActiveEquipment(@PathVariable Long workerId) {
        return Result.success(workerEquipmentService.getActiveEquipment(workerId));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询领用记录")
    public Result<PageResult<WorkerEquipment>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String equipmentStatus) {
        Page<WorkerEquipment> page = workerEquipmentService.pageQuery(current, size, workerId, equipmentStatus);
        PageResult<WorkerEquipment> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
