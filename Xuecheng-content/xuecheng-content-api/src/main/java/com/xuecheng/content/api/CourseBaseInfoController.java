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
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    /**
     * 查询课程列表
     * @param params 分页参数
     * @param queryCourseParamsDto 数据传输对象
     * @return
     */
    @ApiOperation("课程查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        log.info("======课程查询传递参数:====== {}", queryCourseParamsDto.toString());
        return courseBaseInfoService.list(params, queryCourseParamsDto);
    }

    /**
     * 新增课程基本信息
     * @param addCourseDto 新增课程数据传输对象
     * @return 课程基本信息
     */
    @ApiOperation("新增课程基础信息")
    @PostMapping
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidatedGroup.Insert.class)
                                                          AddCourseDto addCourseDto) {
        // TODO 机构ID先写死
        return courseBaseInfoService.addCourseBase(111L,addCourseDto);
    }

    /**
     * 根据课程id获得课程基本信息包括课程营销信息
     * @param id 课程id
     * @return
     */
    @GetMapping("{id}")
    public CourseBaseInfoDto getCourseById(@PathVariable Long id) {
        return courseBaseInfoService.getCourseBaseInfo(id);
    }

    /**
     * 修改课程信息包括修改课程营销信息
     * @param editCourseDto
     * @return
     */
    @PutMapping
    public CourseBaseInfoDto editCourseBase(@RequestBody EditCourseDto editCourseDto) {
        return courseBaseInfoService.editCourseBase(111L,editCourseDto);
    }


}
