package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Enterprise;
import com.scaffolding.service.EnterpriseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/enterprise")
@Api(tags = "企业管理")
public class EnterpriseController {

    @Autowired
    private EnterpriseService enterpriseService;

    @PostMapping
    @ApiOperation("新增企业")
    public Result<Enterprise> save(@RequestBody Enterprise enterprise) {
        try {
            enterpriseService.save(enterprise);
            return Result.success("新增成功", enterprise);
        } catch (Exception e) {
            log.error("新增企业失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新企业")
    public Result<Enterprise> update(@PathVariable Long id, @RequestBody Enterprise enterprise) {
        try {
            enterprise.setId(id);
            enterpriseService.updateById(enterprise);
            return Result.success("更新成功", enterprise);
        } catch (Exception e) {
            log.error("更新企业失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除企业")
    public Result<?> delete(@PathVariable Long id) {
        try {
            enterpriseService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除企业失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询企业")
    public Result<Enterprise> getById(@PathVariable Long id) {
        Enterprise enterprise = enterpriseService.getById(id);
        if (enterprise == null) {
            return Result.error("企业不存在");
        }
        return Result.success(enterprise);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有企业")
    public Result<List<Enterprise>> list() {
        return Result.success(enterpriseService.list());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询企业")
    public Result<PageResult<Enterprise>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String enterpriseName) {
        Page<Enterprise> page = enterpriseService.pageQuery(current, size, enterpriseName);
        PageResult<Enterprise> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
