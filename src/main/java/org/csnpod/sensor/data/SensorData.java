package org.csnpod.sensor.data;

import com.google.common.base.MoreObjects;

public class SensorData {

	private String id;
	private String timestamp;
	private String value;

	public SensorData(String id, String Timestamp, String Value) {
		this.id = id;
		this.timestamp = Timestamp;
		this.value = Value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("ID", id)
				.add("Timestamp", timestamp).add("Value", value).toString();
	}

}
