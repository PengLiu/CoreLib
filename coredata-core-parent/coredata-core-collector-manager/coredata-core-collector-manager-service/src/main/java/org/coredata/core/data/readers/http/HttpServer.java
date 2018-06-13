package org.coredata.core.data.readers.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * tcp服务
 * @author sue
 *
 */
public class HttpServer extends BaseServer {

	/**
	 * 配置解释器
	 */
	private ChannelInitializer<SocketChannel> initializer;

	private ChannelFuture channelFuture;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	public HttpServer() {

	}

	public HttpServer(int port, ChannelInitializer<SocketChannel> initializer) {
		this.port = port;
		this.initializer = initializer;
	}

	/**
	 * 用于启动http服务
	 * @throws Exception
	 */
	@Override
	public void start() throws Exception {
		//EventLoopGroup是用来处理IO操作的多线程事件循环器
		//bossGroup 用来接收进来的连接
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		//workerGroup 用来处理已经被接收的连接
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap strap = new ServerBootstrap();
		strap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
		ChannelFuture channelFuture = strap.bind(this.port).sync();
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.channelFuture = channelFuture;
		httpServers.put(this.port, this);
	}

	/**
	 * 用于停止http服务
	 * @throws Exception
	 */
	@Override
	public void stop(int port) throws Exception {
		httpServers.remove(port);
		this.bossGroup.shutdownGracefully();
		this.workerGroup.shutdownGracefully();
		this.channelFuture.channel().close();
	}

}
