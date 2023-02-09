package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Description 课程分类查询父节点
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/9
 */
@Data
public class CategoryListDto extends CourseCategory implements Serializable {

    private List<CourseCategory> childrenTreeNodes;

}
