package org.csnpod.sensor.data;

import com.google.common.base.MoreObjects;

public class LogicalSensorMetadata {

	private String localId;
	private String csnId;
	private String timeFormat;
	private String valType;
	private String description;

	public LogicalSensorMetadata(String localId, String csnId,
			String timeFormat, String valType, String description) {
		super();
		this.localId = localId;
		this.csnId = csnId;
		this.timeFormat = timeFormat;
		this.valType = valType;
		this.description = description;
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getCsnId() {
		return csnId;
	}

	public void setCsnId(String csnId) {
		this.csnId = csnId;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getValType() {
		return valType;
	}

	public void setValType(String valType) {
		this.valType = valType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Local ID", localId)
				.add("CSN ID", csnId).add("Timestamp Format", timeFormat)
				.add("Value Type", valType).add("Description", description)
				.toString();
	}

}
