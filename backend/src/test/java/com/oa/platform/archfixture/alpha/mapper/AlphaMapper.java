package com.oa.platform.archfixture.alpha.mapper;

import com.oa.platform.archfixture.alpha.entity.AlphaEntity;

/**
 * 违规夹具：alpha 业务域 Mapper。
 */
public interface AlphaMapper {

    AlphaEntity selectById(Long id);
}
