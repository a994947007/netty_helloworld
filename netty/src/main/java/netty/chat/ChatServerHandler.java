package netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 每一个channel对应一个channelhandler，这里不是单例
 * @author 路遥
 *
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
    private static List<Channel> channelList = new ArrayList<Channel>();
    //接收并转发数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        for (Channel c : channelList){
            if(c != channel){
                c.writeAndFlush(channel.remoteAddress() + ":" + msg);
            }
        }
    }

    //通道激活
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("客户端：" + channel.remoteAddress().toString() + "已上线");
        channelList.add(channel);
    }

    //通道离线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("客户端：" + channel.remoteAddress() + "已离线");
        channelList.remove(channel);
    }
}
