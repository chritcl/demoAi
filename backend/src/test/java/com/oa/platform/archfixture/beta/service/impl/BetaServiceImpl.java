package com.oa.platform.archfixture.beta.service.impl;

import com.oa.platform.archfixture.alpha.entity.AlphaEntity;
import com.oa.platform.archfixture.alpha.mapper.AlphaMapper;
import com.oa.platform.archfixture.alpha.service.AlphaService;
import com.oa.platform.archfixture.alpha.service.impl.AlphaServiceImpl;
import com.oa.platform.archfixture.beta.service.BetaService;

/**
 * 违规夹具：跨业务域直接依赖对方的 Mapper、Entity 与 service.impl，
 * 用于验证跨业务域边界规则会失败。
 */
public class BetaServiceImpl implements BetaService {

    private final AlphaMapper alphaMapper;
    private final AlphaService alphaService;

    public BetaServiceImpl(AlphaMapper alphaMapper, AlphaService alphaService) {
        this.alphaMapper = alphaMapper;
        this.alphaService = alphaService;
    }

    @Override
    public String describe() {
        return "beta->" + alphaService.describe();
    }

    public AlphaEntity misuse(Long id) {
        AlphaServiceImpl illegal = new AlphaServiceImpl(alphaMapper, this);
        return illegal.describe() == null ? null : alphaMapper.selectById(id);
    }
}
