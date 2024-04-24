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

// Use antd first, then replace it with your own component
import { FieldType, IFilterCondition, IMemberField, OperatorEnums, getLiteralOperandValue } from '@apitable/core';
import { literal2Operand } from '../../../ui/utils';
import { IWidgetProps } from '../../interface';
import { FilterMember } from 'pc/components/tool_bar/view_filter/filter_value/filter_member';
import { op2fop } from 'pc/components/robot/robot_detail/trigger/helper';

export const FilterMemberWidget = ({ value, onChange }: IWidgetProps) => {
  const _value = getLiteralOperandValue(value);
  const _onChange = (e: any) => {
    const newValue = literal2Operand(e);
    onChange(newValue);
  };

  const field: IMemberField = {
    name: '',
    id: '',
    type: FieldType.Member,
    property: {
      isMulti: true, // Optional single or multiple members.
      shouldSendMsg: false, // Whether to send a message notification after selecting a member
      subscription: false,
      unitIds: []
    }
  };

  const condition: IFilterCondition<FieldType.Member> = {
    conditionId: 'random',
    fieldId: field.id,
    operator: op2fop(OperatorEnums.And),
    fieldType: field.type,
    value: _value,
  }
  
  return (
    <FilterMember
      field={field}
      condition={condition}
      onChange={_onChange}
    />
  );
};
