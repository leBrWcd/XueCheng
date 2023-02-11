package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveOrUpdateTeachPlanDto;
import com.xuecheng.content.model.dto.TeacherPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeacherPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description 课程计划业务实现
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/10
 */
@Service
@Slf4j
public class TeacherPlanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeacherPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeacherPlanDto> treeNodes(Long courseId) {
        List<TeacherPlanDto> list = teachplanMapper.treeNodes(courseId);
        return list;
    }

    @Override
    public void saveOrToUpdate(SaveOrUpdateTeachPlanDto dto) {
        Long id = dto.getId();
        // id不为空表示更新操作,为空表示新增操作
        if (id != null) {
            // 更新
            Teachplan teachplan = baseMapper.selectById(id);
            BeanUtils.copyProperties(dto,teachplan);
            baseMapper.updateById(teachplan);
        } else {
            // 新增
            Teachplan teachplan = new Teachplan();
            // 需要判断该课程计划的同父级别下有多少,有多少就排在它们后面
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(dto.getCourseId() != null,Teachplan::getCourseId,dto.getCourseId())
                    .eq(dto.getParentid() != null,Teachplan::getParentid,dto.getParentid());
            Integer count = baseMapper.selectCount(wrapper);
            BeanUtils.copyProperties(dto,teachplan);
            teachplan.setOrderby(count + 1);
            teachplan.setCreateDate(LocalDateTime.now());
            baseMapper.insert(teachplan);
        }
    }

    @Override
    public String removeTeachPlan(Long id) {
        // 需要判断删除的是小节还是章节，如果是章节，需要先删除其下面所有的小节
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getParentid() == 0) {
            // 章节
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getParentid,id);
            List<Teachplan> teachplans = baseMapper.selectList(wrapper);
            List<Long> idList = teachplans.stream().map( e -> {
                return e.getId();
            }).collect(Collectors.toList());
            log.info("idList ===== {}" ,idList.size());
            return baseMapper.deleteBatchIds(idList) > 0 ? "删除章节成功！" : "删除章节失败！";
        } else {
            // 小节，直接删除
            return baseMapper.deleteById(id) > 0 ? "删除小节成功！" : "删除小节失败！";
        }

    }
}
