package com.example.testplatform.service;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.entity.VersionPlan;
import com.example.testplatform.vo.PlanCaseVO;
import com.example.testplatform.vo.VersionPlanVO;

import java.util.Map;

/**
 * 版本计划服务
 */
public interface VersionPlanService {

    /**
     * 分页查询某项目下的版本计划（含执行统计）
     */
    PageResult<VersionPlanVO> page(String projectId, int pageNum, int pageSize);

    /**
     * 新建版本计划：快照项目下全部测试用例
     */
    VersionPlan create(VersionPlan plan);

    /**
     * 修改版本计划（名称/计划时间）
     */
    void update(VersionPlan plan);

    /**
     * 删除版本计划（级联删除关联用例）
     */
    void delete(Long id);

    /**
     * 分页查询计划下关联的用例
     */
    PageResult<PlanCaseVO> pageCases(Long planId, int pageNum, int pageSize);

    /**
     * 一键执行：模拟计划下用例执行结果（随机成功/失败），
     * 执行时间与版本计划创建时间保持一致。
     * execType 为本次执行方式（手动/定时任务/流水线触发），为空时使用计划自身的执行方式
     *
     * @return total/success/fail 统计
     */
    Map<String, Long> execute(Long planId, String execType);
}
