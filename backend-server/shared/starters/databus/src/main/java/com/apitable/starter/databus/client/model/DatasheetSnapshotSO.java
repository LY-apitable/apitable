/*
 * databus-server
 * databus-server APIs
 *
 * The version of the OpenAPI document: 1.6.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.apitable.starter.databus.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.apitable.starter.databus.client.model.DatasheetMetaSO;
import com.apitable.starter.databus.client.model.RecordSO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * DatasheetSnapshotSO
 */
@JsonPropertyOrder({
  DatasheetSnapshotSO.JSON_PROPERTY_DATASHEET_ID,
  DatasheetSnapshotSO.JSON_PROPERTY_META,
  DatasheetSnapshotSO.JSON_PROPERTY_RECORD_MAP
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class DatasheetSnapshotSO {
  public static final String JSON_PROPERTY_DATASHEET_ID = "datasheetId";
  private String datasheetId;

  public static final String JSON_PROPERTY_META = "meta";
  private DatasheetMetaSO meta;

  public static final String JSON_PROPERTY_RECORD_MAP = "recordMap";
  private Map<String, RecordSO> recordMap = new HashMap<>();

  public DatasheetSnapshotSO() {
  }

  public DatasheetSnapshotSO datasheetId(String datasheetId) {
    
    this.datasheetId = datasheetId;
    return this;
  }

   /**
   * Get datasheetId
   * @return datasheetId
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DATASHEET_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getDatasheetId() {
    return datasheetId;
  }


  @JsonProperty(JSON_PROPERTY_DATASHEET_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDatasheetId(String datasheetId) {
    this.datasheetId = datasheetId;
  }


  public DatasheetSnapshotSO meta(DatasheetMetaSO meta) {
    
    this.meta = meta;
    return this;
  }

   /**
   * Get meta
   * @return meta
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_META)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public DatasheetMetaSO getMeta() {
    return meta;
  }


  @JsonProperty(JSON_PROPERTY_META)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMeta(DatasheetMetaSO meta) {
    this.meta = meta;
  }


  public DatasheetSnapshotSO recordMap(Map<String, RecordSO> recordMap) {
    
    this.recordMap = recordMap;
    return this;
  }

  public DatasheetSnapshotSO putRecordMapItem(String key, RecordSO recordMapItem) {
    this.recordMap.put(key, recordMapItem);
    return this;
  }

   /**
   * Get recordMap
   * @return recordMap
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_RECORD_MAP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Map<String, RecordSO> getRecordMap() {
    return recordMap;
  }


  @JsonProperty(JSON_PROPERTY_RECORD_MAP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRecordMap(Map<String, RecordSO> recordMap) {
    this.recordMap = recordMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatasheetSnapshotSO datasheetSnapshotSO = (DatasheetSnapshotSO) o;
    return Objects.equals(this.datasheetId, datasheetSnapshotSO.datasheetId) &&
        Objects.equals(this.meta, datasheetSnapshotSO.meta) &&
        Objects.equals(this.recordMap, datasheetSnapshotSO.recordMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasheetId, meta, recordMap);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatasheetSnapshotSO {\n");
    sb.append("    datasheetId: ").append(toIndentedString(datasheetId)).append("\n");
    sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
    sb.append("    recordMap: ").append(toIndentedString(recordMap)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
