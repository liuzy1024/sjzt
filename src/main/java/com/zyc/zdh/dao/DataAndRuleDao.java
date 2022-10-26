package com.zyc.zdh.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

//@Mapper
public interface DataAndRuleDao {

	/*@SelectProvider(type=DataAndRuleProvider.class,method="findDataByTableName")
	@Results({ @Result(property = "id", column = "id"),
		       @Result(property = "roleName",column = "role_name") })*/
	public <T> List<T> findDataByTableName(@Param("tableName") String tableName, @Param("sqlResult") String sqlResult, T t);
}
