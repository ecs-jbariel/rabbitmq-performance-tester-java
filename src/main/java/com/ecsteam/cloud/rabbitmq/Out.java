package com.ecsteam.cloud.rabbitmq;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Out {

	protected enum LogLevel {
		TRACE(0, "TRACE"), DEBUG(10, "DEBUG"), INFO(20, "INFO"), WARN(30, "WARN"), ERROR(40, "ERROR"), FATAL(50,
				"FATAL");

		int value;
		String name;

		private LogLevel(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int value() {
			return this.value;
		}

		public String level() {
			return this.name;
		}

		public boolean doLog(LogLevel level) {
			return value() <= level.value();
		}

	}

	private static LogLevel currentLogLevel = LogLevel.INFO;

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static void t(String msg, Object... objects) {
		doLog(LogLevel.TRACE, msg, objects);
	}

	public static void d(String msg, Object... objects) {
		doLog(LogLevel.DEBUG, msg, objects);
	}

	public static void i(String msg, Object... objects) {
		doLog(LogLevel.INFO, msg, objects);
	}

	public static void w(String msg, Object... objects) {
		doLog(LogLevel.WARN, msg, objects);
	}

	public static void e(String msg, Object... objects) {
		doLog(LogLevel.ERROR, msg, objects);
	}

	public static void f(String msg, Object... objects) {
		doLog(LogLevel.FATAL, msg, objects);
	}

	public static void setLogLevel(LogLevel newLevel) {
		currentLogLevel = newLevel;
	}

	protected static void doLog(LogLevel level, String msg, Object... objects) {
		if (currentLogLevel.doLog(level)) {
			System.out.println(
					dateTimeAsString() + String.format("[%-5s] ", level.level()) + String.format(msg, objects));
		}
	}

	public static String dateTimeAsString() {
		return String.format("[%s] ", df.format(new Date()));
	}

}
