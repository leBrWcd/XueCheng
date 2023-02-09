package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CategoryListDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author Lebrwcd
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper categoryMapper;

    @Override
    public List<CategoryListDto> treeNode(String id) {
        // 数据库查询出来的所有分类:根节点下的所有子节点
        List<CategoryListDto> tree = categoryMapper.treeNodes(id);

        // 封装返回数据，根节点直接的下属节点  1， 1-1
        List<CategoryListDto> resultDto = new ArrayList<>();
        HashMap<String,CategoryListDto> map = new HashMap<>();
        tree.stream().forEach( item -> {
            // 每遍历一个，存入map，方便后续父子节点查找
            map.put(item.getId(),item);
            // 先找到根节点的直接下属节点  1-1
            if (item.getParentid().equals(id))  {
                // 这里先存入的是 1-1,1-2,1-3 这些直接下属节点，此时他们的childrenTreeNodes还是空的
                resultDto.add(item);
            }
            // 找到这些直接下属节点的所有子节点 1-1-1,1-1-2...，放到其childrenTreeNodes属性中
            CategoryListDto parent = map.get(item.getParentid());
            if (parent != null) {
                // 如果父节点不为null,封装其子节点
                if (parent.getChildrenTreeNodes() == null) {
                    parent.setChildrenTreeNodes(new ArrayList<>());
                }
                parent.getChildrenTreeNodes().add(item);
            }
        });
        return resultDto;
    }
}
