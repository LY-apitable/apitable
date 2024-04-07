/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.interfaces.automation.facede;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.automation.entity.AutomationTriggerEntity;
import com.apitable.automation.model.AutomationVO;
import com.apitable.automation.service.IAutomationRobotService;
import com.apitable.automation.service.IAutomationTriggerService;
import com.apitable.shared.client.XxlJobClient;
import com.apitable.shared.util.xxljob.XxlJobInfoBO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * custom automation service facade implement.
 */
@Component
@Slf4j
public class CustomAutomationServiceFacadeImpl implements AutomationServiceFacade {

    @Resource
    private IAutomationTriggerService iAutomationTriggerService;

    @Resource
    private XxlJobClient xxlJobClient;

    @Resource
    private IAutomationRobotService iAutomationRobotService;

    @Override
    public void publishSchedule(Long scheduleId) {
        log.info("publishSchedule scheduleId:{}", scheduleId);
    }

    @Override
    public void startSchedule(List<Integer> jobIdList) {
        log.info("startSchedule jobIdList:{}", JSONUtil.parseArray(jobIdList).toString());
        for (Integer jobId : jobIdList) {
            xxlJobClient.startJob(jobId);
        }
    }

    @Override
    public void stopSchedule(List<Integer> jobIdList) {
        log.info("stopSchedule jobIdList:{}", JSONUtil.parseArray(jobIdList).toString());
        for (Integer jobId : jobIdList) {
            xxlJobClient.stopJob(jobId);
        }
    }

    @Override
    public void copy(Map<String, String> newTriggerMap) {
        log.info("copy newTriggerMap:{}", JSONUtil.parseObj(newTriggerMap).toString());
    }

    @Override
    public void createSchedule(String spaceId, String triggerId, String scheduleConfig) {
        log.info("createSchedule spaceId:{}, triggerId:{}, scheduleConfig:{}", spaceId, triggerId, scheduleConfig);
        XxlJobInfoBO info = new XxlJobInfoBO();
        info.setJobDesc("APITABLE triggerId:" + triggerId);
        JSONObject executorObj = xxlJobClient.loadByAppName("xxl-job-executor");
        info.setJobGroup(executorObj.getJSONObject("content").getInt("id")); //执行器主键ID
        JSONObject executorParams = new JSONObject();
        executorParams.set("triggerId", triggerId);
        String robotId = iAutomationTriggerService.selectByTriggerId(triggerId).getRobotId();
        executorParams.set("robotId", robotId);
        executorParams.set("spaceId", spaceId);
        info.setExecutorParam(executorParams.toString()); //任务参数
        info.setScheduleConf("0/3 * * * * ?"); //Cron
        // 省略其他参数
        JSONObject jobInfo = JSONUtil.parseObj(info);
        JSONObject creatResult = xxlJobClient.createJob(jobInfo);
        log.info("createSchedule result:{}", creatResult.toString());
        int jobId = creatResult.getInt("content");
        iAutomationTriggerService.updateJobIdByTriggerId(triggerId, jobId);
    }

    @Override
    public void updateSchedule(String triggerId, String scheduleConfig) {
        AutomationTriggerEntity trigger = iAutomationTriggerService.selectByTriggerId(triggerId);
        Integer jobId = trigger.getJobId();
        JSONObject result = xxlJobClient.loadById(jobId);
        String jobInfoStr = result.getStr("content");
        JSONObject jobInfo = JSONUtil.parseObj(jobInfoStr);
        JSONObject configObj = JSONUtil.parseObj(scheduleConfig);
        String minute = configObj.getStr("minute");
        String hour = configObj.getStr("hour");
        String dayOfMonth = configObj.getStr("dayOfMonth").equals("1L") ? "L" : configObj.getStr("dayOfMonth");
        String month = configObj.getStr("month");
        String dayOfWeek = configObj.getStr("dayOfWeek").equals("*") ? "?" : "*";
        String cronString = "0 " + minute + " " + hour + " " + dayOfMonth + " " + month + " " + dayOfWeek;
        jobInfo.set("scheduleConf", cronString);
        jobInfo.remove("addTime");
        jobInfo.remove("updateTime");
        jobInfo.remove("glueUpdatetime");
        xxlJobClient.updateJob(jobInfo);
        AutomationVO robot = iAutomationRobotService.getRobotByRobotId(trigger.getRobotId());
        if (robot.getIsActive() == 1) {
            xxlJobClient.startJob(jobId);
        }

        log.info("updateSchedule triggerId:{}, scheduleConfig:{}, jobInfo:{}", triggerId, scheduleConfig);
    }

    @Override
    public void deleteSchedule(String triggerId, Long userId) {
        log.info("deleteSchedule triggerId:{}, userId:{}", triggerId, userId);
        AutomationTriggerEntity trigger = iAutomationTriggerService.selectByTriggerId(triggerId);
        Integer jobId = trigger.getJobId();
        xxlJobClient.deleteJob(jobId);
    }
}
