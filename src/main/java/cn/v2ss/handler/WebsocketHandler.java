package cn.v2ss.handler;

import cn.v2ss.protocol.MessageBaseProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;


public class WebsocketHandler extends SimpleChannelInboundHandler<MessageBaseProto.MessageProto> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageBaseProto.MessageProto proto) {
        if (proto != null) {
            System.out.println(proto.getContent());
            MessageBaseProto.MessageProto backMsg = MessageBaseProto.MessageProto.newBuilder()
                    .setContent("服务端的内容")
                    .setRoomId("1234")
                    .setFromId("33333").build();
            ctx.writeAndFlush(backMsg);
        } else {
            System.out.println("没有消息内容");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println("有新连接进来" + address.toString());

    }
}
