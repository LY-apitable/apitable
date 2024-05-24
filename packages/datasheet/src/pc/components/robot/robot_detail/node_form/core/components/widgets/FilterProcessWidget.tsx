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
import { DropdownSelect as Select } from '@apitable/components';
import { Api, Strings, t } from '@apitable/core';
import { literal2Operand, operand2Literal } from '../../../ui/utils';
import { IWidgetProps } from '../../interface';
import { useEffect, useState } from 'react';

export const FilterProcessWidget = ({ value, onChange }: IWidgetProps) => {
  const style = { width: '100%' };
  const [options, setOptions] = useState([]);

  useEffect(() => {
    if (options.length == 0) {
      Api.loadDingtalkProcess().then((res) => {
        const { data } = res;
        const processList = data.data;
        const optionList: any = [];
        processList.forEach(element => {
          const option = {label: element.flowTitle, value: element.processCode};
          optionList.push(option);
        });
        setOptions(optionList);
      });
    }
  }, [options, setOptions]);
  
  return (
    <>
      <Select
        options={(options || []) as any}
        value={operand2Literal(value)}
        onSelected={(option) => {
          onChange(literal2Operand(option.value));
        }}
        dropdownMatchSelectWidth
        noDataTip={t(Strings.no_option)}
        triggerStyle={style}
        placeholder={t(Strings.robot_select_option)}
      />
    </>
  );
};
