package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CategoryListDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author Lebrwcd
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    /**
     *
     * @param id
     * @return
     */
    List<CategoryListDto> treeNodes(String id);
}
