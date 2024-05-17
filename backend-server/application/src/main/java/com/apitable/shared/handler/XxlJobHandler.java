package com.apitable.shared.handler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.automation.entity.AutomationTriggerEntity;
import com.apitable.automation.service.IAutomationRunHistoryService;
import com.apitable.automation.service.IAutomationTriggerService;
import com.apitable.starter.amqp.core.RabbitSenderService;
import com.apitable.starter.databus.client.api.AutomationDaoApiApi;
import com.apitable.starter.databus.client.api.FusionApiApi;
import com.apitable.starter.databus.client.model.ApiResponseRecordDTOs;
import com.apitable.starter.databus.client.model.FieldKeyEnum;
import com.apitable.starter.databus.client.model.RecordDTO;
import com.apitable.user.service.IDeveloperService;
import com.apitable.workspace.entity.DatasheetEntity;
import com.apitable.workspace.service.IDatasheetRecordService;
import com.apitable.workspace.service.IDatasheetService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * XxlJob Handler.
 */
@Component
@Slf4j
public class XxlJobHandler {

    @Autowired(required = false)
    private RabbitSenderService rabbitSenderService;

    @Resource
    private IAutomationRunHistoryService iAutomationRunHistoryService;

    @Resource
    private AutomationDaoApiApi automationDaoApiApi;

    @Resource
    private FusionApiApi fusionApiApi;

    @Resource
    private IDatasheetRecordService iDatasheetRecordService;

    @Resource
    private IAutomationTriggerService iAutomationTriggerService;

    @Resource
    private IDeveloperService iDeveloperService;

    @Resource
    private IDatasheetService iDatasheetService;

    @Value("${SERVER_DOMAIN}")
    private String serverDomain;

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

    /**
     * Apitable Record Time Arrive Automation.
     */
    @XxlJob("apitableRecordTimeArriveJobHandler")
    public void apitableRecordTimeArriveJobHandler() throws Exception {
        String paramStr = XxlJobHelper.getJobParam();
        JSONObject param = JSONUtil.parseObj(paramStr);
        
        String triggerId = param.getStr("triggerId");
        AutomationTriggerEntity triggerEntity = iAutomationTriggerService.selectByTriggerId(triggerId);
        String input = triggerEntity.getInput();
        JSONObject triggerParam = JSONUtil.parseObj(input);
        JSONArray triggerValueArr = triggerParam.getJSONObject("value").getJSONArray("operands");
        String dstId = "";
        String dateFieldId = "";
        String date = "";
        JSONObject dateTimeValue = JSONUtil.createObj();
        JSONArray dateTime = JSONUtil.createArray();
        log.info("triggerValueArr:{}", triggerValueArr);
        for (int i = 0; i < triggerValueArr.size(); i++) {
            if (triggerValueArr.getStr(i).equals("datasheetId")) {
                dstId = triggerValueArr.getJSONObject(++i).getStr("value");
            }
            if (triggerValueArr.getStr(i).equals("dateFieldId")) {
                dateFieldId = triggerValueArr.getJSONObject(++i).getStr("value");
            }
            if (triggerValueArr.getStr(i).equals("dateTime")) {
                dateTimeValue = triggerValueArr.getJSONObject(++i).getJSONObject("value");
                dateTime = dateTimeValue.getJSONArray("operands");
            }
        }
        for (int i = 0; i < dateTime.size(); i++) {
            if (dateTime.getStr(i).equals("date")) {
                date = dateTime.getJSONObject(++i).getStr("value");
            }
        }
        int queryDateDiff = 0;
        String queryDateType = "days";
        if (!date.equals("SameDay")) {
            String type = date.substring(0, date.length() - 1);
            int value = (int) date.charAt(date.length() - 1);
            XxlJobHelper.log("date:" + date + " type:" + type + " value:" + value);
            switch (type) {
                case "AdvanceByDay":
                    queryDateDiff = -value;
                    queryDateType = "days";
                    break;
                case "AdvanceByWeek":
                    queryDateDiff = -value;
                    queryDateType = "weeks";
                    break;
                case "AdvanceByMonth":
                    queryDateDiff = -value;
                    queryDateType = "months";
                    break;
                case "PostponeByDay":
                    queryDateDiff = value;
                    queryDateType = "days";
                    break;
                case "PostponeByWeek":
                    queryDateDiff = value;
                    queryDateType = "weeks";
                    break;
                case "PostponeByMonth":
                    queryDateDiff = value;
                    queryDateType = "months";
                    break;
                default:
                    break;
            }
        }
        String formula = "IS_SAME(DATESTR(DATEADD({" + dateFieldId + "}, " + queryDateDiff + ", '" + queryDateType + "')), DATESTR(TODAY()))";
        long userId = triggerEntity.getUpdatedBy();
        String apiKey = "Bearer " + iDeveloperService.getApiKeyByUserId(userId);
        XxlJobHelper.log("formula:" + formula);
        ApiResponseRecordDTOs response = fusionApiApi.getRecordByDatasheetId(dstId, apiKey, 1000, null, null, null, 
            null, null, null, formula, null, FieldKeyEnum.ID);
        log.info("ApiResponseRecordDTOs:{}", response);
        if (response.getCode() != 200) {
            XxlJobHelper.log("fusionApiApi.getRecordByDatasheetId response error, responseCode:" + response.getCode());
        }

        DatasheetEntity datasheetEntity = iDatasheetService.getByDstId(dstId);
        String datasheetName = datasheetEntity.getDstName();
        JSONObject history = new JSONObject();
        history.set("space_id", param.getStr("spaceId"));
        history.set("status", 3);
        if (response.getData().getTotal() > 0) {
            if (response.getData().getPageSize() > 0) {
                List<RecordDTO> recordDTOs = response.getData().getRecords();
                for (RecordDTO recordDTO : recordDTOs) {
                    String taskId = Long.toString(IdWorker.getId());
                    param.set("taskId", taskId);
                    history.set("task_id", taskId);

                    JSONObject inputObj = JSONUtil.createObj();
                    inputObj.set("datasheetId", dstId);
                    inputObj.set("dateFieldId", dateFieldId);
                    inputObj.set("dateTime", dateTimeValue);

                    JSONObject recordObj = JSONUtil.createObj();
                    recordObj.set("id", recordDTO.getRecordId());
                    recordObj.set("url", serverDomain + "/workbench/" + dstId + "/" + recordDTO.getRecordId());
                    JSONObject fieldObj = JSONUtil.parseObj(recordDTO.getFields());
                    recordObj.set("fields", fieldObj);

                    JSONObject dataSheetObj = JSONUtil.createObj();
                    dataSheetObj.set("id", dstId);
                    dataSheetObj.set("name", datasheetName);

                    JSONObject outputObj = JSONUtil.createObj();
                    outputObj.set("record", recordObj);
                    outputObj.set("recordId", recordDTO.getRecordId());
                    outputObj.set("datasheet", dataSheetObj);
                    outputObj.set("recordUrl", serverDomain + "/workbench/" + dstId + "/" + recordDTO.getRecordId());
                    outputObj.set("datasheetId", dstId);
                    outputObj.set("datasheetName", datasheetName);
                    outputObj.putAll(fieldObj);

                    JSONObject triggerObj = JSONUtil.createObj();
                    triggerObj.set("typeId", triggerEntity.getTriggerTypeId());
                    triggerObj.set("input", inputObj);
                    triggerObj.set("output", outputObj);

                    JSONObject data = JSONUtil.createObj();
                    data.set(triggerId, triggerObj);

                    history.set("data", data);

                    XxlJobHelper.log("CreateAutomationRunHistory paramStr:" + paramStr + " taskId:" + taskId + " data:" + data);
                    automationDaoApiApi.daoCreateAutomationRunHistory(param.getStr("robotId"), history);
                    rabbitSenderService.topicSend("apitable.automation.exchange", "automation.running", param);
                }
            }
        }
    }
}
