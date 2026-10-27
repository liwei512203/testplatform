package com.example.testplatform.service.impl;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.entity.TestCase;
import com.example.testplatform.exception.BizException;
import com.example.testplatform.mapper.CaseExecutionLogMapper;
import com.example.testplatform.mapper.DailyStatMapper;
import com.example.testplatform.mapper.ProjectMapper;
import com.example.testplatform.mapper.TestCaseMapper;
import com.example.testplatform.service.TestCaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 测试用例服务实现
 */
@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseMapper testCaseMapper;

    private final ProjectMapper projectMapper;

    private final DailyStatMapper dailyStatMapper;

    private final CaseExecutionLogMapper executionLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TestCaseServiceImpl(TestCaseMapper testCaseMapper, ProjectMapper projectMapper,
                               DailyStatMapper dailyStatMapper,
                               CaseExecutionLogMapper executionLogMapper) {
        this.testCaseMapper = testCaseMapper;
        this.projectMapper = projectMapper;
        this.dailyStatMapper = dailyStatMapper;
        this.executionLogMapper = executionLogMapper;
    }

    @Override
    public PageResult<TestCase> page(String projectId, String keyword, int pageNum, int pageSize) {
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
        long total = testCaseMapper.countByProjectId(projectId, keyword);
        List<TestCase> list = testCaseMapper.selectByProjectId(projectId, keyword, offset, pageSize);
        return new PageResult<>(total, list);
    }

    @Override
    public TestCase getById(Long id) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new BizException(404, "用例不存在：" + id);
        }
        return testCase;
    }

    @Override
    public TestCase create(TestCase testCase) {
        checkProjectExists(testCase.getProjectId());
        checkContentJson(testCase.getContent());
        long now = System.currentTimeMillis();
        testCase.setCreateTime(now);
        testCase.setUpdateTime(now);
        testCaseMapper.insert(testCase);
        return testCase;
    }

    @Override
    public void update(TestCase testCase) {
        TestCase exists = getById(testCase.getId());
        if (StringUtils.hasText(testCase.getProjectId()) && !exists.getProjectId().equals(testCase.getProjectId())) {
            checkProjectExists(testCase.getProjectId());
        }
        checkContentJson(testCase.getContent());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCaseMapper.update(testCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        testCaseMapper.deleteById(id);
        // 同步清理该用例的执行事实与用例日统计（避免统计复活或悬空）
        executionLogMapper.deleteByCaseId(id);
        dailyStatMapper.deleteCaseStatByCaseId(id);
    }

    private void checkProjectExists(String projectId) {
        if (!StringUtils.hasText(projectId) || projectMapper.selectById(projectId) == null) {
            throw new BizException("所属项目不存在：" + projectId);
        }
    }

    /**
     * 校验 content 是否为合法 JSON
     */
    private void checkContentJson(String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        try {
            objectMapper.readTree(content);
        } catch (Exception e) {
            throw new BizException("用例内容必须是合法的 JSON 格式");
        }
    }
}
