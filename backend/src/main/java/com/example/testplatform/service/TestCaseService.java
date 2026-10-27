package com.example.testplatform.service;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.entity.TestCase;

/**
 * 测试用例服务
 */
public interface TestCaseService {

    /**
     * 分页查询用例
     */
    PageResult<TestCase> page(String projectId, String keyword, int pageNum, int pageSize);

    /**
     * 用例详情
     */
    TestCase getById(Long id);

    /**
     * 新增用例
     */
    TestCase create(TestCase testCase);

    /**
     * 更新用例
     */
    void update(TestCase testCase);

    /**
     * 删除用例
     */
    void delete(Long id);
}
