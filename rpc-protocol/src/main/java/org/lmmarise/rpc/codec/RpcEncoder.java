package org.lmmarise.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.lmmarise.rpc.protocol.MsgHeader;
import org.lmmarise.rpc.protocol.RpcProtocol;
import org.lmmarise.rpc.serialization.RpcSerialization;
import org.lmmarise.rpc.serialization.SerializationFactory;

/**
 * RPC 协议报文编码器。
 *
 * <pre>
 * +---------------------------------------------------------------+
 * | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
 * +---------------------------------------------------------------+
 * | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
 * +---------------------------------------------------------------+
 * |                   数据内容 （长度不定）                          |
 * +---------------------------------------------------------------+
 * <pre/>
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 17:46
 */
@ChannelHandler.Sharable
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf out) throws Exception {
        MsgHeader header = msg.getHeader();
        out.writeShort(header.getMagic());
        out.writeByte(header.getVersion());
        out.writeByte(header.getSerialization());
        out.writeByte(header.getMsgType());
        out.writeByte(header.getStatus());
        out.writeLong(header.getRequestId());

        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(header.getSerialization());    // 获得序列化方式

        byte[] data = new byte[]{};         // 报文没有体，发一个空对象，数据长度设为 0
        if (msg.getBody() != null) {
            data = rpcSerialization.serialize(msg.getBody());
        }
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
