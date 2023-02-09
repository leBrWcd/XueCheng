package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.base.execption.XuechengException;
import com.xuecheng.content.base.model.PageParams;
import com.xuecheng.content.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper categoryMapper;

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

    @Override
    public CourseBaseInfoDto addCourseBase(Long companyId,AddCourseDto dto) {
        // 合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XuechengException("课程名称为空");
        }
        if (StringUtils.isBlank(dto.getMt())) {
            throw new XuechengException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getSt())) {
            throw new XuechengException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XuechengException("课程等级为空");
        }
        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XuechengException("教育模式为空");
        }
        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XuechengException("适应人群为空");
        }
        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XuechengException("收费规则为空");
        }

        // 新增课程
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto,courseBase);
        // 设置课程初始状态
        // 审核状态：未提交  发布状态：未发布
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        // 新增
        int insert1 = courseBaseMapper.insert(courseBase);

        // 新增课程营销信息
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        courseMarket.setId(courseId);
        BeanUtils.copyProperties(dto,courseMarket);
        // 收费课程必须写价格且价格大于0 201000:"免费"  201001:"收费"
        String charge = dto.getCharge();
        if ("201001".equals(charge)) {
            BigDecimal price = dto.getPrice();
            if (price == null || price.floatValue() <= 0) {
                throw new XuechengException("课程设置了收费价格不能为空且必须大于0");
            }
        }
        // 新增
        int insert2 = courseMarketMapper.insert(courseMarket);

        if (insert1 <= 0 || insert2 <= 0) {
            throw new RuntimeException("新增课程失败");
        }
        // 添加成功
        return getCourseBaseInfo(courseId);
    }

    /**
     * 根据课程id获取课程基本信息包括课程的营销信息
     * @param courseId
     * @return
     */
    private CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        // 有该课程
        CourseBaseInfoDto baseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,baseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket,baseInfoDto);
        }

        // 分类名称，数据库中的是数据字典，而不是对应的字符串
        String mName = categoryMapper.selectById(courseBase.getMt()).getName();
        baseInfoDto.setMtName(mName);
        String sName = categoryMapper.selectById(courseBase.getSt()).getName();
        baseInfoDto.setStName(sName);

        return baseInfoDto;
    }
}
