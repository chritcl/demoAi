package com.oa.platform.workflow.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.R;
import com.oa.platform.workflow.entity.FlowDefinition;
import com.oa.platform.workflow.entity.FlowInstance;
import com.oa.platform.workflow.entity.FlowTask;
import com.oa.platform.workflow.service.FlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程管理。
 */
@Tag(name = "流程管理")
@RestController
@RequestMapping("/flow")
public class FlowController {

    private final FlowService flowService;

    public FlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    @Operation(summary = "流程定义列表")
    @PreAuthorize("@ss.hasPerm('workflow:definition:list')")
    @GetMapping("/definition/list")
    public R<List<FlowDefinition>> listDefinitions() {
        return R.ok(flowService.listDefinitions());
    }

    @Operation(summary = "流程定义详情(含节点)")
    @GetMapping("/definition/{id}")
    public R<FlowDefinition> getDefinition(@PathVariable Long id) {
        return R.ok(flowService.getDefinition(id));
    }

    @Operation(summary = "保存流程定义(含节点)")
    @OperLog(title = "流程定义", businessType = 1)
    @PreAuthorize("@ss.hasPerm('workflow:definition:edit')")
    @PostMapping("/definition")
    public R<Void> saveDefinition(@RequestBody FlowDefinition def) {
        flowService.saveDefinition(def);
        return R.ok();
    }

    /* ---------------- 审批 ---------------- */

    @Operation(summary = "审批通过")
    @OperLog(title = "流程审批", businessType = 2)
    @PostMapping("/task/approve")
    public R<Void> approve(@RequestParam Long taskId, @RequestParam(required = false) String comment) {
        flowService.approve(taskId, comment);
        return R.ok();
    }

    @Operation(summary = "驳回")
    @OperLog(title = "流程审批", businessType = 2)
    @PostMapping("/task/reject")
    public R<Void> reject(@RequestParam Long taskId, @RequestParam(required = false) String comment) {
        flowService.reject(taskId, comment);
        return R.ok();
    }

    @Operation(summary = "转办")
    @OperLog(title = "流程审批", businessType = 2)
    @PostMapping("/task/transfer")
    public R<Void> transfer(@RequestParam Long taskId, @RequestParam Long toUserId,
                            @RequestParam(required = false) String comment) {
        flowService.transfer(taskId, toUserId, comment);
        return R.ok();
    }

    /* ---------------- 查询 ---------------- */

    @Operation(summary = "我的待办")
    @GetMapping("/task/todo")
    public R<List<FlowTask>> todo() {
        return R.ok(flowService.myTodo());
    }

    @Operation(summary = "我的已办")
    @GetMapping("/task/done")
    public R<List<FlowTask>> done() {
        return R.ok(flowService.myDone());
    }

    @Operation(summary = "我的发起")
    @GetMapping("/instance/mine")
    public R<List<FlowInstance>> mine() {
        return R.ok(flowService.myInitiated());
    }

    @Operation(summary = "流程实例详情")
    @GetMapping("/instance/{id}")
    public R<FlowInstance> instance(@PathVariable Long id) {
        return R.ok(flowService.getInstance(id));
    }

    @Operation(summary = "按业务查询实例")
    @GetMapping("/instance/by-business")
    public R<FlowInstance> instanceByBusiness(@RequestParam String businessType, @RequestParam Long businessId) {
        return R.ok(flowService.getByBusiness(businessType, businessId));
    }

    @Operation(summary = "按业务查询审批轨迹")
    @GetMapping("/task/business")
    public R<List<FlowTask>> tasksOfBusiness(@RequestParam String businessType, @RequestParam Long businessId) {
        return R.ok(flowService.tasksOfBusiness(businessType, businessId));
    }
}
