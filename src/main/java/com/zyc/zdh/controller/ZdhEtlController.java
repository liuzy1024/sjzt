package com.zyc.zdh.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.SftpException;
import com.zyc.zdh.dao.EtlTaskUpdateLogsMapper;
import com.zyc.zdh.dao.ZdhNginxMapper;
import com.zyc.zdh.entity.*;
import com.zyc.zdh.job.SnowflakeIdWorker;
import com.zyc.zdh.service.DataSourcesService;
import com.zyc.zdh.service.EtlTaskService;
import com.zyc.zdh.util.Const;
import com.zyc.zdh.util.DBUtil;
import com.zyc.zdh.util.SFTPUtil;
import com.zyc.zdh.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 单源ETL任务服务
 */
@Controller
public class ZdhEtlController extends BaseController{

    public Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    DataSourcesService dataSourcesServiceImpl;
    @Autowired
    EtlTaskService etlTaskService;
    @Autowired
    EtlTaskUpdateLogsMapper etlTaskUpdateLogsMapper;
    @Autowired
    ZdhNginxMapper zdhNginxMapper;


    /**
     * 单源ETL首页
     * @return
     */
    @RequestMapping("/etl_task_index")
    public String etl_task_index() {

        return "etl/etl_task_index";
    }

    /**
     * 获取单源ETL任务明细
     * @param id
     * @return
     */
    @RequestMapping(value = "/etl_task_detail", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String etl_task_detail(String id) {
        try{
            EtlTaskInfo eti=etlTaskService.selectById(id);
            return ReturnInfo.createInfo(RETURN_CODE.SUCCESS.getCode(), "查询成功", eti);
        }catch (Exception e){
            return ReturnInfo.createInfo(RETURN_CODE.FAIL.getCode(), "查询失败", e);
        }

    }

    /**
     * 根据条件模糊查询单源ETL任务信息
     * @param etl_context
     * @param file_name
     * @return
     */
    @RequestMapping(value = "/etl_task_list2", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String etl_task_list2(String etl_context, String file_name) {
        List<EtlTaskInfo> list = new ArrayList<>();
        list = etlTaskService.selectByParams(getUser().getId(), etl_context, file_name);
        return JSON.toJSONString(list);
    }

    /**
     * 批量删除单源ETL任务
     * @param ids
     * @return
     */
    @RequestMapping(value = "/etl_task_delete", produces = "text/html;charset=UTF-8")
    @ResponseBody
    @Transactional
    public String etl_task_delete(Long[] ids) {
        try{
            etlTaskService.deleteBatchById(ids);
            return ReturnInfo.createInfo(RETURN_CODE.SUCCESS.getCode(),RETURN_CODE.SUCCESS.getDesc(), null);
        }catch (Exception e){
            logger.error(e.getMessage(),e.getCause());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ReturnInfo.createInfo(RETURN_CODE.FAIL.getCode(),e.getMessage(), null);
        }
    }

    /**
     * 新增单源ETL任务首页
     * @param request
     * @param response
     * @param id
     * @param edit
     * @return
     */
    @RequestMapping("/etl_task_add_index")
    public String etl_task_add(HttpServletRequest request, HttpServletResponse response, Long id, String edit) {

        System.out.println(edit);
        request.setAttribute("edit", edit);
        return "etl/etl_task_add_index";
    }


    /**
     * 新增单源ETL任务
     * 如果输入数据源类型是外部上传,会补充文件服务器信息
     * @param etlTaskInfo
     * @return
     */
    @RequestMapping(value="/etl_task_add", produces = "text/html;charset=UTF-8")
    @ResponseBody
    @Transactional
    public String etl_task_add(EtlTaskInfo etlTaskInfo) {
        //String json_str=JSON.toJSONString(request.getParameterMap());
        try{
            String owner = getUser().getId();
            etlTaskInfo.setOwner(owner);
            debugInfo(etlTaskInfo);

            etlTaskInfo.setId(SnowflakeIdWorker.getInstance().nextId() + "");
            etlTaskInfo.setCreate_time(new Timestamp(new Date().getTime()));
            etlTaskInfo.setUpdate_time(new Timestamp(new Date().getTime()));
            etlTaskInfo.setIs_delete(Const.NOT_DELETE);
            if (etlTaskInfo.getData_source_type_input().equals("外部上传")) {
                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                if (zdhNginx != null && !zdhNginx.getHost().equals("")) {
                    etlTaskInfo.setData_sources_file_name_input(zdhNginx.getNginx_dir() + "/" + owner + "/" + etlTaskInfo.getData_sources_file_name_input());
                } else {
                    etlTaskInfo.setData_sources_file_name_input(zdhNginx.getTmp_dir() + "/" + owner + "/" + etlTaskInfo.getData_sources_file_name_input());
                }

            }

            etlTaskService.insert(etlTaskInfo);
            if (etlTaskInfo.getUpdate_context() != null && !etlTaskInfo.getUpdate_context().equals("")) {
                //插入更新日志表
                EtlTaskUpdateLogs etlTaskUpdateLogs = new EtlTaskUpdateLogs();
                etlTaskUpdateLogs.setId(etlTaskInfo.getId());
                etlTaskUpdateLogs.setUpdate_context(etlTaskInfo.getUpdate_context());
                etlTaskUpdateLogs.setUpdate_time(new Timestamp(new Date().getTime()));
                etlTaskUpdateLogs.setOwner(getUser().getId());
                etlTaskUpdateLogsMapper.insert(etlTaskUpdateLogs);
            }
            return ReturnInfo.createInfo(RETURN_CODE.SUCCESS.getCode(),RETURN_CODE.SUCCESS.getDesc(), null);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error(e.getMessage(),e.getCause());
            e.printStackTrace();
            return ReturnInfo.createInfo(RETURN_CODE.FAIL.getCode(),e.getMessage(), null);
        }
    }


    /**
     * 单源ETL任务输入数据源是外部上传时,上传文件服务
     * @param up_file 上传文件
     * @param request 请求回话
     * @return
     */
    @RequestMapping(value="/etl_task_add_file", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String etl_task_add_file(MultipartFile up_file, HttpServletRequest request) {
        try{
            String json_str = JSON.toJSONString(request.getParameterMap());
            String owner = getUser().getId();
            System.out.println(json_str);
            System.out.println(up_file);
            if (up_file != null) {
                String fileName = up_file.getOriginalFilename();
                System.out.println("上传文件不为空");
                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                File fileDir = new File(zdhNginx.getTmp_dir() + "/" + owner);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String nginx_dir = zdhNginx.getNginx_dir();

                FileCopyUtils.copy(up_file.getInputStream(), Files.newOutputStream(tempFile.toPath()));
                if (!zdhNginx.getHost().equals("")) {
                    System.out.println("通过sftp上传文件");
                    SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                            zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                    sftp.login();
                    InputStream is = new FileInputStream(tempFile);
                    sftp.upload(nginx_dir + "/" + owner + "/", fileName, is);
                    sftp.logout();
                }
            }
            return ReturnInfo.createInfo(RETURN_CODE.SUCCESS.getCode(),RETURN_CODE.SUCCESS.getDesc(), null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage(),e.getCause());
            return ReturnInfo.createInfo(RETURN_CODE.FAIL.getCode(),e.getMessage(), null);
        }
    }

    /**
     * 单源ETL任务更新
     * todo 此次是否每次都更新文件服务器信息,待优化
     * @param etlTaskInfo
     * @return
     */
    @RequestMapping(value="/etl_task_update", produces = "text/html;charset=UTF-8")
    @ResponseBody
    @Transactional
    public String etl_task_update(EtlTaskInfo etlTaskInfo) {
        try{
            String owner = getUser().getId();
            etlTaskInfo.setOwner(owner);
            etlTaskInfo.setIs_delete(Const.NOT_DELETE);
            etlTaskInfo.setUpdate_time(new Timestamp(new Date().getTime()));
            debugInfo(etlTaskInfo);
            if (etlTaskInfo.getData_source_type_input().equals("外部上传")) {
                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                String[] file_name_ary = etlTaskInfo.getData_sources_file_name_input().split("/");
                String file_name = file_name_ary[0];
                if (file_name_ary.length > 0)
                    file_name = file_name_ary[file_name_ary.length - 1];

                if (zdhNginx != null && !zdhNginx.getHost().equals("")) {
                    etlTaskInfo.setData_sources_file_name_input(zdhNginx.getNginx_dir() + "/" + owner + "/" + file_name);
                } else {
                    etlTaskInfo.setData_sources_file_name_input(zdhNginx.getTmp_dir() + "/" + owner + "/" + file_name);
                }

            }

            //获取旧数据是否更新说明
            EtlTaskInfo etlTaskInfo1 = etlTaskService.selectById(etlTaskInfo.getId());

            etlTaskService.update(etlTaskInfo);

            if (etlTaskInfo.getUpdate_context() != null && !etlTaskInfo.getUpdate_context().equals("")
                    && !etlTaskInfo1.getUpdate_context().equals(etlTaskInfo.getUpdate_context())) {
                //插入更新日志表
                EtlTaskUpdateLogs etlTaskUpdateLogs = new EtlTaskUpdateLogs();
                etlTaskUpdateLogs.setId(etlTaskInfo.getId());
                etlTaskUpdateLogs.setUpdate_context(etlTaskInfo.getUpdate_context());
                etlTaskUpdateLogs.setUpdate_time(new Timestamp(new Date().getTime()));
                etlTaskUpdateLogs.setOwner(getUser().getId());
                etlTaskUpdateLogsMapper.insert(etlTaskUpdateLogs);
            }
            return ReturnInfo.createInfo(RETURN_CODE.SUCCESS.getCode(),RETURN_CODE.SUCCESS.getDesc(), null);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error(e.getMessage(),e.getCause());
            return ReturnInfo.createInfo(RETURN_CODE.FAIL.getCode(),e.getMessage(), null);
        }
    }


    /**
     * 根据数据源id 获取数据源下所有的表名字
     * @param id 数据源id
     * @return
     */
    @RequestMapping(value = "/etl_task_tables", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String etl_task_tables(String id) {

        DataSourcesInfo dataSourcesInfo = dataSourcesServiceImpl.selectById(id);

        String jsonArrayStr = tables(dataSourcesInfo);

        if(StringUtils.isEmpty(jsonArrayStr)){
            return JSON.toJSONString(new JSONObject());
        }
        System.out.println(jsonArrayStr);
        return jsonArrayStr;
    }

    private String tables(DataSourcesInfo dataSourcesInfo) {

        debugInfo(dataSourcesInfo);

        String url = dataSourcesInfo.getUrl();


        try {
            return JSON.toJSONString(new DBUtil().R3(dataSourcesInfo.getDriver(), dataSourcesInfo.getUrl(), dataSourcesInfo.getUsername(), dataSourcesInfo.getPassword(),
                    ""));
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }


    }


    /**
     * 根据数据源id,表名获取表的schema
     * @param id
     * @param table_name
     * @return
     */
    @RequestMapping(value="/etl_task_schema", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String etl_task_schema(String id, String table_name) {

        DataSourcesInfo dataSourcesInfo = dataSourcesServiceImpl.selectById(id);

        String jsonArrayStr = schema(dataSourcesInfo, table_name);

        System.out.println(jsonArrayStr);
        return jsonArrayStr;
    }


    private String schema(DataSourcesInfo dataSourcesInfo, String table_name) {

        String url = dataSourcesInfo.getUrl();

        try {
            return JSON.toJSONString(new DBUtil().R4(dataSourcesInfo.getDriver(), dataSourcesInfo.getUrl(), dataSourcesInfo.getUsername(), dataSourcesInfo.getPassword(),
                    "select * from " + table_name + " where 1=2", table_name));
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


    private void debugInfo(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0, len = fields.length; i < len; i++) {
            // 对于每个属性，获取属性名
            String varName = fields[i].getName();
            try {
                // 获取原来的访问控制权限
                boolean accessFlag = fields[i].isAccessible();
                // 修改访问控制权限
                fields[i].setAccessible(true);
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object o;
                try {
                    o = fields[i].get(obj);
                    System.err.println("传入的对象中包含一个如下的变量：" + varName + " = " + o);
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 恢复访问控制权限
                fields[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    }

}
