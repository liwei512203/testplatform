package com.example.testplatform.controller;

import com.example.testplatform.common.Result;
import com.example.testplatform.entity.Project;
import com.example.testplatform.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 项目信息接口
 */
@Tag(name = "项目管理", description = "项目信息的新增、修改、删除与查询")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 项目列表
     */
    @Operation(summary = "项目列表", description = "可按关键字、测试团队过滤")
    @GetMapping
    public Result<List<Project>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String testTeam) {
        return Result.ok(projectService.list(keyword, testTeam));
    }

    /**
     * 项目详情
     */
    @Operation(summary = "项目详情")
    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable String id) {
        return Result.ok(projectService.getById(id));
    }

    /**
     * 新增项目
     */
    @Operation(summary = "新增项目")
    @PostMapping
    public Result<Project> create(@Valid @RequestBody Project project) {
        return Result.ok(projectService.create(project));
    }

    /**
     * 修改项目
     */
    @Operation(summary = "修改项目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @Valid @RequestBody Project project) {
        project.setId(id);
        projectService.update(project);
        return Result.ok();
    }

    /**
     * 删除项目（级联删除项目下用例）
     */
    @Operation(summary = "删除项目", description = "级联删除项目下用例")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        projectService.delete(id);
        return Result.ok();
    }
}
