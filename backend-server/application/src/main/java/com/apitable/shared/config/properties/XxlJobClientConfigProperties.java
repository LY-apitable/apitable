package com.apitable.shared.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * XxlJobClientConfigProperties.
 */
@Configuration
public class XxlJobClientConfigProperties {

    @Value("${xxl.job.client.loginUrl:${xxl.job.admin.addresses}/login?userName=admin&password=123456}")
    private String loginUrl;

    @Value("${xxl.job.client.addUrl:${xxl.job.admin.addresses}/jobinfo/addJob}")
    private String jobInfoAddUrl;

    @Value("${xxl.job.client.deleteUrl:${xxl.job.admin.addresses}/jobinfo/removeJob?id=%s}")
    private String jobInfoDeleteUrl;

    @Value("${xxl.job.client.startJobUrl:${xxl.job.admin.addresses}/jobinfo/startJob?id=%s}")
    private String jobInfoStartJobUrl;

    @Value("${xxl.job.client.stopJobUrl:${xxl.job.admin.addresses}/jobinfo/stopJob?id=%s}")
    private String jobInfoStopJobUrl;

    @Value("${xxl.job.client.updateUrl:${xxl.job.admin.addresses}/jobinfo/updateJob}")
    private String jobInfoUpdateUrl;

    @Value("${xxl.job.client.loadByIdUrl:${xxl.job.admin.addresses}/jobinfo/loadById?id=%s}")
    private String jobInfoLoadByIdUrl;
    
    @Value("${xxl.job.client.loadByAppName:${xxl.job.admin.addresses}/jobgroup/loadByAppName?appName=%s}")
    private String jobGroupLoadByAppNameUrl;

    /**
     * 任务列表.
     */
    @Value("${xxl.job.client.jobInfoPageListUrl:${xxl.job.admin.addresses}/jobinfo/pageList}")
    private String jobInfoPageListUrl;

    /**
     * 执行器列表.
     */
    @Value("${xxl.job.client.jobGroupPageListUrl:${xxl.job.admin.addresses}/jobgroup/pageList}")
    private String jobGroupPageListUrl;

    /**
     * 执行器创建 URL.
     */
    @Value("${xxl.job.client.jobGroupSaveUrl:${xxl.job.admin.addresses}/jobgroup/save")
    private String jobGroupSaveUrl;

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getJobInfoAddUrl() {
        return jobInfoAddUrl;
    }

    public void setJobInfoAddUrl(String jobInfoAddUrl) {
        this.jobInfoAddUrl = jobInfoAddUrl;
    }

    public String getJobInfoDeleteUrl() {
        return jobInfoDeleteUrl;
    }

    public void setJobInfoDeleteUrl(String jobInfoDeleteUrl) {
        this.jobInfoDeleteUrl = jobInfoDeleteUrl;
    }

    public String getJobInfoStartJobUrl() {
        return jobInfoStartJobUrl;
    }

    public void setJobInfoStartJobUrl(String jobInfoStartJobUrl) {
        this.jobInfoStartJobUrl = jobInfoStartJobUrl;
    }

    public String getJobInfoStopJobUrl() {
        return jobInfoStopJobUrl;
    }

    public void setJobInfoStopJobUrl(String jobInfoStopJobUrl) {
        this.jobInfoStopJobUrl = jobInfoStopJobUrl;
    }

    public String getJobInfoUpdateUrl() {
        return jobInfoUpdateUrl;
    }

    public void setJobInfoUpdateUrl(String jobInfoUpdateUrl) {
        this.jobInfoUpdateUrl = jobInfoUpdateUrl;
    }

    public String getJobInfoLoadByIdUrl() {
        return jobInfoLoadByIdUrl;
    }

    public void setJobInfoLoadByIdUrl(String jobInfoLoadByIdUrl) {
        this.jobInfoLoadByIdUrl = jobInfoLoadByIdUrl;
    }

    public String getJobInfoPageListUrl() {
        return jobInfoPageListUrl;
    }

    public void setJobInfoPageListUrl(String jobInfoPageListUrl) {
        this.jobInfoPageListUrl = jobInfoPageListUrl;
    }

    public String getJobGroupPageListUrl() {
        return jobGroupPageListUrl;
    }

    public void setJobGroupPageListUrl(String jobGroupPageListUrl) {
        this.jobGroupPageListUrl = jobGroupPageListUrl;
    }

    public String getJobGroupSaveUrl() {
        return jobGroupSaveUrl;
    }

    public void setJobGroupSaveUrl(String jobGroupSaveUrl) {
        this.jobGroupSaveUrl = jobGroupSaveUrl;
    }

    public String getJobGroupLoadByAppNameUrl() {
        return jobGroupLoadByAppNameUrl;
    }

    public void setJobGroupLoadByAppNameUrl(String jobGroupLoadByAppNameUrl) {
        this.jobGroupLoadByAppNameUrl = jobGroupLoadByAppNameUrl;
    }

    @Override
    public String toString() {
        return "XxlJobClientConfigProperties{" 
            + "loginUrl='" + loginUrl + '\'' 
            + ", jobInfoAddUrl='" + jobInfoAddUrl + '\'' 
            + ", jobInfoDeleteUrl='" + jobInfoDeleteUrl + '\'' 
            + ", jobInfoStartJobUrl='" + jobInfoStartJobUrl + '\'' 
            + ", jobInfoStopJobUrl='" + jobInfoStopJobUrl + '\'' 
            + ", jobInfoUpdateUrl='" + jobInfoUpdateUrl + '\'' 
            + ", jobInfoLoadByIdUrl='" + jobInfoLoadByIdUrl + '\'' 
            + ", jobInfoPageListUrl='" + jobInfoPageListUrl + '\'' 
            + ", jobGroupPageListUrl='" + jobGroupPageListUrl + '\'' 
            + ", jobGroupLoadByAppName='" + jobGroupLoadByAppNameUrl + '\'' 
            + '}';
    }
}
