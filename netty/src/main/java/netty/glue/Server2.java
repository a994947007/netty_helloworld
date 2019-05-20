package netty.glue;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server2 {
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss,worker)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 1024)
		.option(ChannelOption.SO_SNDBUF, 32 * 1024)
		.option(ChannelOption.SO_RCVBUF, 32 * 1024)
		.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new FixedLengthFrameDecoder(5))		//截断之后，或根据段数来决定调用后续Handler的次数
				.addLast(new StringDecoder())
				.addLast(new MyHandler())
				.addLast(new MyHandler2());
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
			ctx.writeAndFlush(Unpooled.copiedBuffer("qqqqqwwwwwee   ".getBytes()));
			ctx.fireChannelRead(msg);
		}
	}
	
	private static class MyHandler2 extends SimpleChannelInboundHandler<String>{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg)
				throws Exception {
			System.out.println(msg);
		}
	}
}
