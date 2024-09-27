import { IJsonSchema } from "@apitable/core";
import { AutomationAction } from "../decorators/automation.action.decorator";
import { IActionResponse } from "../interface/action.response";
import { IBaseAction, IUiSchema } from "../interface/base.action";
import { getAppInstance } from "app_instance";
import { AutomationService } from "automation/services/automation.service";
import { QueueSenderBaseService } from "shared/services/queue/queue.sender.base.service";
import { integrationQueueExchangeName } from "shared/services/queue/queue.module";
import { ResponseStatusCodeEnums } from "../enum/response.status.code.enums";

@AutomationAction("创建钉钉待办任务", {themeLogo:{light: "/space/2024/03/13/5a6d5ac8d1ae49879cbd7b65370894d6", dark: "/space/2024/03/13/5a6d5ac8d1ae49879cbd7b65370894d6"}, description: "创建钉钉待办任务"})
export class DingTalkTodoAction implements IBaseAction {
  async endpoint(input: any): Promise<IActionResponse<any>> {
    console.log(`Entry DingTalkPushAction connector. the input is `, input)
    const creatorObj = input.creator;
    let creator: string = "";
    if (creatorObj.type == 'field') {
      const tmpField = creatorObj.field;
      const searchString = /"/g;
      const field = tmpField.replace(searchString, '');
      creator = field.split(",")[0];
    } if (creatorObj.type == 'member') {
      const member = creatorObj.member;
      creator = member[0]
    }
    console.log(input.creator);
    const executorIds: string[] = [];
    input.executors.forEach((user: any) => {
      if (user.type == 'field') {
        const tmpField = user.field;
        const searchString = /"/g;
        const field = tmpField.replace(searchString, '');
        field.split(",").forEach((item: string) => {
          executorIds.push(item.trim());
        })
      } if (user.type == 'member') {
        const member = user.member;
        member.forEach((userId: string) => {
          executorIds.push(userId);
        })
      }
    })
    const participantIds: string[] = [];
    if (input.participants) {
      input.participants.forEach((user: any) => {
        if (user.type == 'field') {
          const tmpField = user.field;
          const searchString = /"/g;
          const field = tmpField.replace(searchString, '');
          field.split(",").forEach((item: string) => {
            participantIds.push(item.trim());
          })
        } if (user.type == 'member') {
          const member = user.member;
          member.forEach((userId: string) => {
            participantIds.push(userId);
          })
        }
      })
    }
    const subject = input.subject;
    const description = input.description;
    
    const appInstance = getAppInstance();
    const automationService = appInstance.get(AutomationService);
    const spaceId = await automationService.getSpaceIdByRobotId(input.robotId);
    const queueSenderService = appInstance.get(QueueSenderBaseService);
    const message = {
      type: 1,
      creator: creator,
      subject: subject,
      description: description,
      executorIds: executorIds,
      participantIds: participantIds,
      spaceId: spaceId
    };
    queueSenderService.sendMessage(integrationQueueExchangeName, 'integration.message', message);
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
      "required": ["creator", "executors", "subject"],
      "properties": {
        "subject": {
          "type": "string",
				  "title": "待办标题"
        },
        "description": {
          "type": "string",
          "title": "待办备注",
          "description": "输入要创建钉钉待办任务的备注（输入英文斜杠「/」可插入变量）"
        },
        "creator": {
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
          },
          "title": "创建人",
          "description": "选择待办任务的创建人"
        },
        "executors": {
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
          "title": "执行人",
          "description": "选择待办任务的执行人"
        },
        "participants": {
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
          "title": "参与人",
          "description": "选择待办任务的参与人"
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
      "creator": {
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
              "showTitle": false,
              "isMulti": false
            }
          },
          "ui:order": ["type", "field", "member"],
          "ui:options": {
            "inline": true
          },
      },
      "executors": {
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
      "participants": {
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
      "ui:order": ["creator", "executors", "participants", "subject", "description"],
      "ui:options": {
        "layout": [
          ["creator", "executors", "participants", "subject", "description"],
        ]
      }
    };
  }
}