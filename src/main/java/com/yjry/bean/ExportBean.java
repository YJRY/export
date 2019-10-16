package com.yjry.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 导出模板类
 * @author xuqi
 * @date 2019-08-02 10:32:53
 */
public class ExportBean {

    private String entityName;//导出类的名称
    private String daoName;//导出类dao层的类名称
    private String methodName;//dao层数据访问方法名称
    private List<Map<String, String>> columnNames;//导出的列信息
    private String fileName;//导出的文件名
    private JSONObject conditions;//数据查询条件
    //属性
    private String type;
    //重点库导出所需属性
    private Integer libId;//重点库id
    //四类数据导出所需属性
    //目标号码
    private String targetId;
    //任务ID
    private Integer taskId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getLibId() {
        return libId;
    }

    public void setLibId(Integer libId) {
        this.libId = libId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getDaoName() {
        return daoName;
    }

    public void setDaoName(String daoName) {
        this.daoName = daoName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Map<String, String>> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<Map<String, String>> columnNames) {
        this.columnNames = columnNames;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public JSONObject getConditions() {
        return conditions;
    }

    public void setConditions(JSONObject conditions) {
        this.conditions = conditions;
    }
}
