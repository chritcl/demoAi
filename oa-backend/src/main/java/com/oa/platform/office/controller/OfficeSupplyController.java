package com.oa.platform.office.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.entity.OfficeSupply;
import com.oa.platform.office.service.OfficeSupplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 办公用品管理。
 */
@Tag(name = "办公用品")
@RestController
@RequestMapping("/office/supply")
public class OfficeSupplyController {

    private final OfficeSupplyService service;

    public OfficeSupplyController(OfficeSupplyService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询")
    @PreAuthorize("@ss.hasPerm('office:supply:list')")
    @GetMapping("/page")
    public R<PageResult<OfficeSupply>> page(PageQuery pq,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) String category) {
        return R.ok(service.page(pq, name, category));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<OfficeSupply> detail(@PathVariable Long id) {
        return R.ok(service.detail(id));
    }

    @Operation(summary = "新增")
    @OperLog(title = "办公用品", businessType = 1)
    @PreAuthorize("@ss.hasPerm('office:supply:add')")
    @PostMapping
    public R<Void> create(@RequestBody OfficeSupply supply) {
        service.create(supply);
        return R.ok();
    }

    @Operation(summary = "修改")
    @OperLog(title = "办公用品", businessType = 2)
    @PreAuthorize("@ss.hasPerm('office:supply:edit')")
    @PutMapping
    public R<Void> update(@RequestBody OfficeSupply supply) {
        service.update(supply);
        return R.ok();
    }

    @Operation(summary = "出入库")
    @OperLog(title = "办公用品", businessType = 2)
    @PreAuthorize("@ss.hasPerm('office:supply:edit')")
    @PutMapping("/{id}/stock")
    public R<Void> adjustStock(@PathVariable Long id, @RequestParam int amount) {
        service.adjustStock(id, amount);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperLog(title = "办公用品", businessType = 3)
    @PreAuthorize("@ss.hasPerm('office:supply:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }
}
