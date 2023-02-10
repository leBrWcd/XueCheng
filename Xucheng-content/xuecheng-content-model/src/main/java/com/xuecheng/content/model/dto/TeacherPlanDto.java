package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Description 课程计划树形表
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/10
 */
@Data
@ToString
public class TeacherPlanDto extends Teachplan {

    /**
     * 子节点,小节
     */
    List<TeacherPlanDto> teachPlanTreeNodes;
    /**
     * 媒资信息
     */
    TeachplanMedia teachplanMedia;

}
