package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CategoryListDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author Lebrwcd
 * @since 2023-02-09
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CategoryListDto> treeNode(String id);
}
