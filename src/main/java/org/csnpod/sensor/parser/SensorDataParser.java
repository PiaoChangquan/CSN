package org.csnpod.sensor.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorDataParser {
	private Logger logger = LoggerFactory.getLogger(SensorDataParser.class);

	private String parsingRegex;
	private Map<String, Object> targetPosMap;

	public SensorDataParser(String parsingRegex,
			Map<String, Object> targetPosMap) {
		super();
		this.parsingRegex = parsingRegex;
		this.targetPosMap = targetPosMap;
	}

	public Map<String, Object> parseData(String rawData) {
		logger.trace("Start parseData Method");

		Pattern pattern = Pattern.compile(parsingRegex);
		Matcher matcher = pattern.matcher(rawData);
		Map<String, Object> parsedResult = null;

		if (matcher.matches()) {
			parsedResult = new HashMap<String, Object>();
			for (String targetKey : targetPosMap.keySet()) {
				int targetPos = Integer.parseInt((String) targetPosMap.get(targetKey));

				logger.debug("Parsing \"{}\" Key with postion \"{}\" in Regular Expression",	targetKey, targetPos);
				String value = matcher.group(targetPos);
				parsedResult.put(targetKey, value);
			}
		}
		logger.debug("Returned Parsed Result: {}", parsedResult);
		logger.trace("End parseData Method");
		return parsedResult;
	}
}
