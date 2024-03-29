package com.zyc.zdh.dao;

import com.zyc.notscan.base.BaseZdhLogsMapper;
import com.zyc.zdh.entity.ZdhLogs;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

/**
 * ClassName: DataSourcesMapper
 * @author zyc-admin
 * @date 2017年12月26日  
 * @Description: TODO  
 */
public interface ZdhLogsMapper extends BaseZdhLogsMapper<ZdhLogs> {

    @Delete("delete from zdh_logs where job_id = #{ids_str}")
    public int deleteBatchById(@Param("ids_str") String ids_str);

    @Select({"<script>",
            "select * from zdh_logs where level in (${levels}) ",
            "and <![CDATA[ log_time >=#{start_time} ]]> and <![CDATA[ log_time <=#{end_time} ]]>",
            "<when test='task_logs_id!=null and task_logs_id !=\"\"'>",
            "AND task_logs_id = #{task_logs_id}",
            "</when>",
            "<when test='job_id!=null and job_id !=\"\"'>",
            " and job_id=#{job_id}",
            "</when>",
            "</script>"})
    public List<ZdhLogs> selectByTime(@Param("job_id") String etl_task_id,@Param("task_logs_id") String task_logs_id,@Param("start_time") Timestamp start_time, @Param("end_time") Timestamp end_time,@Param("levels") String levels);


    @Select({"<script>",
            "select * from zdh_logs where 1=1",
            "<when test='job_id!=null and job_id !=\"\"'>",
            " and job_id=#{job_id}",
            "</when>",
            " limit 100",
            "</script>"})
    public List<ZdhLogs> selectByJobId(@Param("job_id") String job_id);


    @Delete({"<script>",
            "delete from zdh_logs where task_logs_id = #{task_logs_id}",
            "<when test='task_logs_id == null and task_logs_id == \"\"'>",
             " and <![CDATA[ log_time >=#{start_time} ]]> and <![CDATA[ log_time <=#{end_time} ]]>",
            "</when>",
            "<when test='job_id!=null and job_id !=\"\"'>",
            " and job_id = #{job_id}",
            "</when>",
            "</script>"})
    public int deleteByTime(@Param("job_id") String job_id,@Param("task_logs_id") String task_logs_id ,@Param("start_time") Timestamp start_time, @Param("end_time") Timestamp end_time);

}
