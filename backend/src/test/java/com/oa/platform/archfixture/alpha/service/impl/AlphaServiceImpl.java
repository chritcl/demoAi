package com.oa.platform.archfixture.alpha.service.impl;

import com.oa.platform.archfixture.alpha.mapper.AlphaMapper;
import com.oa.platform.archfixture.alpha.service.AlphaService;
import com.oa.platform.archfixture.beta.service.BetaService;

/**
 * 合法夹具：使用本业务域 Mapper，跨业务域只面向对方 Service 接口。
 * 用于验证架构规则不会误报合法的跨域接口调用。
 */
public class AlphaServiceImpl implements AlphaService {

    private final AlphaMapper alphaMapper;
    private final BetaService betaService;

    public AlphaServiceImpl(AlphaMapper alphaMapper, BetaService betaService) {
        this.alphaMapper = alphaMapper;
        this.betaService = betaService;
    }

    @Override
    public String describe() {
        return "alpha->" + betaService.describe();
    }
}
