package com.apitable.shared.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.automation.service.IAutomationRunHistoryService;
import com.apitable.starter.amqp.core.RabbitSenderService;
import com.apitable.starter.databus.client.api.AutomationDaoApiApi;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * XxlJob Handler.
 */
@Component
public class XxlJobHandler {

    @Autowired(required = false)
    private RabbitSenderService rabbitSenderService;

    @Resource
    private IAutomationRunHistoryService iAutomationRunHistoryService;

    @Resource
    private AutomationDaoApiApi automationDaoApiApi;

    /**
     * Apitable Schedule Automation.
     */
    @XxlJob("apitableAutomationJobHandler")
    public void apitableAutomationJobHandler() throws Exception {
        String paramStr = XxlJobHelper.getJobParam();
        JSONObject param = JSONUtil.parseObj(paramStr);
        JSONObject history = new JSONObject();
        String taskId = Long.toString(IdWorker.getId());
        param.set("taskId", taskId);
        history.set("task_id", taskId);
        history.set("space_id", param.getStr("spaceId"));
        history.set("status", 3);
        XxlJobHelper.log("XXL-JOB, Run! paramStr:" + paramStr + " taskId:" + taskId);
        automationDaoApiApi.daoCreateAutomationRunHistory(param.getStr("robotId"), history);

        rabbitSenderService.topicSend("apitable.automation.exchange", "automation.running", param);
    }
}
