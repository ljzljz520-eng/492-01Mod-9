package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Project;
import com.scaffolding.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/project")
@Api(tags = "项目管理")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @ApiOperation("新增项目")
    public Result<Project> save(@RequestBody Project project) {
        try {
            projectService.save(project);
            return Result.success("新增成功", project);
        } catch (Exception e) {
            log.error("新增项目失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新项目")
    public Result<Project> update(@PathVariable Long id, @RequestBody Project project) {
        try {
            project.setId(id);
            projectService.updateById(project);
            return Result.success("更新成功", project);
        } catch (Exception e) {
            log.error("更新项目失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除项目")
    public Result<?> delete(@PathVariable Long id) {
        try {
            projectService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除项目失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询项目")
    public Result<Project> getById(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有项目")
    public Result<List<Project>> list() {
        return Result.success(projectService.list());
    }

    @GetMapping("/byEnterprise/{enterpriseId}")
    @ApiOperation("根据企业ID查询项目")
    public Result<List<Project>> getByEnterpriseId(@PathVariable Long enterpriseId) {
        return Result.success(projectService.getByEnterpriseId(enterpriseId));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询项目")
    public Result<PageResult<Project>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) String projectStatus) {
        Page<Project> page = projectService.pageQuery(current, size, projectName, enterpriseId, projectStatus);
        PageResult<Project> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
