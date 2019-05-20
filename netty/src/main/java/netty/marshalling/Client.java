package netty.marshalling;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>(){
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
				ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
				ch.pipeline().addLast(new MyHandler());
			}
		});
		
		ChannelFuture cf = bootstrap.connect("127.0.0.1",9999).sync();
		cf.channel().closeFuture().sync();
		group.shutdownGracefully();
	}
	
	private static class MyHandler extends SimpleChannelInboundHandler<Object>{
		@Override
		public void channelRead0(ChannelHandlerContext ctx, Object msg)
				throws Exception {
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(new Student(1,"张三",1));
		}
	}
}
