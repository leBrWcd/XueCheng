### 开发中用到的sql测试

# 如果树的层级固定，可以采用自连接
select
    one.id one_id,
    one.name one_name,
    one.parentid one_parentid,
    two.id two_id,
    two.name two_name,
    two.parentid two_parentid

from course_category one
         inner join course_category two
                    on one.id = two.parentid
# 三级 inner join course_category three on two.id = three.parentid
where one.parentid = 1; #根节点往下找
order by one.orderby,two.orderby;

# 如果树的层级不固定，可以采用mysql递归
with RECURSIVE t1 as (
    select 1 as n
    union all
    SELECT n + 1 from t1 where n <5
)
select * from t1;

with recursive tree as (
    select * from course_category where id = '1'  #表的初始值只有根节点，顺着根节点往下递归
    union all
    select t.*
    from course_category t inner join tree on tree.id = t.parentid # 递归条件，不满足即为递归出口
)
select * from tree order by tree.id,tree.orderby

show index from course_base;

# 课程计划树形查找
select
    t1.id t1_id,
    t1.pname t1_pname,
    t1.grade t1_grade,
    t1.parentid t1_parentid,
    t2.id t2_id,
    t2.pname t2_pname,
    t2.grade t2_grade ,
    t2.parentid t2_parentid,
    tm.teachplan_id,
    tm.media_fileName,
    tm.course_id
from teachplan t1
         inner join teachplan t2
                    on t1.id = t2.parentid
         left join teachplan_media tm
                   on tm.teachplan_id = t2.id
where t1.parentid = '0' and t1.course_id = '117'
order by t1.orderby,t2.orderby
