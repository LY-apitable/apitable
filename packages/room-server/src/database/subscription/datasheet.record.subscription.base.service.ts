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

import { IRemoteChangeset } from '@apitable/core';
import { Injectable } from '@nestjs/common';
import { ICommonData } from 'database/ot/interfaces/ot.interface';
import { isEmpty } from 'lodash';
import { DatasheetRecordSubscriptionEntity } from './entities/datasheet.record.subscription.entity';
import { SubscriptionRepository } from './repositories/subscription.repository';
import { UnitService } from 'unit/services/unit.service';

@Injectable()
export class DatasheetRecordSubscriptionBaseService {
  constructor(
    private readonly unitService: UnitService,
    private readonly subscriptionRepository: SubscriptionRepository,
  ) { }

  public async subscribeDatasheetRecords(_userId: string, _dstId: string, recordIds: string[], _mirrorId?: string | null) {
    if (isEmpty(recordIds)) return;
    for (const recordId of recordIds) {
      const newSubscription = this.subscriptionRepository.create({
        dstId: _dstId,
        recordId,
        createdBy: _userId,
        updatedBy: _userId
      });
      if (_mirrorId != null) {
        newSubscription.mirrorId = _mirrorId;
      }
      if (!await this.subscriptionRepository.checkeSubscribe(_userId, _dstId, recordId)) {
        await this.subscriptionRepository.save(newSubscription);
      }
    }
  }

  public async unsubscribeDatasheetRecords(_userId: string, _dstId: string, recordIds: string[]) {
    if (isEmpty(recordIds)) return;
    for (const recordId of recordIds) {
      await this.subscriptionRepository.unsubscribeDatasheetRecords(_userId, _dstId, recordId);
    }
  }

  public async getSubscribedRecordIds(_userId: string, _dstId: string): Promise<string[]> {
    return await this.subscriptionRepository.getSubscribedRecordIds(_userId, _dstId);
  }

  public async getSubscriptionsByRecordId(_dstId: string, _recordId: string): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await this.subscriptionRepository.getSubscriptionsByRecordId(_dstId, _recordId);
  }

  public async getSubscriptionsByRecordIds(_dstId: string, _recordIds: string[]): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await Promise.resolve([]);
  }

  public async handleChangesets(_changesets: IRemoteChangeset[], _context: any) {
    await Promise.resolve();
  }

  public async handleRecordAutoSubscriptions(
    _commonData: ICommonData,
    _resultSet: { [key: string]: any },
  ) {
    const dstId = _commonData.dstId;
    const toCreateRecordSubscriptions = _resultSet.toCreateRecordSubscriptions;
    const toCancelRecordSubscriptions = _resultSet.toCancelRecordSubscriptions;
    const creatorAutoSubscribedRecordIds = _resultSet.creatorAutoSubscribedRecordIds;
    const unitIdSet = new Set<string>();
    const subscribeMap = new Map<string, Set<string>>();
    const unSubscribeMap = new Map<string, Set<string>>();
    const creatorSubscribedRecordIds: string[] = [];
    toCreateRecordSubscriptions.forEach((subscribeInfo: object) => {
      const unitId = subscribeInfo['unitId'];
      const recordId = subscribeInfo['recordId'];
      unitIdSet.add(unitId);
      if (subscribeMap.has(unitId)) {
        const recordIdSet = subscribeMap.get(unitId);
        recordIdSet?.add(recordId);
      } else {
        subscribeMap.set(unitId, new Set([recordId]));
      }
    });
    
    toCancelRecordSubscriptions.forEach((unSubscribeInfo: object) => {
      const unitId = unSubscribeInfo['unitId'];
      const recordId = unSubscribeInfo['recordId'];
      unitIdSet.add(unitId);
      if (unSubscribeMap.has(unitId)) {
        const recordIdSet = unSubscribeMap.get(unitId);
        recordIdSet?.add(recordId);
      } else {
        unSubscribeMap.set(unitId, new Set([recordId]));
      }
    });

    creatorAutoSubscribedRecordIds.forEach((createRecordId: string) => {
      creatorSubscribedRecordIds.push(createRecordId);
    });
    if (unitIdSet.size > 0) {
      const memberMap = new Map<string, string>();
      const memberInfos = await this.unitService.getUnitMemberInfoByIds(Array.from(unitIdSet));
      memberInfos.forEach((memberInfo: object) => {
        memberMap.set(memberInfo['unitId'], memberInfo['userId']);
      });
      if (subscribeMap.size > 0) {
        for (const [unitId, recordIdSet] of subscribeMap.entries()) {
          const userId = memberMap.get(unitId);
          if (userId != undefined) {
            this.subscribeDatasheetRecords(userId, dstId, Array.from(recordIdSet));
          }
        }
      }
      if (unSubscribeMap.size > 0) {
        for (const [unitId, recordIdSet] of unSubscribeMap.entries()) {
          const userId = memberMap.get(unitId);
          if (userId != undefined) {
            this.unsubscribeDatasheetRecords(userId, dstId, Array.from(recordIdSet));
          }
        }
      }
    }
    if (creatorSubscribedRecordIds.length > 0) {
      this.subscribeDatasheetRecords(_commonData.userId!, dstId, creatorSubscribedRecordIds);
    }
  }
}
