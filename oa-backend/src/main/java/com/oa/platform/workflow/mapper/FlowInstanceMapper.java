package com.oa.platform.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.workflow.entity.FlowInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowInstanceMapper extends BaseMapper<FlowInstance> {
}
