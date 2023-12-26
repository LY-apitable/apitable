import axios from 'axios';
import * as Url from '../../shared/api/url';
import urlcat from 'urlcat';


/**
 * get DingTalk CoprId by AppKey
 * @param appKey
 */
export function getDingTalkCorpId(appKey: string) {
  return axios.get(Url.DINGTALK_CORPID, {
    params: {
      appKey,
    }
  });
}

/**
 * login by DingTalk
 * 
 * @param phone 
 * @param authCode 
 */
export function loginByDingTalk(appKey: string, authCode: string) {
  return axios.post(Url.DINGTALK_LOGIN, {
    appKey,
    authCode
  });
}

/**
 * DingTalk Org Sync
 * 
 * @param spaceId spaceId
 */
export const syncDingTalkOrg = (spaceId: string) => {
  return axios.post(urlcat(Url.DINGTALK_ORG_SYNC, { spaceId }));
};
