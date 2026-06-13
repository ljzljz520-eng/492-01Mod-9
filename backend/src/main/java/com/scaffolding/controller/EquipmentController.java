package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Equipment;
import com.scaffolding.service.EquipmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/equipment")
@Api(tags = "装备管理")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @PostMapping
    @ApiOperation("新增装备")
    public Result<Equipment> save(@RequestBody Equipment equipment) {
        try {
            equipmentService.save(equipment);
            return Result.success("新增成功", equipment);
        } catch (Exception e) {
            log.error("新增装备失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新装备")
    public Result<Equipment> update(@PathVariable Long id, @RequestBody Equipment equipment) {
        try {
            equipment.setId(id);
            equipmentService.updateById(equipment);
            return Result.success("更新成功", equipment);
        } catch (Exception e) {
            log.error("更新装备失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除装备")
    public Result<?> delete(@PathVariable Long id) {
        try {
            equipmentService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除装备失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询装备")
    public Result<Equipment> getById(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return Result.error("装备不存在");
        }
        return Result.success(equipment);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有装备")
    public Result<List<Equipment>> list() {
        return Result.success(equipmentService.list());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询装备")
    public Result<PageResult<Equipment>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String equipmentName,
            @RequestParam(required = false) String equipmentType) {
        Page<Equipment> page = equipmentService.pageQuery(current, size, equipmentName, equipmentType);
        PageResult<Equipment> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
