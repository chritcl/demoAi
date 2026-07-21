package com.oa.platform.system.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.entity.SysDictData;
import com.oa.platform.system.entity.SysDictType;
import com.oa.platform.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理。
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
public class SysDictController {

    private final SysDictService dictService;

    public SysDictController(SysDictService dictService) {
        this.dictService = dictService;
    }

    @Operation(summary = "字典类型分页")
    @PreAuthorize("@ss.hasPerm('system:dict:list')")
    @GetMapping("/type/page")
    public R<PageResult<SysDictType>> pageType(PageQuery pq,
                                                @RequestParam(required = false) String dictName,
                                                @RequestParam(required = false) String dictType) {
        return R.ok(dictService.pageType(pq, dictName, dictType));
    }

    @Operation(summary = "全部字典类型")
    @GetMapping("/type/option")
    public R<List<SysDictType>> typeOption() {
        return R.ok(dictService.listType());
    }

    @Operation(summary = "新增字典类型")
    @OperLog(title = "字典类型", businessType = 1)
    @PreAuthorize("@ss.hasPerm('system:dict:add')")
    @PostMapping("/type")
    public R<Void> createType(@RequestBody SysDictType type) {
        dictService.createType(type);
        return R.ok();
    }

    @Operation(summary = "修改字典类型")
    @OperLog(title = "字典类型", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:dict:edit')")
    @PutMapping("/type")
    public R<Void> updateType(@RequestBody SysDictType type) {
        dictService.updateType(type);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @OperLog(title = "字典类型", businessType = 3)
    @PreAuthorize("@ss.hasPerm('system:dict:remove')")
    @DeleteMapping("/type/{id}")
    public R<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return R.ok();
    }

    @Operation(summary = "根据类型查询字典数据")
    @GetMapping("/data/{dictType}")
    public R<List<SysDictData>> data(@PathVariable String dictType) {
        return R.ok(dictService.listData(dictType));
    }

    @Operation(summary = "新增字典数据")
    @OperLog(title = "字典数据", businessType = 1)
    @PreAuthorize("@ss.hasPerm('system:dict:add')")
    @PostMapping("/data")
    public R<Void> createData(@RequestBody SysDictData data) {
        dictService.createData(data);
        return R.ok();
    }

    @Operation(summary = "修改字典数据")
    @OperLog(title = "字典数据", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:dict:edit')")
    @PutMapping("/data")
    public R<Void> updateData(@RequestBody SysDictData data) {
        dictService.updateData(data);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @OperLog(title = "字典数据", businessType = 3)
    @PreAuthorize("@ss.hasPerm('system:dict:remove')")
    @DeleteMapping("/data/{id}")
    public R<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return R.ok();
    }
}
