/*
 * databus-server
 * databus-server APIs
 *
 * The version of the OpenAPI document: 1.7.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.apitable.starter.databus.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.apitable.starter.databus.client.model.AlarmUser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * RecordAlarm
 */
@JsonPropertyOrder({
  RecordAlarm.JSON_PROPERTY_ALARM_AT,
  RecordAlarm.JSON_PROPERTY_ALARM_USERS,
  RecordAlarm.JSON_PROPERTY_FIELD_ID,
  RecordAlarm.JSON_PROPERTY_ID,
  RecordAlarm.JSON_PROPERTY_RECORD_ID,
  RecordAlarm.JSON_PROPERTY_SUBTRACT,
  RecordAlarm.JSON_PROPERTY_TIME
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class RecordAlarm {
  public static final String JSON_PROPERTY_ALARM_AT = "alarmAt";
  private Long alarmAt;

  public static final String JSON_PROPERTY_ALARM_USERS = "alarmUsers";
  private List<AlarmUser> alarmUsers;

  public static final String JSON_PROPERTY_FIELD_ID = "fieldId";
  private String fieldId;

  public static final String JSON_PROPERTY_ID = "id";
  private String id;

  public static final String JSON_PROPERTY_RECORD_ID = "recordId";
  private String recordId;

  public static final String JSON_PROPERTY_SUBTRACT = "subtract";
  private String subtract;

  public static final String JSON_PROPERTY_TIME = "time";
  private String time;

  public RecordAlarm() {
  }

  public RecordAlarm alarmAt(Long alarmAt) {
    
    this.alarmAt = alarmAt;
    return this;
  }

   /**
   * Get alarmAt
   * minimum: 0
   * @return alarmAt
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ALARM_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Long getAlarmAt() {
    return alarmAt;
  }


  @JsonProperty(JSON_PROPERTY_ALARM_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAlarmAt(Long alarmAt) {
    this.alarmAt = alarmAt;
  }


  public RecordAlarm alarmUsers(List<AlarmUser> alarmUsers) {
    
    this.alarmUsers = alarmUsers;
    return this;
  }

  public RecordAlarm addAlarmUsersItem(AlarmUser alarmUsersItem) {
    if (this.alarmUsers == null) {
      this.alarmUsers = new ArrayList<>();
    }
    this.alarmUsers.add(alarmUsersItem);
    return this;
  }

   /**
   * Get alarmUsers
   * @return alarmUsers
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ALARM_USERS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<AlarmUser> getAlarmUsers() {
    return alarmUsers;
  }


  @JsonProperty(JSON_PROPERTY_ALARM_USERS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAlarmUsers(List<AlarmUser> alarmUsers) {
    this.alarmUsers = alarmUsers;
  }


  public RecordAlarm fieldId(String fieldId) {
    
    this.fieldId = fieldId;
    return this;
  }

   /**
   * Get fieldId
   * @return fieldId
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_FIELD_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getFieldId() {
    return fieldId;
  }


  @JsonProperty(JSON_PROPERTY_FIELD_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }


  public RecordAlarm id(String id) {
    
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getId() {
    return id;
  }


  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setId(String id) {
    this.id = id;
  }


  public RecordAlarm recordId(String recordId) {
    
    this.recordId = recordId;
    return this;
  }

   /**
   * Get recordId
   * @return recordId
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RECORD_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getRecordId() {
    return recordId;
  }


  @JsonProperty(JSON_PROPERTY_RECORD_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }


  public RecordAlarm subtract(String subtract) {
    
    this.subtract = subtract;
    return this;
  }

   /**
   * Get subtract
   * @return subtract
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SUBTRACT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getSubtract() {
    return subtract;
  }


  @JsonProperty(JSON_PROPERTY_SUBTRACT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSubtract(String subtract) {
    this.subtract = subtract;
  }


  public RecordAlarm time(String time) {
    
    this.time = time;
    return this;
  }

   /**
   * Get time
   * @return time
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getTime() {
    return time;
  }


  @JsonProperty(JSON_PROPERTY_TIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTime(String time) {
    this.time = time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecordAlarm recordAlarm = (RecordAlarm) o;
    return Objects.equals(this.alarmAt, recordAlarm.alarmAt) &&
        Objects.equals(this.alarmUsers, recordAlarm.alarmUsers) &&
        Objects.equals(this.fieldId, recordAlarm.fieldId) &&
        Objects.equals(this.id, recordAlarm.id) &&
        Objects.equals(this.recordId, recordAlarm.recordId) &&
        Objects.equals(this.subtract, recordAlarm.subtract) &&
        Objects.equals(this.time, recordAlarm.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(alarmAt, alarmUsers, fieldId, id, recordId, subtract, time);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RecordAlarm {\n");
    sb.append("    alarmAt: ").append(toIndentedString(alarmAt)).append("\n");
    sb.append("    alarmUsers: ").append(toIndentedString(alarmUsers)).append("\n");
    sb.append("    fieldId: ").append(toIndentedString(fieldId)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    recordId: ").append(toIndentedString(recordId)).append("\n");
    sb.append("    subtract: ").append(toIndentedString(subtract)).append("\n");
    sb.append("    time: ").append(toIndentedString(time)).append("\n");
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

