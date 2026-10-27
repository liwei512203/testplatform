package com.example.testplatform.controller;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.common.Result;
import com.example.testplatform.entity.VersionPlan;
import com.example.testplatform.service.VersionPlanService;
import com.example.testplatform.vo.PlanCaseVO;
import com.example.testplatform.vo.VersionPlanVO;
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
import java.util.Map;

/**
 * 版本计划接口
 */
@Tag(name = "版本计划管理", description = "版本计划的创建、修改、删除、用例关联与一键执行")
@RestController
@RequestMapping("/api/plans")
public class VersionPlanController {

    private final VersionPlanService versionPlanService;

    @Autowired
    public VersionPlanController(VersionPlanService versionPlanService) {
        this.versionPlanService = versionPlanService;
    }

    /**
     * 分页查询某项目下的版本计划
     */
    @Operation(summary = "分页查询版本计划", description = "查询某项目下的版本计划")
    @GetMapping
    public Result<PageResult<VersionPlanVO>> page(@RequestParam String projectId,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(versionPlanService.page(projectId, pageNum, pageSize));
    }

    @Operation(summary = "新建版本计划", description = "自动继承项目下全部用例")
    @PostMapping
    public Result<VersionPlan> create(@Valid @RequestBody VersionPlan plan) {
        return Result.ok(versionPlanService.create(plan));
    }

    @Operation(summary = "修改版本计划")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody VersionPlan plan) {
        plan.setId(id);
        versionPlanService.update(plan);
        return Result.ok();
    }

    @Operation(summary = "删除版本计划", description = "级联删除关联用例")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        versionPlanService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查询计划关联用例", description = "分页查询计划下关联的用例（含执行结果/执行时间）")
    @GetMapping("/{id}/cases")
    public Result<PageResult<PlanCaseVO>> pageCases(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(versionPlanService.pageCases(id, pageNum, pageSize));
    }

    @Operation(summary = "一键执行计划", description = "模拟计划下用例执行结果（随机成功/失败）；execType 可选（手动执行/定时任务执行/流水线触发执行），缺省使用计划自身的执行方式")
    @PostMapping("/{id}/execute")
    public Result<Map<String, Long>> execute(@PathVariable Long id,
                                             @RequestParam(required = false) String execType) {
        return Result.ok(versionPlanService.execute(id, execType));
    }
}
