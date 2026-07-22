package com.oa.platform.archfixture.common;

import com.oa.platform.archfixture.alpha.entity.AlphaEntity;

/**
 * 违规夹具：common 依赖具体业务域，用于验证 common 边界规则会失败。
 */
public class FixtureCommonUtil {

    public String nameOf(AlphaEntity entity) {
        return String.valueOf(entity.getId());
    }
}
