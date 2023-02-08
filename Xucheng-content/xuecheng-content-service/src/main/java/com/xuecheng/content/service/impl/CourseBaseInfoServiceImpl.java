package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.base.model.PageParams;
import com.xuecheng.content.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description 课程基本信息查询接口实现类
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/7
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> list(PageParams params, QueryCourseParamsDto dto) {
        // 1.创建分页对象
        Page<CourseBase> basePage = new Page<>(params.getPageNo(),params.getPageSize());
        // 2.组装查询对象器
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dto.getCourseName() != "",CourseBase::getName,dto.getCourseName())
                .eq(dto.getAuditStatus() != "",CourseBase::getAuditStatus,dto.getAuditStatus())
                .eq(dto.getPublishStatus() != null,CourseBase::getStatus,dto.getPublishStatus());
        // 3.分页查询
        basePage = courseBaseMapper.selectPage(basePage,wrapper);
        if (basePage.getTotal() >= 1L) {
            List<CourseBase> records = basePage.getRecords();
            PageResult<CourseBase> pageResult =
                    new PageResult<CourseBase>(records,basePage.getTotal(), params.getPageNo(),params.getPageSize());
            return pageResult;
        }
        return null;
    }
}
