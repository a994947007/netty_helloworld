package netty.marshalling;

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
import io.netty.util.ReferenceCountUtil;

public class Server {
	
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss,worker);
		bootstrap.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 1024)
		.option(ChannelOption.SO_SNDBUF, 32 * 1024)
		.option(ChannelOption.SO_RCVBUF, 32 * 1024)
		.childOption(ChannelOption.SO_KEEPALIVE, true)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
				ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
				ch.pipeline().addLast(new MyHandler());
			}
		});
		
		ChannelFuture cf = bootstrap.bind(9999).sync();
		cf.channel().closeFuture().sync();
		
		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}
	
	private static class MyHandler extends SimpleChannelInboundHandler<Object>{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			try{
				Student student = (Student)msg;
				System.out.println(student);
			}finally{
				ReferenceCountUtil.release(msg);
			}
		}
	}
}
