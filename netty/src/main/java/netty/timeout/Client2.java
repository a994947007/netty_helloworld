package netty.timeout;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Client2 {
	public static void main(String[] args) throws InterruptedException {
		ChannelFuturePool pool = new ChannelFuturePool(5);
		ChannelFuture cf = pool.getChannelFuture();
		try{
			cf.channel().writeAndFlush(Unpooled.copiedBuffer("hello".getBytes()));	
		}finally{
			pool.putBack(cf);
		}
		cf.channel().closeFuture().sync();
		pool.shutdownGracefully();
	}
	
	//构建ChannelFuture连接池
	private static class ChannelFuturePool{
		BlockingQueue<ChannelFuture> que = null;
		
		public ChannelFuturePool(int n){
			if(n <0) throw new IllegalArgumentException();
			else{
				que = new ArrayBlockingQueue<ChannelFuture>(10);
				init(n);;
			} 
		}
		
		public void init(int n){
			for (int i = 0; i < n; i++) {
				que.add(ChannelDriven.getInstance().createChannelFuture());
			}
		}
		
		public ChannelFuture getChannelFuture(){
			try {
				return que.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public void putBack(ChannelFuture cf){
			try {
				que.put(cf);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void shutdownGracefully(){
			ChannelDriven.getInstance().shutdownGracefully();
		}
	}
	
	private static class MyHandler extends SimpleChannelInboundHandler<String>{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg)
				throws Exception {
			
		}
	}
	
	//
	private static class ChannelDriven{
		private static volatile ChannelDriven instance = null;
		private EventLoopGroup group = new NioEventLoopGroup();
		private Bootstrap bootstrap = new Bootstrap();
		static{
			if(instance == null){
				synchronized (ChannelDriven.class) {
					if(instance == null){
						instance = new ChannelDriven();
					}
				}
			}
		}
		private ChannelDriven(){
			bootstrap.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>(){
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new StringDecoder());
					ch.pipeline().addLast(new MyHandler());
				}});
		}
		public static ChannelDriven getInstance(){
			return instance;
		}
		public ChannelFuture createChannelFuture(){
			ChannelFuture cf = null;
			try {
				cf = bootstrap.connect("127.0.0.1",9999).sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return cf;
		}
		
		public void shutdownGracefully(){
			group.shutdownGracefully();
		}
	}
}
