package org.coredata.core.data.readers.http;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ReceiveConsumer implements Runnable {

	public static Logger logger = LoggerFactory.getLogger(ReceiveConsumer.class);

	private BlockingQueue<Object> dataQueue;

	private ReceiveReader reader;

	public ReceiveConsumer(ReceiveReader reader, BlockingQueue<Object> dataQueue) {
		this.dataQueue = dataQueue;
		this.reader = reader;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				if (null != dataQueue.peek()) {
					Object data = dataQueue.take();
					Object json = JSON.parse(data.toString());
					if (json instanceof JSONObject) {
						reader.process((JSONObject) json);
					} else if (json instanceof JSONArray) {
						reader.process((JSONArray) json);
					}
				}
			} catch (Exception e) {
				logger.error("Receive metrics data error.", e);
			}
		}
	}

}
