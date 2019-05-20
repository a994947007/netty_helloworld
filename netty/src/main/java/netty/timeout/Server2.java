package netty.timeout;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;

public class Server2 {
	
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss, worker)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_RCVBUF, 32 * 1024)
		.option(ChannelOption.SO_SNDBUF,32 * 1024)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new MyHandler());
			}
		});
		
		ChannelFuture cf = bootstrap.bind(9999).sync();
		cf.channel().closeFuture().sync();
		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}
	
	private static class MyHandler extends SimpleChannelInboundHandler<String>{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg)
				throws Exception {
			System.out.println(msg);
			ReferenceCountUtil.release(msg);
		}
	}
}
