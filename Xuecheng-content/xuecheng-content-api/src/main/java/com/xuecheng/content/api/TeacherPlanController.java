package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveOrUpdateTeachPlanDto;
import com.xuecheng.content.model.dto.TeacherPlanDto;
import com.xuecheng.content.service.TeacherPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description 课程计划编辑接口
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/10
 */
@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@Slf4j
@RequestMapping("/teachplan")
public class TeacherPlanController {

    @Autowired
    private TeacherPlanService teacherPlanService;

    /**
     * 查询课程计划-树形结构
     * @param courseId
     * @return
     */
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType
            = "Long",paramType = "path")
    @GetMapping("/{id}/tree-nodes")
    public List<TeacherPlanDto> tree(@PathVariable("id") Long courseId) {
        return teacherPlanService.treeNodes(courseId);
    }

    /**
     * 新增或更新课程计划
     * @param dto
     */
    @PostMapping
    public void SaveOrUpdate(@RequestBody SaveOrUpdateTeachPlanDto dto) {
        teacherPlanService.saveOrToUpdate(dto);
    }

    /**
     * 删除课程计划 TODO: 删除章节前端有bug，同时后续考虑删除后 orderby 变量是否修改
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public String DeleteTeachPlan(@PathVariable("id") Long id) {
        return teacherPlanService.removeTeachPlan(id);
    }

}
