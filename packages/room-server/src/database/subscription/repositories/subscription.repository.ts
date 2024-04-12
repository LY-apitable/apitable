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

import { EntityRepository, Repository } from 'typeorm';
import { DatasheetRecordSubscriptionEntity } from '../entities/datasheet.record.subscription.entity';

@EntityRepository(DatasheetRecordSubscriptionEntity)
export class SubscriptionRepository extends Repository<DatasheetRecordSubscriptionEntity> {
  public async getSubscribedRecordIds(userId: string, dstId: string): Promise<string[]> {
    const result = await this.find({
      select: ['recordId'],
      where: {
        isDeleted: 0,
        createdBy: userId,
        dstId
      }
    });
    return result.map((i) => i.recordId);
  }

  public async unsubscribeDatasheetRecords(userId: string, dstId: string, recordId: string) {
    return await this.update({
      createdBy: userId,
      dstId,
      recordId
    }, {
      isDeleted: true,
    });
  }

  public async checkeSubscribe(userId: string, dstId: string, recordId: string): Promise<boolean> {
    const count = await this.count({
      where: {
        isDeleted: 0,
        createdBy: userId,
        dstId,
        recordId
      }
    });
    return count > 0;
  }

  public async getSubscriptionsByRecordId(dstId: string, recordId: string): Promise<DatasheetRecordSubscriptionEntity[]> {
    return await this.find({
      where: {
        isDeleted: 0,
        dstId,
        recordId
      }
    });
  }
}
