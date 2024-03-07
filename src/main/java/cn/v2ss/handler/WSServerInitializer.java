package cn.v2ss.handler;

import cn.v2ss.protocol.MessageBaseProto;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline()
                .addLast(new IdleStateHandler(30, 0, 0))
                // http请求的解码和编码
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ChunkedWriteHandler())
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler("/", null, Boolean.TRUE))
                .addLast(new MessageToMessageDecoder<WebSocketFrame>() {
                    @Override
                    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) throws Exception {
                        ByteBuf buf = frame.content();
                        objs.add(buf);
                        buf.retain();
                    }
                }).addLast(new MessageToMessageEncoder<MessageLiteOrBuilder>() {
                    @Override
                    protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
                        ByteBuf result = null;
                        if (msg instanceof MessageLite) {
                            result = wrappedBuffer(((MessageLite) msg).toByteArray());
                        }
                        if (msg instanceof MessageLite.Builder) {
                            result = wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
                        }

                        // ==== 上面代码片段是拷贝自TCP ProtobufEncoder 源码 ====
                        // 然后下面再转成websocket二进制流，因为客户端不能直接解析protobuf编码生成的
                        if (result != null) {
                            WebSocketFrame frame = new BinaryWebSocketFrame(result);
                            out.add(frame);
                        }
                    }
                })
                .addLast(new ProtobufDecoder(MessageBaseProto.MessageProto.getDefaultInstance()))
                .addLast(new WebsocketHandler());

    }
}
