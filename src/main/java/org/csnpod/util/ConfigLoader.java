package org.csnpod.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.csnpod.comm.data.CommConfig;
import org.csnpod.comm.data.CommunicationType;
import org.csnpod.comm.modem.ResponseMode;
import org.csnpod.datastream.data.DataStreamConfig;
import org.csnpod.mgnt.data.CsnPodConfig;
import org.csnpod.sensor.data.LogicalSensorMetadata;
import org.csnpod.sensor.data.PhysicalSensorInformation;
import org.csnpod.sensor.data.SensorUnitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {
	private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	private static ObjectMapper mapper;
	private static JsonNode rootNode;

	public static void setLoader(String filePath) {
		logger.trace("Start setLoader Method");

		logger.info("Load the \"{}\" configuration file", filePath);
		mapper = new ObjectMapper();
		try {
			rootNode = mapper.readTree(new File(filePath));
		} catch (IOException e) {
			logger.error("Can't load \"{}\" file", filePath);
			logger.error("Error: {}", e.toString());
		}

		logger.trace("End setLoader Method");
	}

	public static void loadAllConfig(String filePath) {
		logger.trace("Start loadAllConfig Method");

		logger.debug("Load Configuration from {}", filePath);

		setLoader(filePath);

		loadCsnPodConfig();
		loadSensorConfig();
		loadDataStreamConfig();
		loadCommConfig();

		logger.trace("End loadAllConfig Method");
	}

	private static void loadCsnPodConfig() {
		logger.trace("Start loadCsnPodConfig Method");

		JsonNode csnPodUnitNode = rootNode.path("csnpod_unit");
		CsnPodConfig.adminPhoneNum = csnPodUnitNode.path("admin_cell_phone")
				.textValue();

		logger.debug("Loaded CSN Pod Config: {}", new CsnPodConfig());

		logger.trace("End loadCsnPodConfig Method");
	}

	private static void loadSensorConfig() {
		logger.trace("Start loadSensorConfig Method");

		JsonNode sensorUnitNode = rootNode.path("sensor_unit");

		SensorUnitConfig.snsrCount = sensorUnitNode.path("count").intValue();

		if (SensorUnitConfig.snsrCount > 0) {
			JsonNode sensorListNode = sensorUnitNode.path("elements");
			List<PhysicalSensorInformation> physicalSensorConf = new ArrayList<PhysicalSensorInformation>();

			for (JsonNode sensorNode : sensorListNode) {
				try {
					List<Map<String, String>> tempSensors = mapper
							.readValue(
									sensorNode.path("sensors").toString(),
									new TypeReference<LinkedList<HashMap<String, String>>>() {
									});
					List<LogicalSensorMetadata> sensors = new LinkedList<LogicalSensorMetadata>();
					for (Map<String, String> logicalSnsrMap : tempSensors) {
						LogicalSensorMetadata tempSensor = new LogicalSensorMetadata(
								logicalSnsrMap.get("local_id"),
								logicalSnsrMap.get("csn_id"),
								logicalSnsrMap.get("time_format"),
								logicalSnsrMap.get("value_type"),
								logicalSnsrMap.get("description"));
						sensors.add(tempSensor);
					}

					HashMap<String, Object> target = mapper.readValue(
							sensorNode.path("parse_target").toString(),
							new TypeReference<HashMap<String, Object>>() {
							});

					PhysicalSensorInformation sensor = new PhysicalSensorInformation(
							sensorNode.path("name").textValue(), sensorNode
									.path("type").textValue(), sensors,
							sensorNode.path("parse_regex").textValue(), target,
							sensorNode.path("sampling_period").intValue());

					physicalSensorConf.add(sensor);
					logger.debug("Added Physical Sensor's number: {}",
							physicalSensorConf.size());
					logger.debug("Added Sensor: {}", physicalSensorConf);

				} catch (JsonParseException e) {
					logger.error("Can't Parse the configuration file");
					logger.error("Error: {}", e.toString());
				} catch (JsonMappingException e) {
					logger.error("Can't map the configuration file");
					logger.error("Error: {}", e.toString());
				} catch (IOException e) {
					logger.error("Can't open the configurationfile");
					logger.error("Error: {}", e.toString());
				}

			}

			SensorUnitConfig.physicalSnsrInfo = physicalSensorConf;
			logger.info("Loaded Sensor Config: {}", new SensorUnitConfig());
		}

		logger.trace("End loadSensorConfig Method");
	}

	private static void loadDataStreamConfig() {
		logger.trace("Start loadDataStreamConfig Method");

		JsonNode dataUnitNode = rootNode.path("data_unit");
		DataStreamConfig.dbName = dataUnitNode.path("db_name").textValue();
		DataStreamConfig.streamName = dataUnitNode.path("stream_name")
				.textValue();
		DataStreamConfig.bufferSize = dataUnitNode.path("buffer_size")
				.intValue();
		DataStreamConfig.serverIP = dataUnitNode.path("server_ip").textValue();
		DataStreamConfig.serverPort = dataUnitNode.path("server_port")
				.intValue();
		DataStreamConfig.transferPeriod = dataUnitNode.path("transfer_period")
				.intValue();
		DataStreamConfig.maxTransferCntAtOnce = dataUnitNode.path(
				"max_transfer_cnt_at_once").intValue();

		logger.info("Loaded DataStream Config: {}", new DataStreamConfig());

		logger.trace("End loadDataStreamConfig Method");
	}

	public static void loadCommConfig() {
		logger.trace("Start loadCommConfig Method");

		JsonNode commUnitNode = rootNode.path("comm_unit");

		if (commUnitNode.path("response_mode").textValue()
				.equals(ResponseMode.NUMERIC.toString())) {
			CommConfig.respMode = ResponseMode.NUMERIC;
		} else if (commUnitNode.path("response_mode").textValue()
				.equals(ResponseMode.VERBOSE.toString())) {
			CommConfig.respMode = ResponseMode.VERBOSE;
		}

		CommConfig.statusChkPeriod = commUnitNode.path("satus_check_period")
				.intValue();

		if (commUnitNode.path("comm_type").textValue().equals(CommunicationType.CELLULAR.toString())) {
			CommConfig.commType = CommunicationType.CELLULAR;
		} else if (commUnitNode.path("comm_type").textValue()
				.equals(CommunicationType.ETHERNET.toString())) {
			CommConfig.commType = CommunicationType.ETHERNET;
		} else if (commUnitNode.path("comm_type").textValue()
				.equals(CommunicationType.WIFI.toString())) {
			CommConfig.commType = CommunicationType.WIFI;
		}

		logger.info("Loaded Communication Config: {}", new CommConfig());

		logger.trace("End loadCommConfig Method");
	}
}
