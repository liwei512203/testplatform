package com.example.testplatform.mapper;

import com.example.testplatform.entity.VersionPlan;
import com.example.testplatform.vo.VersionPlanVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 版本计划 Mapper
 */
public interface VersionPlanMapper {

    /**
     * 分页查询某项目下的版本计划（附带执行统计）
     */
    List<VersionPlanVO> selectPageByProjectId(@Param("projectId") String projectId,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

    /**
     * 统计某项目下的版本计划数量
     */
    long countByProjectId(@Param("projectId") String projectId);

    /**
     * 按ID查询
     */
    VersionPlan selectById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(VersionPlan plan);

    /**
     * 更新
     */
    int update(VersionPlan plan);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按项目删除全部版本计划（项目删除时调用）
     */
    int deleteByProjectId(@Param("projectId") String projectId);
}
