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

import React, { useCallback, useEffect, useState } from 'react';
import { EmptyNullOperand, ILiteralOperand, Strings, objectCombOperand, t } from '@apitable/core';
import {
  addNewFilter as _addNewFilter,
} from './helper';
import { Select } from '../select';
import { PropertyPath, isEqual, set } from 'lodash';
import { literal2Operand } from '../node_form/ui/utils';
import { getDataParameter } from 'pc/components/automation/controller/hooks/get_data_parameter';

interface IDateTimeSelectProps {
  value: any;
  onChange: (value: ILiteralOperand) => void;
}

const transformNullValue = (value?: any | null) => {
  const operands: any[] = [];
  operands.push('date');
  operands.push(literal2Operand('SameDay'));
  operands.push('time');
  operands.push(literal2Operand('09:30'));
  const initData = {
    type: 'Expression',
    value: {
      operator: 'newObject',
      operands: operands,
    },
  };
  return value == null || isEqual(value, EmptyNullOperand)
    ? initData
    : value;
};

export const DateDuration = [
  "SameDay",
  "AdvanceByDay1",
  "AdvanceByDay2",
  "AdvanceByDay3",
  "AdvanceByDay4",
  "AdvanceByDay5",
  "AdvanceByDay6",
  "AdvanceByDay7",
  "AdvanceByWeek1",
  "AdvanceByWeek2",
  "AdvanceByWeek3",
  "AdvanceByWeek4",
  "AdvanceByWeek5",
  "AdvanceByWeek6",
  "AdvanceByWeek7",
  "AdvanceByMonth1",
  "AdvanceByMonth2",
  "AdvanceByMonth3",
  "AdvanceByMonth4",
  "AdvanceByMonth5",
  "AdvanceByMonth6",
  "AdvanceByMonth7",
  "PostponeByDay1",
  "PostponeByDay2",
  "PostponeByDay3",
  "PostponeByDay4",
  "PostponeByDay5",
  "PostponeByDay6",
  "PostponeByDay7",
  "PostponeByWeek1",
  "PostponeByWeek2",
  "PostponeByWeek3",
  "PostponeByWeek4",
  "PostponeByWeek5",
  "PostponeByWeek6",
  "PostponeByWeek7",
  "PostponeByMonth1",
  "PostponeByMonth2",
  "PostponeByMonth3",
  "PostponeByMonth4",
  "PostponeByMonth5",
  "PostponeByMonth6",
  "PostponeByMonth7",
];

export const DateTimeSelect = (props: IDateTimeSelectProps) => {

  const { onChange } = props;

  const [value, setValue] = useState(transformNullValue(props.value));
  const updateValue = useCallback(
    
    (value: any) => {
      setValue(value);
      onChange(value);
    },
    [onChange]
  );

  useEffect(() => {
    setValue(transformNullValue(props.value));
  }, [props.value]);

  const handleDateChange = (date: string) => {
    // const _filter = JSON.parse(JSON.stringify(value));
    const newValue = {
      type: 'Expression',
      value: {
        operator: 'newObject',
        operands: objectCombOperand([...value.value.operands, 'date', literal2Operand(date)]),
      },
    };
    updateValue(newValue);
  };

  const handleTimeChange = (time: string) => {
    const newValue = {
      type: 'Expression',
      value: {
        operator: 'newObject',
        operands: objectCombOperand([...value.value.operands, 'time', literal2Operand(time)]),
      },
    };
    updateValue(newValue);
  };
  
  function createOptionData() {
    return DateDuration.map((item) => {
      if (item == 'SameDay') {
        return {
          label: t(Strings.SameDay),
          value: item,
        };
      } else {
        return {
          label: t(Strings[item.slice(0, -1)], { number: item.slice(-1) }),
          value: item,
        };
      }
    });
  }

  function createTimeOptionData() {
    const options:{label: string, value: string}[] = [];
    for (let hour = 0; hour < 24; hour++) {
      const hourStr = hour.toString().padStart(2, '0');
      const value1 = hourStr + ":00";
      const value2 = hourStr + ":30";
      options.push({
        label: value1,
        value: value1
      });
      options.push({
        label: value2,
        value: value2
      });
    }
    return options;
  }

  const date = getDataParameter<string>(value, 'date');
  const time = getDataParameter<string>(value, 'time');
  return (
    <>
      <div style={{ width: 200, display: 'inline-block'}}>
        <Select options={createOptionData()} value={date}
              onChange={(value) => handleDateChange(value)}/>
      </div>&nbsp;&nbsp;
      <div style={{ width: 100, display: 'inline-block'}}>
        <Select options={createTimeOptionData()} value={time}
              onChange={(value) => handleTimeChange(value)}/>
      </div>
    </>
  );
};
