package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Description 课程基本信息服务接口
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/7
 */
public interface CourseBaseInfoService {

    /**
     * @param params
     * @param queryCourseParamsDto
     * @return com.xuecheng.content.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @description 课程基本信息查询接口
     * @author Lebr7Wcd
     * @date 2023/2/7 20:20
     */
    PageResult<CourseBase> list(PageParams params,
                                @RequestBody QueryCourseParamsDto queryCourseParamsDto);


    /**
     * 添加课程基本信息，包括课程营销信息
     *
     * @param dto
     * @return
     */
    CourseBaseInfoDto addCourseBase(Long companyId, AddCourseDto dto);

    /**
     * 根据id获取课程基本信息
     *
     * @param courseId 课程id
     * @return
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程基本信息包括修改课程营销信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    CourseBaseInfoDto editCourseBase(Long companyId, EditCourseDto editCourseDto);
}
