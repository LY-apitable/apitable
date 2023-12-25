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

import {useEffect} from 'react';
import * as dd from 'dingtalk-jsapi';
import { Router } from 'pc/components/route_manager/router';
import { Navigation, Api} from '@apitable/core';
import { getSearchParams } from 'pc/utils';

const App = () => {
  useEffect(()=>{
    dd.ready(function () {
      const urlParams = getSearchParams();
      let appKey = urlParams.get('appkey');
      let corpId;
      // ts-ignore
      Api.getDingTalkCorpId(appKey)
        .then((result) => {
          if (result.data.code != 200) {
            alert(result.data.message);
            return;
          }
          corpId = result.data.data;
          dd.runtime.permission.requestAuthCode({
            corpId: corpId,
            onSuccess: function(result) {
              // ts-ignore
              Api.loginByDingTalk(appKey, result.code)
                .then(_response => {
                  Router.redirect(Navigation.WORKBENCH);
                })
                .catch(error => {
                  alert(JSON.stringify(error))
                })
            },
            onFail : function(err) {
              alert(JSON.stringify(err))
            }
          });
        });
    });
  },[])

};

export default App;
