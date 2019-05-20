package netty.helloword;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();

		try{
			ServerBootstrap bootstrap = new ServerBootstrap();				//Server配置器
			bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)		
			.option(ChannelOption.SO_BACKLOG, 1024)			//设置TCP缓冲区大小
			.option(ChannelOption.SO_BACKLOG, 32 * 1024)	//设置发送缓冲大小
			.option(ChannelOption.SO_RCVBUF, 32 * 1024)		//设置接收缓冲大小
			.childOption(ChannelOption.SO_KEEPALIVE, true)		//保持连接
			.childHandler(new ChannelInitializer<SocketChannel>() {		//增加监听
				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast(new ServerHandler());			//设置处理线程
				}
			});

			ChannelFuture cf1 = bootstrap.bind(9999).sync();			//异步启动
			cf1.channel().closeFuture().sync();							//为了让程序监听，这里需要保持程序运行，相当于一个死循环
		}finally{
			boss.shutdownGracefully();
			worker.shutdownGracefully();			
		}
	}
}
