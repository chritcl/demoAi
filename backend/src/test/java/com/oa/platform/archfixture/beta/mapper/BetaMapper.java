package com.oa.platform.archfixture.beta.mapper;

import com.oa.platform.archfixture.beta.entity.BetaEntity;

/**
 * 违规夹具：beta 业务域 Mapper。
 */
public interface BetaMapper {

    BetaEntity selectById(Long id);
}
