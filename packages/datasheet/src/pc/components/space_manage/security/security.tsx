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

import { FC, useEffect, useState } from 'react';
import { Api, ISpaceFeatures, Strings, t } from '@apitable/core';
import styles from './style.module.less';
import { Switch, Typography } from '@apitable/components';
import { Radio, RadioChangeEvent } from 'antd';
import { useRequest } from 'pc/hooks';

export const Security: FC<React.PropsWithChildren<unknown>> = () => {
  const { data: result } = useRequest(Api.getSpaceFeaturesNew);
  const [spaceFeatures, setSpaceFeatures] = useState<ISpaceFeatures | undefined>();

  useEffect(() => {
    if (result?.data.data) {
      setSpaceFeatures(result.data.data);
    }
  }, [result]);

  if (!spaceFeatures) {
    return null;
  }

  const updateSpaceFeatures = async (spaceFeatures: ISpaceFeatures) => {
    setSpaceFeatures(spaceFeatures);
    await Api.updateSecuritySetting(spaceFeatures);
  };

  const switchRootManageable = (checked: boolean) => {
    const newSpaceFeatures = { ...spaceFeatures, rootManageable: !checked };
    updateSpaceFeatures(newSpaceFeatures);
  };

  const switchExportLevel = (checked: boolean) => {
    const newSpaceFeatures = { ...spaceFeatures, exportLevel: checked ? 1 : 0 };
    updateSpaceFeatures(newSpaceFeatures);
  };

  const onExportLevelChange = (e: RadioChangeEvent) => {
    const newSpaceFeatures = { ...spaceFeatures, exportLevel: e.target.value };
    updateSpaceFeatures(newSpaceFeatures);
  };

  const switchDownload = (checked: boolean) => {
    const newSpaceFeatures = { ...spaceFeatures, allowDownloadAttachment: !checked };
    updateSpaceFeatures(newSpaceFeatures);
  };

  const switchCopy = (checked: boolean) => {
    const newSpaceFeatures = { ...spaceFeatures, allowCopyDataToExternal: !checked };
    updateSpaceFeatures(newSpaceFeatures);
  };

  const switchWatermark = (checked: boolean) => {
    const newSpaceFeatures = { ...spaceFeatures, watermarkEnable: checked };
    updateSpaceFeatures(newSpaceFeatures);
  };

  return (
    <div className={styles.securityContainer}>
      <Typography variant={'h1'}>
        {t(Strings.permission_and_security)}
      </Typography>
      <Typography variant={'body2'} className={styles.pageSubscribe}>
        {t(Strings.permission_and_security_content)}
      </Typography>
      <div className={styles.content}>
        <div className={styles.optionSection}>
          {/* 禁止成员在根目录增删文件 */}
          <div className={styles.optionItem} style={{ maxWidth: '820px'}}>
            <div className={styles.switchInfo}>
              <div className={styles.switchInfoTop}>
                <Switch size="small" checked={!spaceFeatures.rootManageable} onChange={switchRootManageable} />
                <span className={styles.switchText}>{t(Strings.security_setting_catalog_management_title)}</span>
              </div>
              <div>
                <ul>
                  <li>
                    {t(Strings.security_setting_catalog_management_description)}
                  </li>
                </ul>
              </div>
            </div>
          </div>
          {/* 指定成员可导出表格和视图 */}
          <div className={styles.optionItem} style={{ maxWidth: '820px'}}>
            <div className={styles.switchInfo}>
              <div className={styles.switchInfoTop}>
                <Switch size="small" checked={spaceFeatures.exportLevel != 0} onChange={switchExportLevel} />
                <span className={styles.switchText}>{t(Strings.security_setting_export_data_title)}</span>
              </div>
              <div>
                <ul>
                  <li>
                    {t(Strings.security_setting_export_data_description)}
                  </li>
                </ul>
              </div>
              <div className={styles.radioGroup}>
                <Radio.Group value={spaceFeatures.exportLevel} disabled={spaceFeatures.exportLevel == 0} onChange={onExportLevelChange}>
                  <Radio value={1}>
                    <span>
                      {t(Strings.security_setting_export_data_read_only)}
                    </span>
                  </Radio>
                  <Radio value={4}>
                    <span>
                      {t(Strings.security_setting_export_data_updatable)}
                    </span>
                  </Radio>
                  <Radio value={2}>
                    <span>
                      {t(Strings.security_setting_export_data_editable)}
                    </span>
                  </Radio>
                  <Radio value={3}>
                    <span>
                      {t(Strings.security_setting_export_data_manageable)}
                    </span>
                  </Radio>
                </Radio.Group>
              </div>
            </div>
          </div>
          {/* 禁止「只可阅读」用户下载附件 */}
          <div className={styles.optionItem} style={{ maxWidth: '820px'}}>
            <div className={styles.switchInfo}>
              <div className={styles.switchInfoTop}>
                <Switch size="small" checked={!spaceFeatures.allowDownloadAttachment} onChange={switchDownload} />
                <span className={styles.switchText}>{t(Strings.security_setting_download_file_title)}</span>
              </div>
              <div>
                <ul>
                  <li>
                    {t(Strings.security_setting_download_file_description)}
                  </li>
                </ul>
              </div>
            </div>
          </div>
          {/* 禁止用户复制数据 */}
          <div className={styles.optionItem} style={{ maxWidth: '820px'}}>
            <div className={styles.switchInfo}>
              <div className={styles.switchInfoTop}>
                <Switch size="small" checked={!spaceFeatures.allowCopyDataToExternal} onChange={switchCopy} />
                <span className={styles.switchText}>{t(Strings.security_setting_prohibit_copy_cell_data_title)}</span>
              </div>
              <div>
                <ul>
                  <li>
                    {t(Strings.security_setting_prohibit_copy_cell_data_description)}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div className={styles.optionSection}>
          {/* 显示全局水印 */}
          <div className={styles.optionItem} style={{ maxWidth: '820px'}}>
            <div className={styles.switchInfo}>
              <div className={styles.switchInfoTop}>
                <Switch size="small" checked={spaceFeatures.watermarkEnable} onChange={switchWatermark} />
                <span className={styles.switchText}>{t(Strings.security_show_watermark)}</span>
              </div>
              <div>
                <ul>
                  <li>
                    {t(Strings.security_show_watermark_description)}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
