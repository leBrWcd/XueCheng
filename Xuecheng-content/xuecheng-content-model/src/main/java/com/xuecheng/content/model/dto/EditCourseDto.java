package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description 修改课程dto
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/10
 */
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    /**
     * 主键
     */
    @ApiModelProperty(value = "课程名称", required = true)
    private Long id;

}
