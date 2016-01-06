package org.csnpod.datastream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.csnpod.datastream.data.DataStreamConfig;
import org.csnpod.sensor.data.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamManager {
	private static Logger logger = LoggerFactory.getLogger(StreamManager.class);
	private Connection c;
	private Statement stmt;

	public void setUpStreamQueue() {
		logger.trace("Start setUpStreamQueue method");

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"
					+ DataStreamConfig.dbName + ".db");
			stmt = c.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS "
					+ DataStreamConfig.streamName
					+ "(id BIGINT PRIMARY KEY     	NOT NULL,"
					+ " snsr_id        VARCHAR(50)	NOT NULL, "
					+ " timestamp      DATETIME    NOT NULL, "
		 			+ " value        	VARCHAR(50) )";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE IF NOT EXISTS queue_ptr"
					+ "(cur_ptr BIGINT	NOT NULL," + " max_ptr BIGINT	NOT NULL)";
			stmt.executeUpdate(sql);

			sql = "INSERT INTO queue_ptr (max_ptr, cur_ptr) SELECT -1, -1 "
					+ "WHERE NOT EXISTS (SELECT * FROM queue_ptr);";
			stmt.executeUpdate(sql);

			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't initialize Sensor Steram Queue DB becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
		} catch (ClassNotFoundException e) {
			logger.error("Can't initialize Sensor Steram Queue DB becaue of Class Not Found Exception");
			logger.error("Error: {}", e.toString());
		}
		logger.trace("Finish To create Tables");

		logger.trace("End setUpStreamQueue method");
	}

	private void initPtr() {
		logger.trace("Start initPtr method");

		long maxPtr = getMaxPtr();
		logger.debug("Current Max Ptr: {}", maxPtr);
		if (maxPtr < 0) {
			logger.trace("Init Max Pointer");
			String sql = "INSERT INTO queue_ptr (max_ptr) VALUES (-1);";
			try {
				stmt.executeUpdate(sql);

				stmt.close();
			} catch (SQLException e) {
				logger.error("Can't initialize Sensor Steram Queue Pointer DB becaue of SQL Exception");
				logger.error("Error: {}", e.toString());
			}
		}

		long curPtr = getCurPtr();
		logger.debug("Current Cur Ptr: {}", curPtr);
		if (curPtr < 0) {
			logger.trace("Init Current Pointer");
			String sql = "INSERT INTO queue_ptr (cur_ptr) VALUES (-1);";
			try {
				stmt.executeUpdate(sql);

				stmt.close();
			} catch (SQLException e) {
				logger.error("Can't initialize Sensor Steram Queue Pointer DB becaue of SQL Exception");
				logger.error("Error: {}", e.toString());
			}
		}

		logger.trace("End initPtr method");
	}

	public void insertData(SensorData data) {
		logger.trace("Start insertData method");

		long maxPtr = getMaxPtr() + 1;
		logger.debug("Sensor data: \"{}\" will be added to DB", data.toString());

		String sql = "INSERT INTO " + DataStreamConfig.streamName
				+ " (id, snsr_id, timestamp, value) " + "VALUES (" + maxPtr
				+ ", '" + data.getId() + "', '" + data.getTimestamp() + "', '"
				+ data.getValue() + "');";
		try {
			stmt.executeUpdate(sql);

			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't Add Sensor data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
		}
		updateMaxPtr(maxPtr);

		logger.trace("End insertData method");
	}

	public long getMaxPtr() {
		logger.trace("Start getMaxPtr method");

		long maxPtr = -1;
		try {
			ResultSet rs = stmt.executeQuery("SELECT max_ptr FROM queue_ptr;");
			rs.next();
			maxPtr = rs.getLong("max_ptr");

			logger.debug("Fetched max pointer value: \"{}\"", maxPtr);
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't Get Max Queue Pointer data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
			return -1;
		}

		logger.trace("End getMaxPtr method");
		return maxPtr;
	}

	public void updateMaxPtr(long maxPtr) {
		logger.trace("Start updateMaxPtr method");

		String sql = "UPDATE queue_ptr SET max_ptr = " + maxPtr + ";";
		try {
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't update Max Queue Pointer data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
		}

		logger.trace("End updateMaxPtr method");
	}

	public long getCurPtr() {
		logger.trace("Start getCurPtr method");

		long curPtr = -1;
		try {
			ResultSet rs = stmt.executeQuery("SELECT cur_ptr FROM queue_ptr;");
			rs.next();
			curPtr = rs.getLong("cur_ptr");
			logger.debug("Fetched current pointer value: \"{}\"", curPtr);

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't Get Current Queue Pointer data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
			return -1;
		}

		logger.trace("End getCurPtr method");
		return curPtr;
	}

	public List<SensorData> getUntransferredDataQueue() {
		logger.trace("Start getUntransferredData method");
		List<SensorData> dataList = new LinkedList<SensorData>();
		long curPtr = getCurPtr();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM "
					+ DataStreamConfig.streamName + " WHERE id > " + curPtr);

			while (rs.next()) {
				int id = rs.getInt("id");
				SensorData data = new SensorData(rs.getString("snsr_id"),
						rs.getString("timestamp"), rs.getString("value"));
				logger.debug("Fetched current Sensor Data ID {}: \"{}\"", id, data.toString());
				dataList.add(data);
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't Get Current Queue Pointer data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
		}

		logger.trace("End getUntransferredData method");
		return dataList;
	}

	public void updateCurPtr(long curPtr) {
		logger.trace("Start updateCurPtr method");

		String sql = "UPDATE queue_ptr SET cur_ptr = " + curPtr + ";";
		try {
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't update Current Queue Pointer data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
		}

		logger.trace("End updateCurPtr method");
	}

	public void addCurPtr(long size) {
		logger.trace("Start addCurPtr method");

		try {
			ResultSet rs = stmt.executeQuery("SELECT cur_ptr FROM queue_ptr;");
			rs.next();
			long curPtr = rs.getLong("cur_ptr");

			curPtr += size;
			String sql = "UPDATE queue_ptr SET cur_ptr = " + curPtr + ";";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			logger.error(
					"Can't Add \"{}\" Current Queue Pointer data becaue of SQL Exception",
					size);
			logger.error("Error: {}", e.toString());
		}

		logger.trace("End addCurPtr method");
	}

	public int closeStreamQueue() {
		logger.trace("Start closeStreamQueue method");

		int retData = 0;
		try {
			c.close();
		} catch (SQLException e) {
			logger.error("Can't close  Queue DB data becaue of SQL Exception");
			logger.error("Error: {}", e.toString());
			retData = -1;
		}

		logger.trace("End closeStreamQueue method");
		return retData;
	}
}
