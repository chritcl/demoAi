package com.oa.platform.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.system.entity.SysUser;
import com.oa.platform.system.mapper.SysUserMapper;
import com.oa.platform.workflow.entity.FlowDefinition;
import com.oa.platform.workflow.entity.FlowInstance;
import com.oa.platform.workflow.entity.FlowNode;
import com.oa.platform.workflow.entity.FlowTask;
import com.oa.platform.workflow.mapper.FlowDefinitionMapper;
import com.oa.platform.workflow.mapper.FlowInstanceMapper;
import com.oa.platform.workflow.mapper.FlowNodeMapper;
import com.oa.platform.workflow.mapper.FlowTaskMapper;
import com.oa.platform.workflow.event.FlowCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 轻量审批流程引擎服务。
 * <p>支持：发起、审批通过、驳回、转办、待办/已办/办结/我的发起。</p>
 */
@Service
public class FlowService {

    private final FlowDefinitionMapper definitionMapper;
    private final FlowNodeMapper nodeMapper;
    private final FlowInstanceMapper instanceMapper;
    private final FlowTaskMapper taskMapper;
    private final SysUserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public FlowService(FlowDefinitionMapper definitionMapper, FlowNodeMapper nodeMapper,
                       FlowInstanceMapper instanceMapper, FlowTaskMapper taskMapper,
                       SysUserMapper userMapper, ApplicationEventPublisher eventPublisher) {
        this.definitionMapper = definitionMapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    /* ===================== 流程定义维护 ===================== */

    public List<FlowDefinition> listDefinitions() {
        return definitionMapper.selectList(new LambdaQueryWrapper<FlowDefinition>()
                .orderByDesc(FlowDefinition::getId));
    }

    public FlowDefinition getDefinition(Long id) {
        FlowDefinition def = definitionMapper.selectById(id);
        if (def == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        def.setNodes(nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, id).orderByAsc(FlowNode::getSort)));
        return def;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDefinition(FlowDefinition def) {
        if (def.getFlowKey() == null || def.getFlowKey().isBlank()) {
            throw new BusinessException("流程标识不能为空");
        }
        def.setBusinessType(def.getFlowKey());
        def.setStatus(0);
        def.setVersion(1);
        if (def.getId() == null) {
            definitionMapper.insert(def);
        } else {
            definitionMapper.updateById(def);
            nodeMapper.delete(new LambdaQueryWrapper<FlowNode>().eq(FlowNode::getFlowId, def.getId()));
        }
        List<FlowNode> nodes = def.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            throw new BusinessException("流程节点不能为空");
        }
        int sort = 1;
        for (FlowNode node : nodes) {
            node.setId(null);
            node.setFlowId(def.getId());
            if (node.getSort() == null) {
                node.setSort(sort);
            }
            sort++;
            nodeMapper.insert(node);
        }
    }

    /* ===================== 流程运行 ===================== */

    /**
     * 发起流程。
     *
     * @return 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long start(String flowKey, Long businessId, String title) {
        FlowDefinition def = definitionMapper.selectOne(new LambdaQueryWrapper<FlowDefinition>()
                .eq(FlowDefinition::getFlowKey, flowKey)
                .eq(FlowDefinition::getStatus, 0)
                .last("LIMIT 1"));
        if (def == null) {
            throw new BusinessException(ResultCode.FLOW_ERROR, "未找到启用的流程定义: " + flowKey);
        }
        List<FlowNode> nodes = nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, def.getId()).orderByAsc(FlowNode::getSort));
        if (nodes.isEmpty()) {
            throw new BusinessException(ResultCode.FLOW_ERROR, "流程未配置节点");
        }
        FlowNode first = nodes.get(0);
        Long startUserId = SecurityUtils.getCurrentUserId();
        SysUser starter = userMapper.selectById(startUserId);

        FlowInstance instance = new FlowInstance();
        instance.setFlowId(def.getId());
        instance.setFlowKey(flowKey);
        instance.setBusinessType(def.getBusinessType());
        instance.setBusinessId(businessId);
        instance.setTitle(title);
        instance.setStartUserId(startUserId);
        instance.setStartUserName(starter == null ? null : starter.getNickname());
        instance.setCurrentNodeId(first.getId());
        instance.setCurrentNodeName(first.getNodeName());
        instance.setStatus(Constants.FLOW_RUNNING);
        instanceMapper.insert(instance);

        createTask(instance, first, starter);
        return instance.getId();
    }

    /**
     * 审批通过。
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long taskId, String comment) {
        FlowTask task = mustLoadPendingTask(taskId);
        checkAssignee(task);

        // 完成当前任务
        FlowTask update = new FlowTask();
        update.setId(task.getId());
        update.setStatus(Constants.TASK_DONE);
        update.setComment(comment);
        update.setActionUserId(SecurityUtils.getCurrentUserId());
        update.setActionUserName(SecurityUtils.getLoginUser().getNickname());
        update.setActionTime(LocalDateTime.now());
        taskMapper.updateById(update);

        // 查找下一节点
        List<FlowNode> nodes = nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getFlowId, task.getFlowId()).orderByAsc(FlowNode::getSort));
        FlowNode next = nodes.stream()
                .filter(n -> n.getSort() != null && n.getSort() > task.getNodeSort())
                .min(Comparator.comparingInt(FlowNode::getSort))
                .orElse(null);

        FlowInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (next == null) {
            // 流程结束
            instance.setStatus(Constants.FLOW_DONE);
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName("已结束");
            eventPublisher.publishEvent(new FlowCompletedEvent(this, instance.getBusinessType(),
                    instance.getBusinessId(), instance.getId(), true));
        } else {
            SysUser starter = userMapper.selectById(instance.getStartUserId());
            createTask(instance, next, starter);
            instance.setCurrentNodeId(next.getId());
            instance.setCurrentNodeName(next.getNodeName());
        }
        instanceMapper.updateById(instance);
    }

    /**
     * 驳回（终止流程）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, String comment) {
        FlowTask task = mustLoadPendingTask(taskId);
        checkAssignee(task);
        FlowTask update = new FlowTask();
        update.setId(task.getId());
        update.setStatus(Constants.TASK_REJECTED);
        update.setComment(comment);
        update.setActionUserId(SecurityUtils.getCurrentUserId());
        update.setActionUserName(SecurityUtils.getLoginUser().getNickname());
        update.setActionTime(LocalDateTime.now());
        taskMapper.updateById(update);

        FlowInstance instance = instanceMapper.selectById(task.getInstanceId());
        instance.setStatus(Constants.FLOW_TERMINATED);
        instance.setCurrentNodeName("已驳回");
        instanceMapper.updateById(instance);
        eventPublisher.publishEvent(new FlowCompletedEvent(this, instance.getBusinessType(),
                instance.getBusinessId(), instance.getId(), false));
    }

    /**
     * 转办。
     */
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long taskId, Long toUserId, String comment) {
        FlowTask task = mustLoadPendingTask(taskId);
        checkAssignee(task);
        if (toUserId == null) {
            throw new BusinessException("转办对象不能为空");
        }
        SysUser target = userMapper.selectById(toUserId);
        if (target == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "转办用户不存在");
        }
        FlowTask update = new FlowTask();
        update.setId(task.getId());
        update.setStatus(Constants.TASK_TRANSFERRED);
        update.setComment(comment);
        update.setActionUserId(SecurityUtils.getCurrentUserId());
        update.setActionUserName(SecurityUtils.getLoginUser().getNickname());
        update.setActionTime(LocalDateTime.now());
        taskMapper.updateById(update);

        FlowInstance instance = instanceMapper.selectById(task.getInstanceId());
        // 重新创建同节点任务给目标人
        FlowTask newTask = baseTask(instance, task.getNodeName(), task.getNodeSort(), toUserId, target.getNickname());
        taskMapper.insert(newTask);
    }

    /* ===================== 工作台查询 ===================== */

    /** 我的待办 */
    public List<FlowTask> myTodo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return taskMapper.selectList(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getAssignee, userId)
                .eq(FlowTask::getStatus, Constants.TASK_PENDING)
                .orderByDesc(FlowTask::getId));
    }

    /** 我的已办 */
    public List<FlowTask> myDone() {
        Long userId = SecurityUtils.getCurrentUserId();
        return taskMapper.selectList(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getActionUserId, userId)
                .in(FlowTask::getStatus, Constants.TASK_DONE, Constants.TASK_REJECTED, Constants.TASK_TRANSFERRED)
                .orderByDesc(FlowTask::getId));
    }

    /** 我的发起 */
    public List<FlowInstance> myInitiated() {
        Long userId = SecurityUtils.getCurrentUserId();
        return instanceMapper.selectList(new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getStartUserId, userId)
                .orderByDesc(FlowInstance::getId));
    }

    /** 流程实例详情（含任务轨迹） */
    public FlowInstance getInstance(Long id) {
        FlowInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return instance;
    }

    /** 业务对应的实例 */
    public FlowInstance getByBusiness(String businessType, Long businessId) {
        return instanceMapper.selectOne(new LambdaQueryWrapper<FlowInstance>()
                .eq(FlowInstance::getBusinessType, businessType)
                .eq(FlowInstance::getBusinessId, businessId)
                .orderByDesc(FlowInstance::getId).last("LIMIT 1"));
    }

    /** 当前业务的待办任务 */
    public List<FlowTask> tasksOfBusiness(String businessType, Long businessId) {
        return taskMapper.selectList(new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getBusinessType, businessType)
                .eq(FlowTask::getBusinessId, businessId)
                .orderByAsc(FlowTask::getId));
    }

    /* ===================== 内部方法 ===================== */

    private void createTask(FlowInstance instance, FlowNode node, SysUser starter) {
        Long assignee = resolveAssignee(node, starter);
        SysUser assigneeUser = userMapper.selectById(assignee);
        FlowTask task = baseTask(instance, node.getNodeName(), node.getSort(), assignee,
                assigneeUser == null ? null : assigneeUser.getNickname());
        taskMapper.insert(task);
    }

    private FlowTask baseTask(FlowInstance instance, String nodeName, Integer nodeSort, Long assignee, String assigneeName) {
        FlowTask task = new FlowTask();
        task.setInstanceId(instance.getId());
        task.setFlowId(instance.getFlowId());
        task.setFlowKey(instance.getFlowKey());
        task.setBusinessType(instance.getBusinessType());
        task.setBusinessId(instance.getBusinessId());
        task.setTitle(instance.getTitle());
        task.setNodeName(nodeName);
        task.setNodeSort(nodeSort);
        task.setAssignee(assignee);
        task.setAssigneeName(assigneeName);
        task.setStartUserId(instance.getStartUserId());
        task.setStatus(Constants.TASK_PENDING);
        return task;
    }

    private Long resolveAssignee(FlowNode node, SysUser starter) {
        String type = node.getApproverType() == null ? "user" : node.getApproverType();
        Long assignee = switch (type) {
            case "role" -> {
                Long uid = userMapper.selectUserIdByRoleKey(node.getApproverValue());
                yield uid != null ? uid : Constants.SUPER_ADMIN_ID;
            }
            case "initiator" -> starter == null ? Constants.SUPER_ADMIN_ID : starter.getId();
            default -> {
                if (node.getApproverValue() == null) {
                    yield Constants.SUPER_ADMIN_ID;
                }
                yield Long.valueOf(node.getApproverValue());
            }
        };
        return assignee;
    }

    private FlowTask mustLoadPendingTask(Long taskId) {
        FlowTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "任务不存在");
        }
        if (!Constants.TASK_PENDING.equals(task.getStatus())) {
            throw new BusinessException("任务已办理，请勿重复提交");
        }
        return task;
    }

    private void checkAssignee(FlowTask task) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !userId.equals(task.getAssignee())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
