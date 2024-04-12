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

import { DynamicModule, Module, forwardRef } from '@nestjs/common';
import { DatasheetRecordSubscriptionBaseService } from 'database/subscription/datasheet.record.subscription.base.service';
import path from 'path';
import * as fs from 'fs';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SubscriptionRepository } from './repositories/subscription.repository';
import { UnitModule } from 'unit/unit.module';
import { SubscriptionEvent } from './events/subscription.event';
import { UserModule } from 'user/user.module';
import { NodeModule } from 'node/node.module';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetMetaRepository } from 'database/datasheet/repositories/datasheet.meta.repository';
import { UserService } from 'user/services/user.service';
import { RecordCommentService } from 'database/datasheet/services/record.comment.service';
import { DatasheetRecordArchiveRepository } from 'database/datasheet/repositories/datasheet.record.archive.repository';
import { DatasheetChangesetService } from 'database/datasheet/services/datasheet.changeset.service';
import { DatasheetRecordService } from 'database/datasheet/services/datasheet.record.service';
import { RecordCommentRepository } from 'database/datasheet/repositories/record.comment.repository';
import { DatasheetChangesetRepository } from 'database/datasheet/repositories/datasheet.changeset.repository';
import { DatasheetRecordRepository } from 'database/datasheet/repositories/datasheet.record.repository';
import { CommandOptionsService } from 'database/command/services/command.options.service';
import { CommandService } from 'database/command/services/command.service';
import { UserRepository } from 'user/repositories/user.repository';
import { UnitMemberRepository } from 'unit/repositories/unit.member.repository';

@Module({
  imports: [
    UnitModule,
    UserModule,
    forwardRef(() => NodeModule),
    TypeOrmModule.forFeature([
      SubscriptionRepository,
      DatasheetMetaRepository,
      RecordCommentRepository,
      DatasheetRecordArchiveRepository,
      RecordCommentRepository,
      DatasheetChangesetRepository,
      DatasheetRecordRepository,
      UserRepository,
      UnitMemberRepository,
    ]),
  ],
  providers: [
    SubscriptionEvent,
    DatasheetMetaService,
    DatasheetRecordService,
    RecordCommentService,
    DatasheetChangesetService,
    UserService,
    CommandOptionsService,
    CommandService,
    {
      provide: DatasheetRecordSubscriptionBaseService,
      useClass: class SubscriptionService extends DatasheetRecordSubscriptionBaseService {}
    },
  ],
  exports: [
    {
      provide: DatasheetRecordSubscriptionBaseService,
      useClass: class SubscriptionService extends DatasheetRecordSubscriptionBaseService {}
    },
  ]
})
export class SubscriptionDynamicModule { 
  static forRoot(): DynamicModule {
    const subscriptionEnterpriseModulePath = path.join(__dirname, '../../enterprise/database/subscription');
    const isEnterpriseLevel: boolean = fs.existsSync(subscriptionEnterpriseModulePath);
    if (isEnterpriseLevel) {
      const { SubscriptionEnterpriseModule } = require(`${subscriptionEnterpriseModulePath}/subscription.enterprise.module`);
      return {
        module: SubscriptionEnterpriseModule,
      };
    }
    return { 
      module: SubscriptionDynamicModule,
    }; 

  }
}
