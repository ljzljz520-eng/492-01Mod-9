package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.dto.ResignationApplicationDTO;
import com.scaffolding.entity.EquipmentReturn;
import com.scaffolding.entity.ResignationApplication;
import com.scaffolding.service.ResignationApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/resignation")
@Api(tags = "离职押金归还管理")
public class ResignationApplicationController {

    @Autowired
    private ResignationApplicationService resignationApplicationService;

    @PostMapping
    @ApiOperation("创建离职申请")
    public Result<ResignationApplication> create(@RequestBody ResignationApplicationDTO dto, @RequestParam(required = false) Long userId, @RequestParam(required = false) String userName) {
        try {
            ResignationApplication application = resignationApplicationService.createApplication(dto, userId, userName);
            return Result.success("申请创建成功", application);
        } catch (Exception e) {
            log.error("创建离职申请失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PostMapping("/teamLeaderCheck/{id}")
    @ApiOperation("班组长检查")
    public Result<?> teamLeaderCheck(@PathVariable Long id, @RequestBody ResignationApplicationDTO dto, @RequestParam(required = false) Long userId, @RequestParam(required = false) String userName) {
        try {
            resignationApplicationService.teamLeaderCheck(id, dto, userId, userName);
            return Result.success("检查完成");
        } catch (Exception e) {
            log.error("班组长检查失败", e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    @PostMapping("/financeAudit/{id}")
    @ApiOperation("财务审核")
    public Result<?> financeAudit(@PathVariable Long id, @RequestBody ResignationApplicationDTO dto, @RequestParam(required = false) Long userId, @RequestParam(required = false) String userName) {
        try {
            resignationApplicationService.financeAudit(id, dto, userId, userName);
            return Result.success("审核完成");
        } catch (Exception e) {
            log.error("财务审核失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    @PostMapping("/confirmRefund/{id}")
    @ApiOperation("确认退款")
    public Result<?> confirmRefund(@PathVariable Long id, @RequestParam BigDecimal actualRefundAmount, @RequestParam(required = false) String refundVoucher, @RequestParam(required = false) Long userId, @RequestParam(required = false) String userName) {
        try {
            resignationApplicationService.confirmRefund(id, actualRefundAmount, refundVoucher, userId, userName);
            return Result.success("退款确认完成");
        } catch (Exception e) {
            log.error("确认退款失败", e);
            return Result.error("确认失败：" + e.getMessage());
        }
    }

    @PostMapping("/reject/{id}")
    @ApiOperation("驳回申请")
    public Result<?> reject(@PathVariable Long id, @RequestParam String rejectReason, @RequestParam(required = false) Long userId, @RequestParam(required = false) String userName) {
        try {
            resignationApplicationService.rejectApplication(id, rejectReason, userId, userName);
            return Result.success("驳回成功");
        } catch (Exception e) {
            log.error("驳回申请失败", e);
            return Result.error("驳回失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("查询申请详情")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        ResignationApplication application = resignationApplicationService.getDetail(id);
        if (application == null) {
            return Result.error("申请不存在");
        }
        List<EquipmentReturn> equipmentReturns = resignationApplicationService.getEquipmentReturns(id);
        Map<String, Object> result = new HashMap<>();
        result.put("application", application);
        result.put("equipmentReturns", equipmentReturns);
        return Result.success(result);
    }

    @GetMapping("/{id}/equipmentReturns")
    @ApiOperation("查询申请的装备归还明细")
    public Result<List<EquipmentReturn>> getEquipmentReturns(@PathVariable Long id) {
        return Result.success(resignationApplicationService.getEquipmentReturns(id));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询离职申请")
    public Result<PageResult<ResignationApplication>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String applicationStatus,
            @RequestParam(required = false) String resignationType) {
        Page<ResignationApplication> page = resignationApplicationService.pageQuery(current, size, workerName, enterpriseId, projectId, applicationStatus, resignationType);
        PageResult<ResignationApplication> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
