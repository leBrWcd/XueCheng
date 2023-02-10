package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeacherPlanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author Lebrwcd
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeacherPlanDto> treeNodes(Long courseId);
}
