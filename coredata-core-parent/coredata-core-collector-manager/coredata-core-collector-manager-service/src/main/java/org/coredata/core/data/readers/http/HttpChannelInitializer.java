package org.coredata.core.data.readers.http;

import java.util.concurrent.BlockingQueue;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

	private BlockingQueue<Object> mqueue;

	private String requestMethod;

	public HttpChannelInitializer(BlockingQueue<Object> mqueue, String requestMethod) {
		this.mqueue = mqueue;
		this.requestMethod = requestMethod;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast("encoder", new HttpResponseEncoder());
		ch.pipeline().addLast("decoder", new HttpRequestDecoder());
		ch.pipeline().addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
		ch.pipeline().addLast("handler", new ReceiveHandler(this.mqueue, this.requestMethod));//添加相关处理类
	}
}
