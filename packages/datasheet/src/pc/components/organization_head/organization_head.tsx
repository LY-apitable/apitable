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

import classnames from 'classnames';
import * as React from 'react';
import { useSelector } from 'react-redux';
import { Tooltip } from '../common/tooltip';
import styles from './style.module.less';

export interface IOrganizationHeadProps {
  className?: string;
  hideTooltip?: boolean;
}

export const OrganizationHead: React.FC<React.PropsWithChildren<IOrganizationHeadProps>> = ({ className, hideTooltip = false }) => {
  const spaceName = useSelector((state) => state.space.curSpaceInfo?.spaceName);
  return (
    <div className={classnames(styles.organization, className)}>
      <h2 className={styles.orgName}>个人工作台</h2>

      {/* {hideTooltip ? (
        <h2 className={styles.orgName}>{spaceName}</h2>
      ) : (
        <Tooltip title={spaceName} textEllipsis>
          <h2 className={styles.orgName}>{spaceName}</h2>
        </Tooltip>
      )} */}
    </div>
  );
};
