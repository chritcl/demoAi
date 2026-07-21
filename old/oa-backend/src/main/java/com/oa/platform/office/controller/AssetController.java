package com.oa.platform.office.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.entity.Asset;
import com.oa.platform.office.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 资产管理。
 */
@Tag(name = "资产管理")
@RestController
@RequestMapping("/office/asset")
public class AssetController {

    private final AssetService service;

    public AssetController(AssetService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询")
    @PreAuthorize("@ss.hasPerm('office:asset:list')")
    @GetMapping("/page")
    public R<PageResult<Asset>> page(PageQuery pq,
                                     @RequestParam(required = false) String assetName,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) Integer status) {
        return R.ok(service.page(pq, assetName, category, status));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<Asset> detail(@PathVariable Long id) {
        return R.ok(service.detail(id));
    }

    @Operation(summary = "新增")
    @OperLog(title = "资产管理", businessType = 1)
    @PreAuthorize("@ss.hasPerm('office:asset:add')")
    @PostMapping
    public R<Void> create(@RequestBody Asset asset) {
        service.create(asset);
        return R.ok();
    }

    @Operation(summary = "修改")
    @OperLog(title = "资产管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('office:asset:edit')")
    @PutMapping
    public R<Void> update(@RequestBody Asset asset) {
        service.update(asset);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperLog(title = "资产管理", businessType = 3)
    @PreAuthorize("@ss.hasPerm('office:asset:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }
}
