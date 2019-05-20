package netty.timeout;

import java.nio.charset.Charset;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.ReferenceCountUtil;

public class Client {

	public static void main(String[] args) throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(boss)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ReadTimeoutHandler(5));		//5秒内没有收到服务端的通信则断开连接
				ch.pipeline().addLast(new StringDecoder(Charset.forName("utf-8")));
				ch.pipeline().addLast(new MyHandler());
			}
		});
		
		bootstrap.connect("127.0.0.1",9999).sync().channel().closeFuture().sync();
		boss.shutdownGracefully();
	}
	
	private static class MyHandler extends SimpleChannelInboundHandler<String>{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg)
				throws Exception {
			System.out.println(msg);
			ReferenceCountUtil.release(msg);
		}
		
		@Override
		public void channelActive(final ChannelHandlerContext ctx) throws Exception {
			for (int i = 0; i < 3; i++) {
				ctx.writeAndFlush(Unpooled.copiedBuffer(("hello " + i).getBytes()));
				System.out.println(i);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			ctx.close();
		}
	}
}
