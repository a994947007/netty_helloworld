package netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatClient {
    //client中可以讲ChannelFuture封装成一个pool，简单起见，就不完成这步
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ChannelFutureDriven driven = new ChannelFutureDriven(new ChatClientChannelInitializer());
        try{
            ChannelFuture cf = driven.createChannelFuture();
            while(scanner.hasNext()){
                cf.channel().writeAndFlush(scanner.next());
            }
        }finally {
        	driven.shutdownGracefully();
            scanner.close();
        }
    }

    /**
     * ChannelFuture池
     */
    private static class ChannelFuturePool{
        private BlockingQueue<ChannelFuture> queue = null;
        private ChannelFutureDriven driven = null;
        public ChannelFuturePool(int size,ChannelInitializer<SocketChannel> initializer){
            if(size < 0) throw new IllegalArgumentException("参数错误");
            driven = new ChannelFutureDriven(initializer);
            queue = new ArrayBlockingQueue<ChannelFuture>(size);
            init(size);
        }

        public void init(int size){
            for (int i = 0; i < size; i++) {
                queue.add(driven.createChannelFuture());
            }
        }

        public ChannelFuture take(){
            try {
                return queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void putBack(ChannelFuture channelFuture){
            try {
                queue.put(channelFuture);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void shutdownGracefully(){
            driven.shutdownGracefully();
        }
    }

    /**
     * 通过该驱动创建一个ChannelFuture对象
     */
    private static class ChannelFutureDriven{
        ChannelInitializer<SocketChannel> channelInitializer = null;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        public ChannelFutureDriven(ChannelInitializer<SocketChannel> channelInitializer){
            this.channelInitializer = channelInitializer;
            init();
        }
        public void init(){
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(channelInitializer);
        }
        public ChannelFuture createChannelFuture(){
            try {
                return bootstrap.connect("127.0.0.1",9999).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return  null;
        }

        public void shutdownGracefully(){
            group.shutdownGracefully();
        }
    }
}
