package cn.v2ss.handler;

import cn.v2ss.protocol.MessageBaseProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class WebsocketHandler extends SimpleChannelInboundHandler<MessageBaseProto.MessageProto> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageBaseProto.MessageProto proto) throws Exception {
        if (proto != null) {
            System.out.println(proto.getContent());
        } else {
            System.out.println("没有消息内容");
        }
    }
}
