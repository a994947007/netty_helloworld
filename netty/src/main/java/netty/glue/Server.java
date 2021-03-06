package netty.glue;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server {
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss, worker)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 1024)
		.option(ChannelOption.SO_SNDBUF, 32 * 1024)
		.option(ChannelOption.SO_RCVBUF, 32 * 1024)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//设置分割符
				ByteBuf buf = Unpooled.copiedBuffer("$".getBytes());
				//两个参数，第一个参数是接收的数据包最大长度，第二个参数是分隔符
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, buf));	//在最外层设置一个粘包拆包处理器
				//如果传过来的是文本数据，那么可以直接定义一个字符串解码器
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
			System.out.println("client:" + msg);
			ctx.writeAndFlush(Unpooled.copiedBuffer("hello client$".getBytes()));
		}
	}
}
