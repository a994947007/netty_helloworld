package netty.helloword;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
	//收到数据时触发
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
			throws Exception {
			byte buf[] = new byte[msg.readableBytes()];
			msg.readBytes(buf);
			System.out.println(new String(buf,"utf-8"));
			ReferenceCountUtil.release(msg);
	}
	
	//通道激活时触发
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(Unpooled.copiedBuffer("hello Server!",CharsetUtil.UTF_8));
	}
	
	//异常直接关闭通道
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();			//打印异常
		ctx.close();
	}
}
