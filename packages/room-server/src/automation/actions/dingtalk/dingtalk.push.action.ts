import { IJsonSchema } from "@apitable/core";
import { AutomationAction } from "../decorators/automation.action.decorator";
import { IActionResponse } from "../interface/action.response";
import { IBaseAction, IUiSchema } from "../interface/base.action";
import { ResponseStatusCodeEnums } from "../enum/response.status.code.enums";
import { QueueSenderBaseService } from "shared/services/queue/queue.sender.base.service";
import { getAppInstance } from "app_instance";
import { notificationQueueExchangeName } from "shared/services/queue/queue.module";
import { AutomationService } from "automation/services/automation.service";

@AutomationAction("钉钉消息推送", {themeLogo:{light: "/space/2024/03/13/5a6d5ac8d1ae49879cbd7b65370894d6", dark: "/space/2024/03/13/5a6d5ac8d1ae49879cbd7b65370894d6"}, description: "向用户发送钉钉消息通知"})
export class DingTalkPushAction implements IBaseAction {
  async endpoint(input: any): Promise<IActionResponse<any>> {
    console.log(`Entry customer connector. the input is `, input)
    const toUnitId: string[] = [];
    input.users.forEach((user: any) => {
      if (user.type == 'field') {
        const tmpField = user.field;
        const searchString = /"/g;
        const field = tmpField.replace(searchString, '');
        field.split(",").forEach((item: string) => {
          toUnitId.push(item.trim());
        })
      } if (user.type == 'member') {
        const member = user.member;
        member.forEach((userId: string) => {
          toUnitId.push(userId);
        })
      }
    })
    const title = input.title;
    const content = input.content;
    
    const appInstance = getAppInstance();
    const automationService = appInstance.get(AutomationService);
    const spaceId = await automationService.getSpaceIdByRobotId(input.robotId);
    const queueSenderService = appInstance.get(QueueSenderBaseService);
    const message = {
      type: 1,
      title: title,
      content: content,
      toUnitId: toUnitId,
      spaceId: spaceId
    };
    queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
    return {
      success: true,
      code: ResponseStatusCodeEnums.Success,
      data: {
        data: "",
      },
    };
  }

  getInputSchema(): IJsonSchema {
    return {
      "type": "object",
      "required": ["users", "title", "content"],
      "properties": {
        "title": {
          "type": "string",
				  "title": "通知标题"
        },
        "content": {
          "type": "string",
          "title": "消息内容",
          "description": "输入要发送到钉钉工作通知的消息内容（输入英文斜杠「/」可插入变量）"
        },
        "users": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["type", "field", "member"],
            "properties": {
              "type": {
                "enum": ["field", "member"],
                "type": "string",
                "default": "field",
                "enumNames": ["成员字段", "用户"],
              }
            },
            "dependencies": {
              "type": {
                "oneOf": [{
                  "properties": {
                    "field": {
                      "type": "string",
                    },
                    "type": {
                      "enum": ["field"]
                    }
                  }
                }, {
                  "properties": {
                    "member": {
                      "type": "string",
                    },
                    "type": {
                      "enum": ["member"]
                    }
                  }
                }]
              }
            }
          },
          "title": "发送对象",
          "description": "选择要发送钉钉通知的用户"
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
      "users": {
        "items": {
          "type": {
            "ui:options": {
              "showTitle": false
            }
          },
          "field": {
            "ui:widget": "TextDateWidget",
            "ui:options": {
              "showTitle": false
            }
          },
          "member": {
            "ui:widget": "FilterMemberWidget",
            "ui:options": {
              "showTitle": false
            }
          },
          "ui:order": ["type", "field", "member"],
          "ui:options": {
            "inline": true
          }
        },
        "ui:options": {
          "orderable": false
        }
      },
      "ui:order": ["users", "title", "content"],
      "ui:options": {
        "layout": [
          ["users", "title", "content"],
        ]
      }
    };
  }
}