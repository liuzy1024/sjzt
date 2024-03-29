package com.zyc.zdh.dao;

import com.zyc.notscan.base.BaseDataTagMapper;
import com.zyc.zdh.entity.DataTagInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;

public interface DataTagMapper extends BaseDataTagMapper<DataTagInfo> {
    @Update(
            {
                    "<script>",
                    "update data_tag_info set is_delete=1 ,update_time= #{update_time} where id in ",
                    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
                    "#{id}",
                    "</foreach>",
                    "</script>"
            }
    )
    public int deleteBatchById(@Param("ids") String[] ids, @Param("update_time") Timestamp update_time);
}