package com.xuecheng.content.api;

import com.xuecheng.content.base.model.PageParams;
import com.xuecheng.content.base.model.PageResult;
import com.xuecheng.content.base.validated.ValidatedGroup;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/7 16:22
 */
@Api(value = "课程管理接口", tags = "课程管理接口")
@RestController
@Slf4j
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        log.info("======课程查询传递参数:====== {}", queryCourseParamsDto.toString());
        return courseBaseInfoService.list(params, queryCourseParamsDto);
    }

    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidatedGroup.Insert.class)
                                                          AddCourseDto addCourseDto) {
        // TODO 公司ID先写死
        return courseBaseInfoService.addCourseBase(111L,addCourseDto);
    }

    @GetMapping("/course/{id}")
    public CourseBaseInfoDto getCourseById(@PathVariable Long id) {
        return courseBaseInfoService.getCourseBaseInfo(id);
    }

    @PutMapping("/course")
    public CourseBaseInfoDto editCourseBase(@RequestBody EditCourseDto editCourseDto) {
        return courseBaseInfoService.editCourseBase(111L,editCourseDto);
    }


}
