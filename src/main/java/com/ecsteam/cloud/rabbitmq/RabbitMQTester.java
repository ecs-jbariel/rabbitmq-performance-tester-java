package com.ecsteam.cloud.rabbitmq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.ecsteam.cloud.rabbitmq.Out.LogLevel;

public class RabbitMQTester {

	public static ScheduledThreadPoolExecutor globalThreadPool = new ScheduledThreadPoolExecutor(10);

	private static boolean doProduce = false;
	private static boolean doConsume = false;

	public static void main(String[] args) {
		Out.i("Starting...");

		RMQProperties properties = loadProperties();

		Out.setLogLevel(properties.getLogLevel());
		doProduce = properties.isProducer();
		doConsume = properties.isConsumer();

		// final RMQProducer rmqProducer = new RMQProducer(properties);
		// final RMQConsumer rmqConsumer = new RMQConsumer(properties);

		final List<RMQProducer> rmqProducers = new ArrayList<>();
		final int numProducers = properties.getNumberOfProducers();
		for (int i = 0; i < numProducers; i++) {
			rmqProducers.add(new RMQProducer(properties));
		}

		final List<RMQConsumer> rmqConsumers = new ArrayList<>();
		final int numConsumers = properties.getNumberOfConsumers();
		for (int i = 0; i < properties.getNumberOfConsumers(); i++) {
			rmqConsumers.add(new RMQConsumer(properties));
		}

		if (doProduce) {
			Out.d("Creating %d producer(s)...", numProducers);
			int cnt = 0;
			for (final RMQProducer p : rmqProducers) {
				Out.d("Starting producer %d of %d...", ++cnt, numConsumers);
				globalThreadPool.schedule(new Runnable() {

					@Override
					public void run() {
						try {
							p.start();
						} catch (IOException e) {
							Out.e("Error starting RMQProducer");
							e.printStackTrace();
						}
					}

				}, 0, TimeUnit.MILLISECONDS);
			}
		}

		if (doConsume) {
			Out.d("Creating %d consumer(s)...", numConsumers);
			int cnt = 0;
			for (final RMQConsumer c : rmqConsumers) {
				Out.d("Starting consumer %d of %d...", ++cnt, numConsumers);
				globalThreadPool.schedule(new Runnable() {

					@Override
					public void run() {
						try {
							c.start();
						} catch (IOException e) {
							Out.e("Error starting RMQConsumer");
							e.printStackTrace();
						}
					}

				}, 0, TimeUnit.MILLISECONDS);
			}
		}

		Out.i("Running...");

		Out.i("Press \"ENTER\" to continue...");
		try {
			System.in.read();
		} catch (

		IOException e) {
			e.printStackTrace();
		}

		Out.i("Closing down...");

		if (doProduce) {
			rmqProducers.forEach(p -> {
				try {
					p.stop();
				} catch (IOException e) {
					Out.w("IOException stopping producer!");
					e.printStackTrace();
				}
			});
		}

		if (doConsume) {
			rmqConsumers.forEach(c -> {
				try {
					c.stop();
				} catch (IOException e) {
					Out.w("IOException stopping consumer!");
					e.printStackTrace();
				}
			});
		}

		Out.i("Exiting...");
		System.exit(0);
	}

	private static RMQProperties loadProperties() {
		Out.d("Loading properties...");
		RMQProperties props = new RMQProperties();

		Properties propfile = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("props.properties");
			propfile.load(input);

			props.setUsername(StringUtils.trimToEmpty(propfile.getProperty("username")));
			props.setPassword(StringUtils.trimToEmpty(propfile.getProperty("password")));
			props.setPort(NumberUtils.toInt(propfile.getProperty("port", "5672")));
			props.setHostname(StringUtils.trimToEmpty(propfile.getProperty("hostname", "localhost")));
			props.setvHost(StringUtils.trimToEmpty(propfile.getProperty("vhost")));
			props.setLogLevel(LogLevel.valueOf(StringUtils.trimToEmpty(propfile.getProperty("loglevel", "INFO"))));
			props.setProducer(Boolean.parseBoolean(StringUtils.trimToEmpty(propfile.getProperty("producer", "false"))));
			props.setConsumer(Boolean.parseBoolean(StringUtils.trimToEmpty(propfile.getProperty("consumer", "false"))));
			props.setNumberOfConsumers(NumberUtils.toInt(propfile.getProperty("numberOfConsumers", "1")));
			props.setNumberOfProducers(NumberUtils.toInt(propfile.getProperty("numberOfProducers", "1")));
			props.setProducerMessageRate(NumberUtils.toLong(propfile.getProperty("producerMessageRate", "1000L")));
			props.setProducerMessageSize(NumberUtils.toInt(propfile.getProperty("producerMessageSize", "1000")));

		} catch (IOException e) {
			Out.e("Exception loading properties file");
			e.printStackTrace();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					Out.w("Error closing input...");
				}
			}
		}

		Out.i("Found properties: %s", props);
		return props;
	}

}
