--<ScriptOptions statementTerminator="%"/>

DROP TRIGGER TLMY.MonitoringData_dup_notification%

ALTER TABLE TLMY.MonitoringData DROP CONSTRAINT MonitoringData_PK%

DROP INDEX TLMY.MonitoringData_createTimestamp_IDX%

DROP VIEW TLMY.LatestAlert%

DROP VIEW TLMY.LatestControl%

DROP VIEW TLMY.LatestMeasurement%

DROP VIEW TLMY.LatestRequestResponse%

DROP VIEW TLMY.LatestStatus%

DROP VIEW TLMY.MonitoringStatus%

DROP TABLE TLMY.AlertData%

DROP TABLE TLMY.ControlData%

DROP TABLE TLMY.MeasurementData%

DROP TABLE TLMY.MonitoringData%

DROP TABLE TLMY.RequestResponseData%

DROP TABLE TLMY.StatusData%

DROP SCHEMA TLMY RESTRICT%

CREATE SCHEMA TLMY%

CREATE TABLE TLMY.AlertData (
		clusterId VARCHAR(64) NOT NULL,
		agentId VARCHAR(64) NOT NULL,
		createTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
		timestamp TIMESTAMP NOT NULL,
		value VARCHAR(64) NOT NULL,
		daynum INTEGER NOT NULL GENERATED ALWAYS AS ((INTEGER(DATE(createTimestamp))))
	)
	ORGANIZE BY DIMENSIONS (
	(clusterId),  
	(daynum))
	DATA CAPTURE NONE 
	COMPRESS YES%

CREATE TABLE TLMY.ControlData (
		clusterId VARCHAR(64) NOT NULL,
		agentId VARCHAR(64) NOT NULL,
		valueName VARCHAR(64) NOT NULL,
		createTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
		timestamp TIMESTAMP NOT NULL,
		value VARCHAR(64) NOT NULL,
		units VARCHAR(16) NOT NULL,
		daynum INTEGER NOT NULL GENERATED ALWAYS AS ((INTEGER(DATE(createTimestamp))))
	)
	ORGANIZE BY DIMENSIONS (
	(clusterId),  
	(daynum))
	DATA CAPTURE NONE 
	COMPRESS YES%

CREATE TABLE TLMY.MeasurementData (
		clusterId VARCHAR(64) NOT NULL,
		agentId VARCHAR(64) NOT NULL,
		valueName VARCHAR(64) NOT NULL,
		createTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
		timestamp TIMESTAMP NOT NULL,
		value REAL NOT NULL,
		units VARCHAR(16) NOT NULL,
		period INTEGER,
		daynum INTEGER NOT NULL GENERATED ALWAYS AS ((INTEGER(DATE(createTimestamp))))
	)
	ORGANIZE BY DIMENSIONS (
	(clusterId),  
	(daynum))
	DATA CAPTURE NONE 
	COMPRESS YES%

CREATE TABLE TLMY.MonitoringData (
		namespaceId VARCHAR(64) NOT NULL,
		CI_ID CHAR(64) NOT NULL,
		CI_NAME CHAR(64) NOT NULL,
		COMPONENT_NAME CHAR(64) NOT NULL,
		SERVER_NAME CHAR(64) NOT NULL,
		CI_STATUS CHAR(24) NOT NULL,
		CI_STATUS_DATE TIMESTAMP NOT NULL,
		CI_SEVERITY CHAR(1) NOT NULL,
		createTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP
	)
	DATA CAPTURE NONE 
	COMPRESS YES%

CREATE TABLE TLMY.RequestResponseData (
		clusterId VARCHAR(64) NOT NULL,
		agentId VARCHAR(64) NOT NULL,
		isRequest CHAR(1) NOT NULL,
		requestType VARCHAR(64) NOT NULL,
		requestId VARCHAR(64),
		createTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
		timestamp TIMESTAMP NOT NULL,
		properties VARCHAR(1024),
		daynum INTEGER NOT NULL GENERATED ALWAYS AS ((INTEGER(DATE(createTimestamp))))
	)
	ORGANIZE BY DIMENSIONS (
	(clusterId),  
	(daynum))
	DATA CAPTURE NONE 
	COMPRESS YES%

CREATE TABLE TLMY.StatusData (
		clusterId VARCHAR(64) NOT NULL,
		agentId VARCHAR(64) NOT NULL,
		valueName VARCHAR(64) NOT NULL,
		createTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
		timestamp TIMESTAMP NOT NULL,
		value VARCHAR(64) NOT NULL,
		daynum INTEGER NOT NULL GENERATED ALWAYS AS ((INTEGER(DATE(createTimestamp))))
	)
	ORGANIZE BY DIMENSIONS (
	(clusterId),  
	(daynum))
	DATA CAPTURE NONE 
	COMPRESS YES%

CREATE INDEX TLMY.MonitoringData_createTimestamp_IDX
	ON TLMY.MonitoringData
	(createTimestamp		ASC) PCTFREE 0
ALLOW REVERSE SCANS%

ALTER TABLE TLMY.MonitoringData ADD CONSTRAINT MonitoringData_PK PRIMARY KEY
	(namespaceId,
	 CI_ID,
	 createTimestamp)%

CREATE VIEW TLMY.LatestAlert (clusterId, agentId, timestamp, value) AS
SELECT evt.clusterId as "clusterId", evt.agentId as "agentId", evt.timestamp as "timestamp", evt.value as "value" FROM TLMY.AlertData evt JOIN 
(SELECT part.clusterId, part.agentId, MAX(part.createTimestamp) AS lastTimestamp FROM TLMY.AlertData part WHERE part.daynum = INTEGER(CURRENT DATE) GROUP BY part.clusterId, part.agentId) last
ON evt.createTimestamp = last.lastTimestamp AND evt.clusterId = last.clusterId AND evt.agentId = last.agentId%

CREATE VIEW TLMY.LatestControl (clusterId, agentId, timestamp, valueName, value) AS
SELECT evt.clusterId as "clusterId", evt.agentId as "agentId", evt.timestamp as "timestamp", evt.valueName as "valueName", evt.value as "value" FROM TLMY.ControlData evt JOIN 
(SELECT part.clusterId, part.agentId, part.valueName, MAX(part.createTimestamp) AS lastTimestamp FROM TLMY.ControlData part WHERE part.daynum = INTEGER(CURRENT DATE) GROUP BY part.clusterId, part.agentId, part.valueName) last
ON evt.createTimestamp = last.lastTimestamp AND evt.clusterId = last.clusterId AND evt.agentId = last.agentId AND evt.valueName = last.valueName%

CREATE VIEW TLMY.LatestMeasurement (clusterId, agentId, timestamp, valueName, value, units) AS
SELECT evt.clusterId as "clusterId", evt.agentId as "agentId", evt.timestamp as "timestamp", evt.valueName as "valueName", evt.value as "value", evt.units as "units" FROM TLMY.MeasurementData evt JOIN 
(SELECT part.clusterId, part.agentId, part.valueName, MAX(part.createTimestamp) AS lastTimestamp FROM TLMY.MeasurementData part WHERE part.daynum = INTEGER(CURRENT DATE) GROUP BY part.clusterId, part.agentId, part.valueName) last
ON evt.createTimestamp = last.lastTimestamp AND evt.clusterId = last.clusterId AND evt.agentId = last.agentId AND evt.valueName = last.valueName%

CREATE VIEW TLMY.LatestRequestResponse (clusterId, agentId, timestamp, isRequest, requestType, properties) AS
SELECT evt.clusterId as "clusterId", evt.agentId as "agentId", evt.timestamp as "timestamp", evt.isRequest as "isRequest", evt.requestType as "requestType", evt.properties as "properties" FROM TLMY.RequestResponseData evt JOIN 
(SELECT part.clusterId, part.agentId, part.isRequest, part.requestType, MAX(part.createTimestamp) AS lastTimestamp FROM TLMY.RequestResponseData part WHERE part.daynum = INTEGER(CURRENT DATE) GROUP BY part.clusterId, part.agentId, part.isRequest, part.requestType) last
ON evt.createTimestamp = last.lastTimestamp AND evt.clusterId = last.clusterId AND evt.agentId = last.agentId AND evt.isRequest = last.isRequest AND evt.requestType = last.requestType%

CREATE VIEW TLMY.LatestStatus (clusterId, agentId, timestamp, valueName, value) AS
SELECT evt.clusterId as "clusterId", evt.agentId as "agentId", evt.timestamp as "timestamp", evt.valueName as "valueName", evt.value as "value" FROM TLMY.StatusData evt JOIN 
(SELECT part.clusterId, part.agentId, part.valueName, MAX(part.createTimestamp) AS lastTimestamp FROM TLMY.StatusData part WHERE part.daynum = INTEGER(CURRENT DATE) GROUP BY part.clusterId, part.agentId, part.valueName) last
ON evt.createTimestamp = last.lastTimestamp AND evt.clusterId = last.clusterId AND evt.agentId = last.agentId AND evt.valueName = last.valueName%

CREATE VIEW TLMY.MonitoringStatus (NAMESPACEID, CI_ID, CI_NAME, COMPONENT_NAME, SERVER_NAME, CI_STATUS, CI_STATUS_DATE, CI_SEVERITY, LAST_MODIFIED) AS
SELECT ms.namespaceId AS namespaceId, ms.CI_ID AS CI_ID, ms.CI_NAME AS CI_NAME, ms.COMPONENT_NAME AS COMPONENT_NAME, ms.SERVER_NAME AS SERVER_NAME, ms.CI_STATUS AS CI_STATUS,
CAST(CONCAT(CONCAT(VARCHAR_FORMAT(ms.CI_STATUS_DATE,'YYYY-MM-DD'),'T'),CONCAT(VARCHAR_FORMAT(ms.CI_STATUS_DATE,'HH24:MI:SS.FF3'),'Z')) AS CHAR(24)) AS CI_STATUS_DATE,
ms.CI_SEVERITY AS CI_SEVERITY, ms.createTimestamp AS LAST_MODIFIED  FROM TLMY.MonitoringData ms JOIN 
(SELECT md.namespaceId, md.CI_ID, MAX(md.createTimestamp) AS latest FROM TLMY.MonitoringData md GROUP BY md.namespaceId, md.CI_ID) lastStatus
ON ms.namespaceId = lastStatus.namespaceId AND ms.CI_ID = lastStatus.CI_ID AND ms.createTimestamp = lastStatus.latest%

CREATE TRIGGER TLMY.MonitoringData_dup_notification 
	AFTER INSERT ON TLMY.MonitoringData
	REFERENCING  NEW AS n
	FOR EACH ROW
WHEN (n.ci_status = 'CONNECTED')
begin atomic
delete from tlmy.monitoringdata data where data.ci_status = 'CONNECTED' and data.createTimestamp = n.createTimestamp and (select ci_status_date from tlmy.monitoringdata prev
where prev.namespaceid = data.namespaceid
and prev.ci_id = data.ci_id
and prev.ci_name = data.ci_name
and prev.ci_status = 'CLEANDISCONNECTED'
order by ci_status_date desc fetch first row only) - data.ci_status_date > -2;
end%

COMMENT ON COLUMN TLMY.AlertData.agentId IS
'ID of the agent that is sending the telemetry event.
The agent ID is unique within the cluster.'%

COMMENT ON COLUMN TLMY.AlertData.clusterId IS
'ID of the PowerMatcher cluster.'%

COMMENT ON COLUMN TLMY.AlertData.createTimestamp IS
'Timestamp when the row was created. The timestamp is in local time.'%

COMMENT ON COLUMN TLMY.AlertData.daynum IS
'Generated column for multi-dimensional clustering by day number (YYYYMMDD) of createTimestamp.'%

COMMENT ON COLUMN TLMY.AlertData.timestamp IS
'Source timestamp for the event, in UTC.'%

COMMENT ON COLUMN TLMY.AlertData.value IS
'The alert or notification message.'%

COMMENT ON COLUMN TLMY.ControlData.agentId IS
'ID of the agent that is sending the telemetry event.
The agent ID is unique within the cluster.'%

COMMENT ON COLUMN TLMY.ControlData.clusterId IS
'ID of the PowerMatcher cluster.'%

COMMENT ON COLUMN TLMY.ControlData.createTimestamp IS
'Timestamp when the row was created. The timestamp is in local time.'%

COMMENT ON COLUMN TLMY.ControlData.daynum IS
'Generated column for multi-dimensional clustering by day number (YYYYMMDD) of createTimestamp.'%

COMMENT ON COLUMN TLMY.ControlData.timestamp IS
'Source timestamp for the event, in UTC.'%

COMMENT ON COLUMN TLMY.ControlData.units IS
'The unit (for example "C") for the control setting.
This is a copy of the unit defined in the associated ControlType.'%

COMMENT ON COLUMN TLMY.ControlData.value IS
'The new value for the control property.'%

COMMENT ON COLUMN TLMY.ControlData.valueName IS
'The ID of the control point of which the setting is changed.'%

COMMENT ON COLUMN TLMY.MeasurementData.agentId IS
'ID of the agent that is sending the telemetry event.
The agent ID is unique within the cluster.'%

COMMENT ON COLUMN TLMY.MeasurementData.clusterId IS
'ID of the PowerMatcher cluster.'%

COMMENT ON COLUMN TLMY.MeasurementData.createTimestamp IS
'Timestamp when the row was created. The timestamp is in local time.'%

COMMENT ON COLUMN TLMY.MeasurementData.daynum IS
'Generated column for multi-dimensional clustering by day number (YYYYMMDD) of createTimestamp.'%

COMMENT ON COLUMN TLMY.MeasurementData.period IS
'The (optional) period a quantitative measurement relates to, in seconds from source timestamp.
For example, 120 Wh in the 15 minutes starting at timestamp.'%

COMMENT ON COLUMN TLMY.MeasurementData.timestamp IS
'Source timestamp for the event, in UTC.'%

COMMENT ON COLUMN TLMY.MeasurementData.units IS
'The unit (for example "W") for the measurement.
This is a copy of the unit defined in the associated MeasurementType.'%

COMMENT ON COLUMN TLMY.MeasurementData.value IS
'The new value for the measurement property.'%

COMMENT ON COLUMN TLMY.MeasurementData.valueName IS
'The ID of the measurement point for which the value has changed.'%

COMMENT ON COLUMN TLMY.MonitoringData.CI_ID IS
'Unique identification of the Configuration Item (component, application or application element) within the namespace.'%

COMMENT ON COLUMN TLMY.MonitoringData.CI_NAME IS
'Unique name of the component, application or application element.'%

COMMENT ON COLUMN TLMY.MonitoringData.CI_SEVERITY IS
'Severity code, used for monitoring event dispatching.
F (Fatal)
C (Critical)
W (Warning)
M (Minor)
I (Informational)'%

COMMENT ON COLUMN TLMY.MonitoringData.CI_STATUS IS
'This field contains the status of the component / application / application element.'%

COMMENT ON COLUMN TLMY.MonitoringData.CI_STATUS_DATE IS
'UTC date and time when the status is reported by the Configuration Item (based on the time used by the configuration item).'%

COMMENT ON COLUMN TLMY.MonitoringData.COMPONENT_NAME IS
'Component Name, used for filtering in monitoring tool.'%

COMMENT ON COLUMN TLMY.MonitoringData.SERVER_NAME IS
'Fully qualified host name of the server where the Configuration Item is deployed.
Note: if the CI is deployed on a cluster, then the cluster address must be used.'%

COMMENT ON COLUMN TLMY.MonitoringData.createTimestamp IS
'Automatically generated during insert of new status event.
Used for monitoring of regular Status updates.'%

COMMENT ON COLUMN TLMY.MonitoringData.namespaceId IS
'The namespace that the monitored component belongs to.'%

COMMENT ON COLUMN TLMY.RequestResponseData.agentId IS
'ID of the agent that is sending the telemetry event.
The agent ID is unique within the cluster.'%

COMMENT ON COLUMN TLMY.RequestResponseData.clusterId IS
'ID of the PowerMatcher cluster.'%

COMMENT ON COLUMN TLMY.RequestResponseData.createTimestamp IS
'Timestamp when the row was created. The timestamp is in local time.'%

COMMENT ON COLUMN TLMY.RequestResponseData.daynum IS
'Generated column for multi-dimensional clustering by day number (YYYYMMDD) of createTimestamp.'%

COMMENT ON COLUMN TLMY.RequestResponseData.isRequest IS
'T'' if the event is for a request, ''F'' if the event is for a response.'%

COMMENT ON COLUMN TLMY.RequestResponseData.properties IS
'String representation of the name/value pairs of the request or response properties.'%

COMMENT ON COLUMN TLMY.RequestResponseData.requestId IS
'Optional ID for the request, intended for pairing requests and response.'%

COMMENT ON COLUMN TLMY.RequestResponseData.requestType IS
'ID for the type of request the request or response message relates to.'%

COMMENT ON COLUMN TLMY.RequestResponseData.timestamp IS
'Source timestamp for the event, in UTC.'%

COMMENT ON COLUMN TLMY.StatusData.agentId IS
'ID of the agent that is sending the telemetry event.
The agent ID is unique within the cluster.'%

COMMENT ON COLUMN TLMY.StatusData.clusterId IS
'ID of the PowerMatcher cluster.'%

COMMENT ON COLUMN TLMY.StatusData.createTimestamp IS
'Timestamp when the row was created. The timestamp is in local time.'%

COMMENT ON COLUMN TLMY.StatusData.daynum IS
'Generated column for multi-dimensional clustering by day number (YYYYMMDD) of createTimestamp.'%

COMMENT ON COLUMN TLMY.StatusData.timestamp IS
'Source timestamp for the event, in UTC.'%

COMMENT ON COLUMN TLMY.StatusData.value IS
'The new value for the status property.'%

COMMENT ON COLUMN TLMY.StatusData.valueName IS
'The ID of the status point for which the value has changed.'%

COMMENT ON TABLE TLMY.AlertData IS
'Data for an Alert event.
An alert event represents an alarm or notification signaled by a piece of equipment.'%

COMMENT ON TABLE TLMY.ControlData IS
'Data for a Control event.
A control event is a control message with a setting sent to an agent.'%

COMMENT ON TABLE TLMY.MeasurementData IS
'Data for a Measurement event.
A measurement event represents an status change for one of the measurement values reported by an agent.'%

COMMENT ON TABLE TLMY.MonitoringData IS
'Monitoring events from data processing services.'%

COMMENT ON TABLE TLMY.MonitoringStatus IS
'View on MonitoringData that returns the last event for each Configuration Item.'%

COMMENT ON TABLE TLMY.RequestResponseData IS
'Data for a Request or Response event.
A request event is a request message with paremeters sent to an agent.
A response event is a response message with paremeters received from anagent..'%

COMMENT ON TABLE TLMY.StatusData IS
'Data for a Status event.
A status event represents a status change for one of the status values reported by an agent.'%

COMMENT ON TRIGGER TLMY.MonitoringData_dup_notification IS
'Delete duplicate CONNECTED notification resulting from an assumed bug in WebSphere MQ 7.1.0.1.'%

