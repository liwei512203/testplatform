package com.example.testplatform.service.impl;

import com.example.testplatform.entity.Project;
import com.example.testplatform.enums.TestTeam;
import com.example.testplatform.exception.BizException;
import com.example.testplatform.mapper.CaseExecutionLogMapper;
import com.example.testplatform.mapper.DailyStatMapper;
import com.example.testplatform.mapper.PlanCaseMapper;
import com.example.testplatform.mapper.ProjectMapper;
import com.example.testplatform.mapper.TestCaseMapper;
import com.example.testplatform.mapper.VersionPlanMapper;
import com.example.testplatform.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * 项目信息服务实现
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    private final TestCaseMapper testCaseMapper;

    private final VersionPlanMapper versionPlanMapper;

    private final PlanCaseMapper planCaseMapper;

    private final DailyStatMapper dailyStatMapper;

    private final CaseExecutionLogMapper executionLogMapper;

    @Autowired
    public ProjectServiceImpl(ProjectMapper projectMapper,
                              TestCaseMapper testCaseMapper,
                              VersionPlanMapper versionPlanMapper,
                              PlanCaseMapper planCaseMapper,
                              DailyStatMapper dailyStatMapper,
                              CaseExecutionLogMapper executionLogMapper) {
        this.projectMapper = projectMapper;
        this.testCaseMapper = testCaseMapper;
        this.versionPlanMapper = versionPlanMapper;
        this.planCaseMapper = planCaseMapper;
        this.dailyStatMapper = dailyStatMapper;
        this.executionLogMapper = executionLogMapper;
    }

    @Override
    public List<Project> list(String keyword, String testTeam) {
        if (StringUtils.hasText(testTeam) && !TestTeam.contains(testTeam)) {
            throw new BizException("测试团队取值不合法，仅支持：" + TestTeam.TIP);
        }
        return projectMapper.selectAll(keyword, testTeam);
    }

    @Override
    public Project getById(String id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(404, "项目不存在：" + id);
        }
        return project;
    }

    @Override
    public Project create(Project project) {
        checkNameDuplicate(project.getName(), null);
        checkTestTeam(project.getTestTeam());
        project.setId(UUID.randomUUID().toString());
        projectMapper.insert(project);
        return project;
    }

    @Override
    public void update(Project project) {
        Project exists = getById(project.getId());
        if (StringUtils.hasText(project.getName())) {
            checkNameDuplicate(project.getName(), project.getId());
        }
        checkTestTeam(project.getTestTeam());
        projectMapper.update(project);
        // 测试团队变更时同步两张统计表中的冗余团队字段
        if (StringUtils.hasText(project.getTestTeam())
                && !project.getTestTeam().equals(exists.getTestTeam())) {
            dailyStatMapper.updateTeamByProject(project.getId(), project.getTestTeam());
            dailyStatMapper.updateCaseTeamByProject(project.getId(), project.getTestTeam());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        getById(id);
        testCaseMapper.deleteByProjectId(id);
        planCaseMapper.deleteByProjectId(id);
        executionLogMapper.deleteByProjectId(id);
        versionPlanMapper.deleteByProjectId(id);
        dailyStatMapper.deleteByProject(id);
        dailyStatMapper.deleteCaseByProject(id);
        projectMapper.deleteById(id);
    }

    private void checkNameDuplicate(String name, String excludeId) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        List<Project> projects = projectMapper.selectAll(null, null);
        for (Project p : projects) {
            if (name.equals(p.getName()) && !p.getId().equals(excludeId)) {
                throw new BizException("项目名称已存在：" + name);
            }
        }
    }

    private void checkTestTeam(String testTeam) {
        if (StringUtils.hasText(testTeam) && !TestTeam.contains(testTeam)) {
            throw new BizException("测试团队取值不合法，仅支持：" + TestTeam.TIP);
        }
    }
}
