package org.lmmarise.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.common.RpcRequest;
import org.lmmarise.rpc.common.RpcResponse;
import org.lmmarise.rpc.protocol.*;
import org.lmmarise.rpc.serialization.RpcSerialization;
import org.lmmarise.rpc.serialization.SerializationFactory;

import java.util.List;

/**
 * 对接收到的流量解码为 RPC 协议报文。
 * <p>
 * 按照以下协议结构进行解码：
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
 * @since 2022/5/23 12:20
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {      // 首先确保获得完整的协议头，避免解析协议头时阻塞
            return;
        }

        in.markReaderIndex();       // 记录读指针，保存开始解析协议头的位置

        short magic = in.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }

        byte version = in.readByte();
        byte serializeType = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();
        int dataLength = in.readInt();

        if (in.readableBytes() < dataLength) {      // 判断消息体是否接收完
            in.resetReaderIndex();                  // 读指针此时为距离上次标记点增加了 18（协议头的长度），重置为上次标记点的位置
            return;                                 // 读指针归为到协议头位置后，本次 I/O 触发事件不再处理
        }

        byte[] data = new byte[dataLength];
        if (dataLength > 0) {
            in.readBytes(data);                         // 数据拷贝到 Java 堆
        }

        MsgType msgTypeEnum = MsgType.findByType(msgType);          // 数据包类型
        if (msgTypeEnum == null) {
            return;           // 报文体数据是不支持的类型。读完后，读指针既不复位，取出的数据也不处理，表示废弃这部分数据
        }

        MsgHeader header = new MsgHeader();         // RPC 报文头用 Java 实例来描述
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(serializeType);

        switch (msgTypeEnum) {
            case REQUEST:           // 本次数据包为客户端请求，将请求体反序列化到 Java RpcRequest 对象
                RpcRequest request = rpcSerialization.deserialize(data, RpcRequest.class);          // 获取报文体，反序列化
                if (request != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            case RESPONSE:           // 本次数据包为服务端响应，将响应体反序列化到 Java RpcResponse 对象
                RpcResponse response = rpcSerialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                log.info("收到心跳请求：from channel = {}, requestId = {}", ctx.channel(), requestId);
                out.add(new HeartBeatData().setStatus(header.getStatus()));
                break;
        }
    }

}
