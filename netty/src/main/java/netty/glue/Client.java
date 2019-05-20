package netty.glue;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;

public class Client {
	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ByteBuf buf = Unpooled.copiedBuffer("$".getBytes());
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,buf));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new MyHandler());
			}
		});
		
		ChannelFuture cf = bootstrap.connect("127.0.0.1",9999).sync();
		cf.channel().closeFuture().sync();
		
		group.shutdownGracefully();
	}
	
	private static class MyHandler extends SimpleChannelInboundHandler<String>{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg)
				throws Exception {
			System.out.println("server:" + msg);
			ReferenceCountUtil.release(msg);
			ctx.close();
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(Unpooled.copiedBuffer("hello Server$".getBytes()));
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			ctx.close();
		}
	}
}
