package netty.timeout;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.ReferenceCountUtil;

public class Server {
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss,worker)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_RCVBUF, 32 * 1024)
		.option(ChannelOption.SO_RCVBUF, 32 * 1024)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new ReadTimeoutHandler(5));	//5秒钟内收到客户端的请求则不会断开与客户端的连接
				ch.pipeline().addLast(new StringDecoder(Charset.forName("utf-8")));
				ch.pipeline().addLast(new MyHandler());
			}
		});
		
		bootstrap.bind(9999).sync().channel().closeFuture().sync();
		
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
