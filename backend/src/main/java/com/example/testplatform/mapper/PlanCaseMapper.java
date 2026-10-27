package com.example.testplatform.mapper;

import com.example.testplatform.vo.PlanCaseVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 版本计划关联用例 Mapper
 */
public interface PlanCaseMapper {

    /**
     * 新建计划时快照项目下全部用例（plan_date 为计划日期，分区键）
     */
    int insertSnapshot(@Param("planId") Long planId,
                       @Param("projectId") String projectId,
                       @Param("planDate") LocalDate planDate);

    /**
     * 分页查询计划下的用例（附带用例名称/内容）
     */
    List<PlanCaseVO> selectPageByPlanId(@Param("planId") Long planId,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    /**
     * 统计计划下的用例数量
     */
    long countByPlanId(@Param("planId") Long planId);

    /**
     * 统计计划下指定结果的数量
     */
    long countByPlanIdAndResult(@Param("planId") Long planId, @Param("result") String result);

    /**
     * 一键执行：随机分配成功/失败，执行时间与计划创建时间一致，记录执行方式
     */
    int executeByPlanId(@Param("planId") Long planId,
                        @Param("executeTime") Long executeTime,
                        @Param("execType") String execType);

    /**
     * 计划时间变更后，同步已执行用例的执行时间（保持与计划时间一致）
     */
    int updateExecuteTimeByPlanId(@Param("planId") Long planId, @Param("executeTime") Long executeTime);

    /**
     * 计划时间变更后，同步全部关联用例的计划日期（保证分区归位正确）
     */
    int updatePlanDateByPlanId(@Param("planId") Long planId, @Param("planDate") LocalDate planDate);

    /**
     * 按项目删除关联用例（连表删除该项目下所有计划的用例）
     */
    int deleteByProjectId(@Param("projectId") String projectId);

    /**
     * 按计划删除关联用例
     */
    int deleteByPlanId(@Param("planId") Long planId);
}
