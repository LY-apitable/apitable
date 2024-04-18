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
import { EmptyNullOperand, ILiteralOperand, OperandTypeEnums, Selectors } from '@apitable/core';
import { useAllColumnsOrEmpty } from 'pc/hooks';
import { useAppSelector } from 'pc/store/react-redux';
import { FieldSelect } from './field_select';
import {
  addNewFilter as _addNewFilter,
  getFields,
} from './helper';
import { isEqual, set } from 'lodash';

interface IDateFieldSelectProps {
  filter?: ILiteralOperand;
  datasheetId: string;
  onChange?: (filter: ILiteralOperand) => void;
  readonly: false;
}

const transformNullFilter = (filter?: ILiteralOperand | null) => {
  return filter == null || isEqual(filter, EmptyNullOperand)
    ? {
      type: OperandTypeEnums.Literal,
      value: ""
    }
    : filter;
};

export const DateFieldSelect = (props: IDateFieldSelectProps) => {

  const datasheetId = props.datasheetId;

  const { readonly = false, onChange } = props;

  const [filter, setFilter] = useState(transformNullFilter(props.filter));
  const updateFilter = useCallback(
    (filter: ILiteralOperand) => {
      setFilter(filter);
      if (onChange) {
        onChange(filter);
      }
    },
    [onChange]
  );
  
  useEffect(() => {
    setFilter(transformNullFilter(props.filter));
  }, [props.filter]);

  const columns = useAllColumnsOrEmpty(datasheetId);
  const snapshot = useAppSelector((state) => {
    return Selectors.getSnapshot(state, datasheetId)!;
  });

  // Here are all the fields, with or without permissions
  const fieldMap = snapshot?.meta?.fieldMap;
  const fields = getFields(columns!, fieldMap).filter(field => field.type == 5);

  const handleFilterChange = (value: ILiteralOperand) => {
    const _filter = JSON.parse(JSON.stringify(filter));
    set(_filter, 'value', value);
    updateFilter(_filter);
  };
  
  return (
    <>
      <FieldSelect fields={fields} disabled={readonly} value={filter.value}
        onChange={(value) => handleFilterChange(value)}/>
    </>
  );
};
