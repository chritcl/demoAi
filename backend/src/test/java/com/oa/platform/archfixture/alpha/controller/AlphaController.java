package com.oa.platform.archfixture.alpha.controller;

import com.oa.platform.archfixture.alpha.entity.AlphaEntity;
import com.oa.platform.archfixture.alpha.mapper.AlphaMapper;

/**
 * 违规夹具：Controller 直接依赖 Mapper 与 Entity，用于验证架构规则会失败。
 */
public class AlphaController {

    private final AlphaMapper alphaMapper;

    public AlphaController(AlphaMapper alphaMapper) {
        this.alphaMapper = alphaMapper;
    }

    public AlphaEntity load(Long id) {
        return alphaMapper.selectById(id);
    }
}
