package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Worker;
import com.scaffolding.service.WorkerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/worker")
@Api(tags = "工人管理")
public class WorkerController {

    @Autowired
    private WorkerService workerService;

    @PostMapping
    @ApiOperation("新增工人")
    public Result<Worker> save(@RequestBody Worker worker) {
        try {
            Worker exist = workerService.getByIdCard(worker.getIdCard());
            if (exist != null) {
                return Result.error("该身份证号已存在");
            }
            workerService.save(worker);
            return Result.success("新增成功", worker);
        } catch (Exception e) {
            log.error("新增工人失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新工人")
    public Result<Worker> update(@PathVariable Long id, @RequestBody Worker worker) {
        try {
            worker.setId(id);
            workerService.updateById(worker);
            return Result.success("更新成功", worker);
        } catch (Exception e) {
            log.error("更新工人失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除工人")
    public Result<?> delete(@PathVariable Long id) {
        try {
            workerService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除工人失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询工人")
    public Result<Worker> getById(@PathVariable Long id) {
        Worker worker = workerService.getById(id);
        if (worker == null) {
            return Result.error("工人不存在");
        }
        return Result.success(worker);
    }

    @GetMapping("/idCard/{idCard}")
    @ApiOperation("根据身份证号查询工人")
    public Result<Worker> getByIdCard(@PathVariable String idCard) {
        Worker worker = workerService.getByIdCard(idCard);
        if (worker == null) {
            return Result.error("工人不存在");
        }
        return Result.success(worker);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询工人")
    public Result<PageResult<Worker>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String workerStatus) {
        Page<Worker> page = workerService.pageQuery(current, size, workerName, phone, enterpriseId, projectId, workerStatus);
        PageResult<Worker> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
