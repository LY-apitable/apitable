/**
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

import { FieldType, ICurrencyFieldProperty, IDateTimeFieldProperty, IEventInstance, IMeta, IOPEvent, IPercentFieldProperty, ISelectFieldProperty, NoticeTemplatesConstant, OPEventNameEnums, numberToShow, str2Currency, times } from '@apitable/core';
import { Injectable } from '@nestjs/common';
import { IOtEventContext } from 'database/ot/interfaces/ot.interface';
import { IEventExecutor, OTEventManager } from 'shared/event/ot.event.manager';
import { notificationQueueExchangeName } from 'shared/services/queue/queue.module';
import { QueueSenderBaseService } from 'shared/services/queue/queue.sender.base.service';
import { DatasheetRecordSubscriptionBaseService } from '../datasheet.record.subscription.base.service';
import { UnitMemberService } from 'unit/services/unit.member.service';
import { UserService } from 'user/services/user.service';
import { NodeService } from 'node/services/node.service';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetRecordService } from 'database/datasheet/services/datasheet.record.service';
import { dateTimeFormat } from '@apitable/core/dist/model/field/date_time_base_field';
import { UnitService } from 'unit/services/unit.service';

@Injectable()
export class SubscriptionEvent implements IEventExecutor {
  constructor(
    private readonly queueSenderService: QueueSenderBaseService,
    private readonly unitMemberService: UnitMemberService,
    private readonly unitService: UnitService,
    private readonly userService: UserService,
    private readonly datasheetRecordSubscriptionBaseService: DatasheetRecordSubscriptionBaseService,
    private readonly nodeService: NodeService,
    private readonly datasheetMetaService: DatasheetMetaService,
    private readonly datasheetRecordService: DatasheetRecordService,
  ) {
    OTEventManager.addExecutor('SubscriptionEvent', this);
  }

  execute(events: IEventInstance<IOPEvent>[], context?: IOtEventContext): void {
    events.forEach((event) => {
      switch (event.eventName) {
        case OPEventNameEnums.CellUpdated:
          const eventContext = event.context;
          const recordId = eventContext.recordId;
          const fieldId = eventContext.fieldId;
          const datasheetId = eventContext.datasheetId;
          const change = eventContext.change;
          const spaceId = context?.spaceId!;
          const fromUserId = context?.operatorUserId!;
          this.sendMessage(spaceId, datasheetId, recordId, fieldId, fromUserId, change);
          break;
        case OPEventNameEnums.RecordCreated:
          break;
        case OPEventNameEnums.RecordDeleted:
          break;
        case OPEventNameEnums.RecordUpdated:
          break;
        case OPEventNameEnums.RecordCommentUpdated:
          break;
        case OPEventNameEnums.RecordArchived:
          break;
        case OPEventNameEnums.RecordUnarchived:
          break;
      }
    });
  }

  async sendMessage(spaceId: string, dstId: string, recordId: string, fieldId: string, fromUserId: string, change: any): Promise<void> {
    const subscriptions = await this.datasheetRecordSubscriptionBaseService.getSubscriptionsByRecordId(dstId, recordId);
    const toUserIds = subscriptions.filter(subscription => fromUserId != subscription.createdBy).map(subscription => subscription.createdBy);
    const memberInfos = await this.unitMemberService.getMembersBaseInfoBySpaceIdAndUserIds(spaceId, toUserIds, false);
    const memberName = await this.userService.getUserMemberName(fromUserId, spaceId);
    const nodeName = await this.nodeService.getNameByNodeId(dstId);
    const meta = await this.datasheetMetaService.getMetaDataByDstId(dstId);
    const primaryFieldId = meta.views[0]!.columns[0]!.fieldId;
    const records = await this.datasheetRecordService.getRecordsByDstIdAndRecordIds(dstId, [recordId]);
    const record = records[recordId];
    const recordTitle = record?.data[primaryFieldId]![0]['text'];

    const viewId = meta.views[0]?.id;
    const fieldName = meta.fieldMap[fieldId]?.name;

    const result = await this.getColumnTextByType(spaceId, meta, fieldId, change);
    if (result.isBreak) {
      return;
    }
    
    const toMemberIds: string[] = [];
    toUserIds.forEach(toUserId => {
      const memberId = memberInfos[toUserId]?.memberId;
      if  (memberId != undefined) {
        toMemberIds.push(memberId);
      }
    })
    const message = {
      nodeId: dstId,
      recordId,
      fieldId,
      spaceId: spaceId,
      body: {
        extras: {
          recordTitle: recordTitle,
          oldDisplayValue: result['from'],
          newDisplayValue: result['to'],
          viewId: viewId,
          recordIds: [recordId],
          memberName: memberName,
          nodeName: nodeName,
          endAt: Date.now(),
          fieldName
        },
      },
      templateId: NoticeTemplatesConstant.subscribed_record_cell_updated,
      toMemberId: toMemberIds,
      fromUserId: fromUserId
    };
    this.queueSenderService.sendMessage(notificationQueueExchangeName, 'notification.message', message);
  }

  async getColumnTextByType(spaceId: string, meta: IMeta, fieldId: string, change: any) {
    const fieldMap = meta.fieldMap;
    const field = fieldMap[fieldId]!;
    const from = change['from'];
    const to = change['to'];
    let fromValue = "";
    let toValue = "";
    let isBreak = false;
    switch(field.type) {
      case FieldType.SingleText:
      case FieldType.Text:
      case FieldType.Email:
      case FieldType.Phone:
        if (from != undefined) {
          fromValue = from[0]['text'];
        }
        if (to != undefined) {
          toValue = to[0]['text'];
        }
        break;
      case FieldType.Number:
      case FieldType.Rating:
        if (from != undefined) {
          fromValue = from;
        }
        if (to != undefined) {
          toValue = to;
        }
        break;
      case FieldType.SingleSelect:
      case FieldType.MultiSelect:
        const selectProperty = meta.fieldMap[fieldId]?.property as ISelectFieldProperty;
        const optionMap: { [id: string]: string } = selectProperty.options.reduce((map: { [id: string]: string; }, option) => {
          map[option.id] = option.name;
          return map;
        }, {});
        if (from != undefined) {
          const nameArr: string[] = [];
          from.forEach((id: string) => {
            nameArr.push(optionMap[id]!);
          })
          fromValue = nameArr.join();
        }
        if (to != undefined) {
          const nameArr: string[] = [];
          to.forEach((id: string) => {
            nameArr.push(optionMap[id]!);
          })
          toValue = nameArr.join();
        }
        break;
      case FieldType.DateTime:
        const dateProperty = meta.fieldMap[fieldId]?.property as IDateTimeFieldProperty;
        if (from != undefined) {
          fromValue = dateTimeFormat(from, dateProperty)!;
        }
        if (to != undefined) {
          toValue = dateTimeFormat(to, dateProperty)!;
        }
        break;
      case FieldType.Attachment:
        if (from != undefined) {
          const nameArr: string[] = [];
          from.forEach((file: { name: string; }) => {
            nameArr.push(file.name);
          })
          fromValue = nameArr.join();
        }
        if (to != undefined) {
          const nameArr: string[] = [];
          to.forEach((file: { name: string; }) => {
            nameArr.push(file.name);
          })
          toValue = nameArr.join();
        }
        break;
      case FieldType.URL:
        if (from != undefined) {
          fromValue = from[0]['text'];
        }
        if (to != undefined) {
          toValue = to[0]['text'];
        }
        isBreak = fromValue == toValue;
        break;
      case FieldType.Checkbox:
        fromValue = from ? "是" : "否";
        toValue = to ? "是" : "否";
        break;
      case FieldType.Member:
        if (from != undefined) {
          const idArr: string[] = [];
          from.forEach((unitId: string) => {
            idArr.push(unitId);
          })
          const unitInfos = await this.unitService.getMembersByUnitIds(spaceId, idArr);
          const nameArr: string[] = [];
          for (let unitId in unitInfos) {
            const infos = unitInfos[unitId];
            nameArr.push(infos![0].name);
          }
          fromValue = nameArr.join();
        }
        if (to != undefined) {
          const idArr: string[] = [];
          to.forEach((unitId: string) => {
            idArr.push(unitId);
          })
          const unitInfos = await this.unitService.getMembersByUnitIds(spaceId, idArr);
          const nameArr: string[] = [];
          for (let unitId in unitInfos) {
            const infos = unitInfos[unitId];
            nameArr.push(infos![0].name);
          }
          toValue = nameArr.join();
        }
        break;
      case FieldType.Currency:
        const currencyProperty = meta.fieldMap[fieldId]?.property as ICurrencyFieldProperty;
        if (from != undefined) {
          fromValue = numberToShow(from, currencyProperty.precision)!;
          fromValue = str2Currency(fromValue, currencyProperty.symbol, 3)!;
        }
        if (to != undefined) {
          toValue = numberToShow(to, currencyProperty.precision)!;
          toValue = str2Currency(toValue, currencyProperty.symbol, 3)!;
        }
        break;
      case FieldType.Percent:
        const percentProperty = meta.fieldMap[fieldId]?.property as IPercentFieldProperty;
        if (from != undefined) {
          fromValue = numberToShow(times(from, 100), percentProperty.precision)! + '%';
        }
        if (to != undefined) {
          toValue = numberToShow(times(to, 100), percentProperty.precision)! + '%';
        }
        break;
      case FieldType.Link:
      case FieldType.LookUp:
      case FieldType.Formula:
      case FieldType.AutoNumber: //AUTO_NUMBER
      case FieldType.CreatedTime: //CREATED_TIME
      case FieldType.LastModifiedTime: //LAST_MODIFIED_TIME
      case FieldType.CreatedBy: //CREATED_BY
      case FieldType.LastModifiedBy: //LAST_MODIFIED_BY
      case FieldType.Cascader: //CASCADER
      case FieldType.OneWayLink: //ONE_WAY_LINK
      case FieldType.WorkDoc: //WORK_DOC
      case FieldType.Button: //BUTTON
        isBreak = true;
        break;
    }
    return {
      from: fromValue,
      to: toValue,
      isBreak
    };
  }
}
