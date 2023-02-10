package com.xuecheng.content.base.validated;

/**
 * Description 参数校验分组
 * 有时候在同一个属性上设置一个校验规则不能满足要求，比如：订单编号由系统生成，在添加订单时要
 * 求订单编号为空，在更新 订单时要求订单编写不能为空。此时就用到了分组校验，同一个属性定义多个
 * 校验规则属于不同的分组，
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/10
 */
public class ValidatedGroup {

    public interface Insert{};
    public interface Update{};
    public interface Delete{};

}
