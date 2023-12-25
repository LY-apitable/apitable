/**
 * Api Document
 * Backend_Server Api Document
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { HttpFile } from '../http/http';

/**
* Login result VO
*/
export class LoginResultVO {
    /**
    * User registration sign
    */
    'isNewUser'?: boolean;

    static readonly discriminator: string | undefined = undefined;

    static readonly attributeTypeMap: Array<{name: string, baseName: string, type: string, format: string}> = [
        {
            "name": "isNewUser",
            "baseName": "isNewUser",
            "type": "boolean",
            "format": ""
        }    ];

    static getAttributeTypeMap() {
        return LoginResultVO.attributeTypeMap;
    }

    public constructor() {
    }
}

