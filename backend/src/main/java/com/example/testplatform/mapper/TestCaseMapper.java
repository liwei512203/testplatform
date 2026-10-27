package com.example.testplatform.mapper;

import com.example.testplatform.entity.TestCase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用例 Mapper
 */
public interface TestCaseMapper {

    /**
     * 分页查询某项目下的用例
     */
    List<TestCase> selectByProjectId(@Param("projectId") String projectId,
                                     @Param("keyword") String keyword,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    /**
     * 统计某项目下的用例数量
     */
    long countByProjectId(@Param("projectId") String projectId, @Param("keyword") String keyword);

    /**
     * 按ID查询
     */
    TestCase selectById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(TestCase testCase);

    /**
     * 更新
     */
    int update(TestCase testCase);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按项目删除全部用例
     */
    int deleteByProjectId(@Param("projectId") String projectId);
}
