package org.coredata.core.data.readers.http;

import java.util.concurrent.BlockingQueue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public class ReceiveHandler extends ChannelInboundHandlerAdapter {

	private BlockingQueue<Object> mqueue;

	private ByteBufToBytes reader;

	private String requestMethod;

	public ReceiveHandler(BlockingQueue<Object> mqueue, String requestMethod) {
		this.mqueue = mqueue;
		this.requestMethod = requestMethod;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest request = (FullHttpRequest) msg;
			HttpMethod method = request.method();
			if (!method.toString().equals(requestMethod)) {//如果不符合请求method，直接返回
				//接收消息后，发送给队列
				FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
						Unpooled.wrappedBuffer(new String("Error.Request method should be " + requestMethod).getBytes()));
				response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
				response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				ctx.writeAndFlush(response);
				return;
			}
			if (HttpUtil.isContentLengthSet(request))
				reader = new ByteBufToBytes((int) HttpUtil.getContentLength(request));
			ByteBuf content = request.content();
			reader.reading(content);
			content.release();
			if (reader.isEnd()) {
				String reqBody = new String(reader.readFull());
				this.mqueue.put(reqBody);
			}
		}
		//接收消息后，发送给队列
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("success".getBytes()));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		ctx.writeAndFlush(response);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}
