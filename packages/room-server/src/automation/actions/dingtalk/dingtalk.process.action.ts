import { IJsonSchema } from "@apitable/core";
import { AutomationAction } from "../decorators/automation.action.decorator";
import { IActionResponse } from "../interface/action.response";
import { IBaseAction, IUiSchema } from "../interface/base.action";
import { getAppInstance } from "app_instance";
import { AutomationService } from "automation/services/automation.service";
import { QueueSenderBaseService } from "shared/services/queue/queue.sender.base.service";
import { integrationQueueExchangeName } from "shared/services/queue/queue.module";

@AutomationAction("发起钉钉审批", {themeLogo:{light: "/space/2024/03/13/5a6d5ac8d1ae49879cbd7b65370894d6", dark: "/space/2024/03/13/5a6d5ac8d1ae49879cbd7b65370894d6"}, description: "发起钉钉OA审批"})
export class DingTalkProcessAction implements IBaseAction {
  async endpoint(input: any): Promise<IActionResponse<any>> {
    console.log(`Entry DingTalkProcessAction connector. the input is `, input);
    const searchString = /"/g;
    const originator = input.originator.replace(searchString, '');
    const process = input.process;
    const component = input.component;
    
    const appInstance = getAppInstance();
    const automationService = appInstance.get(AutomationService);
    const spaceId = await automationService.getSpaceIdByRobotId(input.robotId);
    const queueSenderService = appInstance.get(QueueSenderBaseService);
    const message = {
      type: 0,
      processCode: process,
      component: component,
      spaceId: spaceId,
      originator: originator
    };
    queueSenderService.sendMessage(integrationQueueExchangeName, 'integration.message', message);
    return {
      success: true,
      code: 200,
      data: {
        data: "",
      },
    };
  }

  getInputSchema(): IJsonSchema {
    return {
      "type": "object",
      "required": ["process", "originator", "component"],
      "properties": {
        "process": {
          "type": "string",
          "title": "审批流程",
          "description": "选择要发起的钉钉OA审批流程"
        },
        "originator": {
          "type": "string",
          "title": "审批发起人",
          "description": "选择要审批的发起人"
        },
        "component": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["key", "value"],
            "properties": {
              "key": {
                "type": "string",
                "description": "请选择表单组件名称"
              },
              "value": {
                "type": "string",
                "description": "请输入表单组件内容"
              }
            }
          },
          "title": "表单数据",
          "description": "请填写审批发起时需要写入表单控件的数据"
        }
      },
      "additionalProperties": false
    };
  }

  getOutputSchema(): IJsonSchema {
    return { };
  }

  getUISchema(): IUiSchema {
    return {
      "process": {
        "ui:widget": "FilterProcessWidget",
      },
      "originator": {
        "ui:widget": "TextDateWidget",
      },
      "ui:order": ["process", "originator", "component"],
      "component": {
        "items": {
          "key": {
            "ui:widget": "FilterProcessComponentWidget",
            "ui:options": {
              "showTitle": false
            }
          },
          "value": {
            "ui:options": {
              "showTitle": false
            }
          },
          "ui:order": ["key", "value"],
          "ui:options": {
            "inline": true
          }
        },
        "ui:options": {
          "orderable": false
        }
      },
      "ui:options": {
        "layout": [
          ["process", "originator", "component"],
        ]
      }
    };
  }
}