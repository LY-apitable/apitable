package com.apitable.shared.util.xxljob;

import lombok.Data;

/**
 * XxlJobInfoBO.
 */
@Data
public class XxlJobInfoBO {

    private int id;               // 主键ID

    private int jobGroup;     // 执行器主键ID

    private String jobDesc;

    private String author = "AUTO CREATE";        // 负责人

    private String alarmEmail;    // 报警邮件

    private String scheduleType = "CRON";          // 调度类型

    private String scheduleConf;

    private String executorHandler = "apitableAutomationJobHandler";           // 执行器，任务Handler名称

    private String executorParam;         // 执行器，任务参数

    private String executorRouteStrategy = "FIRST"; // 执行器路由策略

    private String misfireStrategy = "DO_NOTHING";           // 调度过期策略

    private String executorBlockStrategy = "SERIAL_EXECUTION"; // 阻塞处理策略

    private int executorTimeout = 0;          // 任务执行超时时间，单位秒

    private int executorFailRetryCount = 0;       // 失败重试次数

    private String glueType = "BEAN";      // GLUE类型   #com.xxl.job.core.glue.GlueTypeEnum

    private String childJobId;        // 子任务ID，多个逗号分隔

    private String glueSource = "";        // GLUE源代码

    private String glueRemark = "";        // GLUE备注

    public String getGlueType() {
        return glueType;
    }

    public void setGlueType(String glueType) {
        this.glueType = glueType;
    }

    public String getChildJobId() {
        return childJobId;
    }

    public void setChildJobId(String childJobId) {
        this.childJobId = childJobId;
    }

    public String getGlueSource() {
        return glueSource;
    }

    public void setGlueSource(String glueSource) {
        this.glueSource = glueSource;
    }

    public String getGlueRemark() {
        return glueRemark;
    }

    public void setGlueRemark(String glueRemark) {
        this.glueRemark = glueRemark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(int jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlarmEmail() {
        return alarmEmail;
    }

    public void setAlarmEmail(String alarmEmail) {
        this.alarmEmail = alarmEmail;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getScheduleConf() {
        return scheduleConf;
    }

    public void setScheduleConf(String scheduleConf) {
        this.scheduleConf = scheduleConf;
    }

    public String getExecutorHandler() {
        return executorHandler;
    }

    public void setExecutorHandler(String executorHandler) {
        this.executorHandler = executorHandler;
    }

    public String getExecutorParam() {
        return executorParam;
    }

    public void setExecutorParam(String executorParam) {
        this.executorParam = executorParam;
    }

    public String getExecutorRouteStrategy() {
        return executorRouteStrategy;
    }

    public void setExecutorRouteStrategy(String executorRouteStrategy) {
        this.executorRouteStrategy = executorRouteStrategy;
    }

    public String getMisfireStrategy() {
        return misfireStrategy;
    }

    public void setMisfireStrategy(String misfireStrategy) {
        this.misfireStrategy = misfireStrategy;
    }

    public String getExecutorBlockStrategy() {
        return executorBlockStrategy;
    }

    public void setExecutorBlockStrategy(String executorBlockStrategy) {
        this.executorBlockStrategy = executorBlockStrategy;
    }

    public int getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(int executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public int getExecutorFailRetryCount() {
        return executorFailRetryCount;
    }

    public void setExecutorFailRetryCount(int executorFailRetryCount) {
        this.executorFailRetryCount = executorFailRetryCount;
    }
}