package netty.helloword;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws UnsupportedEncodingException{
		byte[]req = new byte[msg.readableBytes()];
		msg.readBytes(req);
		String body = new String(req,"utf-8");
		System.out.println("server : " + body);
		
		String response = "Hi Client";
		ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));			//会自动释放Buf
	}
}
