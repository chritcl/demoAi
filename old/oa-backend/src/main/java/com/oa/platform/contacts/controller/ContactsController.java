package com.oa.platform.contacts.controller;

import com.oa.platform.common.api.R;
import com.oa.platform.contacts.service.ContactsService;
import com.oa.platform.system.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 通讯录。
 */
@Tag(name = "通讯录")
@RestController
@RequestMapping("/contacts")
public class ContactsController {

    private final ContactsService contactsService;

    public ContactsController(ContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @Operation(summary = "组织/通讯录树")
    @GetMapping("/tree")
    public R<List<Map<String, Object>>> tree() {
        return R.ok(contactsService.deptTree());
    }

    @Operation(summary = "搜索联系人(姓名/拼音/电话/邮箱)")
    @GetMapping("/search")
    public R<List<SysUser>> search(@RequestParam(required = false) String keyword) {
        return R.ok(contactsService.search(keyword));
    }
}
