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

import { useAtomValue } from 'jotai';
import * as React from 'react';
import { memo, useCallback, useRef, useState } from 'react';
import { useSelector } from 'react-redux';
import useSWR from 'swr';
import { Dropdown, IDropdownControl, ScreenSize, useThemeColors } from '@apitable/components';
import { EmptyNullOperand, Selectors, Strings, t } from '@apitable/core';
import { automationStateAtom } from 'pc/components/automation/controller';
import {
  getTriggerDatasheetId2,
  IFetchedDatasheet,
  useAutomationFieldInfo
} from '../../../automation/controller/hooks/use_robot_fields';
import { AutomationScenario, INodeOutputSchema, ITriggerType } from '../../interface';
import { IWidgetProps } from '../node_form/core/interface';
import { enrichDatasheetTriggerOutputSchemaForDate } from './helper';
import { MagicVariableContainer } from './magic_variable_container';
import styles from 'pc/components/tool_bar/view_filter/filter_value/style.module.less';
import { ComponentDisplay } from 'pc/components/common/component_display';
import { CONST_INITIAL_DROPDOWN_INDEX } from '@apitable/components/dist/components/dropdown/float_ui/useFloatUiDropdown';
import classNames from 'classnames';
import { ChevronDownOutlined } from '@apitable/icons';
import { Popup } from 'pc/components/common/mobile/popup';
import { useClickAway } from 'ahooks';
import { OptionalCellContainer } from 'pc/components/multi_grid/cell/optional_cell_container/optional_cell_container';
import { MouseDownType } from 'pc/components/multi_grid';
import { MagicVariableElement } from './magic_variable_element';
import { getDataParameter } from 'pc/components/automation/controller/hooks/get_data_parameter';

type IMagicTextDateFieldProps = IWidgetProps & {
  nodeOutputSchemaList: INodeOutputSchema[];
  value: any;
  onChange?: (value: any) => void;
  isOneLine?: boolean;
  triggerType: ITriggerType | null;
  triggerDataSheetMap: TriggerDataSheetMap
};

export type TriggerDataSheetMap = Record<string, string>;

export const MagicTextDateField = memo((props: IMagicTextDateFieldProps) => {
  const colors = useThemeColors();
  const { onChange, schema, triggerDataSheetMap } = props;
  const isJSONField = (schema as any)?.format === 'json';
  const [isOpen, setOpenState] = useState(false);

  const triggerControlRef = useRef<IDropdownControl | null>(null);

  const setOpen = useCallback(
    (isOpen: boolean) => {
      setOpenState(isOpen);
      if (isOpen) {
        triggerControlRef?.current?.open();
      } else {
        triggerControlRef?.current?.close();
      }
    },
    [setOpenState],
  );

  const state = useAtomValue(automationStateAtom);
  const [value, setValue] = useState(props.value);
  const [visible, setVisible] = useState(false);

  const triggers = state?.robot?.triggers ?? [];

  const { data: dataList2 } = useSWR(['getTriggersRelatedDatasheetId2', triggers], () => getTriggerDatasheetId2(props.nodeOutputSchemaList.map(r => r.id)), {});

  const activeDstId = useSelector(Selectors.getActiveDatasheetId);
  const dataLis: IFetchedDatasheet[] =
    state?.scenario === AutomationScenario?.datasheet
      ? Array.from({ length: props.nodeOutputSchemaList.length }, () => activeDstId)
      : ((dataList2 ?? []) as IFetchedDatasheet[]);

  // @ts-ignore
  const fieldInfoList = useAutomationFieldInfo(dataLis);
  const nodeOutputSchemaList = props.nodeOutputSchemaList.map((nodeOutputSchema, index) => {
    const item = fieldInfoList[index];
    if (nodeOutputSchema?.id.startsWith('dst') && item && item?.fields?.length && item.fieldPermissionMap) {
      return enrichDatasheetTriggerOutputSchemaForDate(nodeOutputSchema, item.fields, item.fieldPermissionMap!);
    }
    return nodeOutputSchema;
  });

  // TODO Type inconsistency (e.g. magic lookup switching type) to be corrected after the change operation.
  const refSelect = useRef<HTMLDivElement>(null);
  const refSelectItem = useRef<HTMLDivElement>(null);
  useClickAway(() => setVisible(false), [refSelect, refSelectItem], 'click');

  function renderPopup() {
    return (
      <MagicVariableContainer
        isJSONField={isJSONField}
        insertMagicVariable={(data) => {
          setOpen(false);
          const formatData = {
            "type": "Expression",
            "value": {
              "operands": [{
                "type": "Expression",
                "value": data
              }, {
                "type": "Literal",
                "value": ["id"]
              }],
              "operator": "getObjectProperty"
            }
          };
          setValue(formatData);
          onChange && onChange(formatData);
        }}
        nodeOutputSchemaList={nodeOutputSchemaList}
        setOpen={(isOpen) => {
        }}
      />
    );
  }

  async function onMouseDown(e: React.MouseEvent<HTMLDivElement>) {
    if (e.button === MouseDownType.Right) {
      return;
    }
  }

  const renderElement = (props: any) => {
    return <MagicVariableElement {...props} nodeOutputSchemaList={nodeOutputSchemaList} triggerDataSheetMap={triggerDataSheetMap}/>;
  };

  function returnSingle(content: any) {
    if (JSON.stringify(content) != JSON.stringify(EmptyNullOperand)) {
      const arr: any[] = content['value']['operands'];
      let value: any = {};
      arr.forEach(item => {
        if (item['type'] == "Expression") {
          value = item['value'];
        }
      })
      return renderElement({element: {
        type: 'magicVariable',
        data: value,
        children: [{ text: '' }],
      }});
    }
    return <></>
  }

  return (
    <div className={styles.select} ref={refSelect}>
      <ComponentDisplay minWidthCompatible={ScreenSize.md}>
        <Dropdown
          options={{ autoWidth: true, zIndex:CONST_INITIAL_DROPDOWN_INDEX }}
          trigger={
            <div className={classNames(styles.displayBox, styles.option)}>
              {!value ? (
                <div className={styles.placeholder}>xxxxxx</div>
              ) : (
                <OptionalCellContainer
                  onMouseDown={onMouseDown}
                  displayMinWidth={Boolean(true)}
                >
                  {returnSingle(value as string)}
                </OptionalCellContainer>
              )}
              <div className={styles.iconArrow}>
                <ChevronDownOutlined color={colors.black[500]} />
              </div>
            </div>
          }
        >
          {() => renderPopup()}
        </Dropdown>
      </ComponentDisplay>

      <ComponentDisplay maxWidthCompatible={ScreenSize.md}>
        <div
          className={classNames(styles.displayBox, styles.option)}
          onClick={() => setVisible(!visible)}
        >
          <OptionalCellContainer
            onMouseDown={onMouseDown}
            displayMinWidth={Boolean(true)}
          >
            {returnSingle(value as string)}
          </OptionalCellContainer>
          <div className={styles.iconArrow}>
            <ChevronDownOutlined size={16} color={colors.fourthLevelText} />
          </div>
        </div>
        <Popup
          title={t(Strings.please_choose)}
          height="90%"
          open={visible}
          onClose={() => setVisible(false)}
          className={styles.filterGeneralPopupWrapper}
        >
          {renderPopup()}
        </Popup>
      </ComponentDisplay>
    </div>
  );
});
