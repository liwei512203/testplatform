package com.example.testplatform.service.impl;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.entity.PlanCase;
import com.example.testplatform.entity.VersionPlan;
import com.example.testplatform.enums.ExecType;
import com.example.testplatform.exception.BizException;
import com.example.testplatform.mapper.CaseExecutionLogMapper;
import com.example.testplatform.mapper.ExecTypeMapper;
import com.example.testplatform.mapper.PlanCaseMapper;
import com.example.testplatform.mapper.ProjectMapper;
import com.example.testplatform.mapper.VersionPlanMapper;
import com.example.testplatform.service.VersionPlanService;
import com.example.testplatform.vo.PlanCaseVO;
import com.example.testplatform.vo.VersionPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 版本计划服务实现
 */
@Service
public class VersionPlanServiceImpl implements VersionPlanService {

    private final VersionPlanMapper versionPlanMapper;

    private final PlanCaseMapper planCaseMapper;

    private final ProjectMapper projectMapper;

    private final CaseExecutionLogMapper executionLogMapper;

    private final ExecTypeMapper execTypeMapper;

    @Autowired
    public VersionPlanServiceImpl(VersionPlanMapper versionPlanMapper,
                                  PlanCaseMapper planCaseMapper,
                                  ProjectMapper projectMapper,
                                  CaseExecutionLogMapper executionLogMapper,
                                  ExecTypeMapper execTypeMapper) {
        this.versionPlanMapper = versionPlanMapper;
        this.planCaseMapper = planCaseMapper;
        this.projectMapper = projectMapper;
        this.executionLogMapper = executionLogMapper;
        this.execTypeMapper = execTypeMapper;
    }

    @Override
    public PageResult<VersionPlanVO> page(String projectId, int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 200) {
            pageSize = 200;
        }
        int offset = (pageNum - 1) * pageSize;
        long total = versionPlanMapper.countByProjectId(projectId);
        List<VersionPlanVO> list = versionPlanMapper.selectPageByProjectId(projectId, offset, pageSize);
        return new PageResult<>(total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VersionPlan create(VersionPlan plan) {
        checkProjectExists(plan.getProjectId());
        if (plan.getCreateTime() == null || plan.getCreateTime() <= 0) {
            throw new BizException("计划时间不能为空");
        }
        plan.setUpdateTime(System.currentTimeMillis());
        versionPlanMapper.insert(plan);
        // 继承项目下全部测试用例（快照），plan_date 用于分区归位
        planCaseMapper.insertSnapshot(plan.getId(), plan.getProjectId(),
                toLocalDate(plan.getCreateTime()));
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(VersionPlan plan) {
        VersionPlan exists = getById(plan.getId());
        if (StringUtils.hasText(plan.getProjectId())
                && !exists.getProjectId().equals(plan.getProjectId())) {
            throw new BizException("不允许修改版本计划所属项目");
        }
        if (plan.getCreateTime() != null && plan.getCreateTime() <= 0) {
            throw new BizException("计划时间不合法");
        }
        plan.setUpdateTime(System.currentTimeMillis());
        versionPlanMapper.update(plan);
        // 计划时间变更：已执行用例的执行时间保持与计划时间一致，并重算新旧两天的统计
        Long oldTime = exists.getCreateTime();
        Long newTime = plan.getCreateTime();
        if (newTime != null && !newTime.equals(oldTime)) {
            // 计划日期同步（分区归位）+ 已执行用例执行时间同步 + 执行事实归期。
            // 统计表由每日跑批重算，不实时维护（新旧两日统计最迟下一轮跑批自动修正）
            planCaseMapper.updatePlanDateByPlanId(plan.getId(), toLocalDate(newTime));
            planCaseMapper.updateExecuteTimeByPlanId(plan.getId(), newTime);
            executionLogMapper.updateTimeByPlanId(plan.getId(), newTime, toLocalDate(newTime));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        VersionPlan plan = getById(id);
        planCaseMapper.deleteByPlanId(id);
        executionLogMapper.deleteByPlanId(id);
        versionPlanMapper.deleteById(id);
        // 统计表由每日跑批重算，不实时维护
    }

    @Override
    public PageResult<PlanCaseVO> pageCases(Long planId, int pageNum, int pageSize) {
        getById(planId);
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 200) {
            pageSize = 200;
        }
        int offset = (pageNum - 1) * pageSize;
        long total = planCaseMapper.countByPlanId(planId);
        List<PlanCaseVO> list = planCaseMapper.selectPageByPlanId(planId, offset, pageSize);
        return new PageResult<>(total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Long> execute(Long planId, String execType) {
        VersionPlan plan = getById(planId);
        // 执行方式是"每次执行"的属性：由触发方传入，缺省视为页面手动点击
        String actualType = StringUtils.hasText(execType) ? execType : ExecType.MANUAL.getName();
        checkExecType(actualType);
        long total = planCaseMapper.countByPlanId(planId);
        if (total == 0) {
            throw new BizException("该版本计划下没有测试用例，无法执行");
        }
        // 执行时间与版本计划创建时间保持一致
        planCaseMapper.executeByPlanId(planId, plan.getCreateTime(), actualType);
        // 追加执行事实：本次执行对每条用例记一行日志（携带执行方式），重复执行自然累计次数
        executionLogMapper.insertByPlanId(planId, plan.getCreateTime(),
                toLocalDate(plan.getCreateTime()), actualType);
        // 更新计划记录时间
        VersionPlan update = new VersionPlan();
        update.setId(planId);
        update.setUpdateTime(System.currentTimeMillis());
        versionPlanMapper.update(update);
        // 统计表由每日跑批统一重算，执行路径不再实时维护报表数据

        Map<String, Long> stats = new HashMap<>(4);
        stats.put("total", total);
        stats.put("success", planCaseMapper.countByPlanIdAndResult(planId, PlanCase.RESULT_SUCCESS));
        stats.put("fail", planCaseMapper.countByPlanIdAndResult(planId, PlanCase.RESULT_FAIL));
        return stats;
    }

    /**
     * 毫秒时间戳 -> LocalDate（系统时区）
     */
    private java.time.LocalDate toLocalDate(Long timeMs) {
        return Instant.ofEpochMilli(timeMs).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private VersionPlan getById(Long id) {
        VersionPlan plan = versionPlanMapper.selectById(id);
        if (plan == null) {
            throw new BizException(404, "版本计划不存在：" + id);
        }
        return plan;
    }

    /**
     * 执行方式值域校验：查字典表，新增执行方式无需改代码
     */
    private void checkExecType(String execType) {
        if (StringUtils.hasText(execType) && !execTypeMapper.selectAll().contains(execType)) {
            throw new BizException("执行方式取值不合法：" + execType);
        }
    }

    private void checkProjectExists(String projectId) {
        if (!StringUtils.hasText(projectId) || projectMapper.selectById(projectId) == null) {
            throw new BizException("所属项目不存在：" + projectId);
        }
    }
}
