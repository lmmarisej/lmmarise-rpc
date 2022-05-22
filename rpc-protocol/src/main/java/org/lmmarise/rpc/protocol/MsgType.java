package org.lmmarise.rpc.protocol;

import lombok.Getter;

/**
 * 对 RPC 传输协议报文类型进行描述。
 *
 * @see MsgHeader#getMsgType()
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 23:16
 */
public enum MsgType {
    REQUEST(1),
    RESPONSE(2),
    HEARTBEAT(3);

    @Getter
    private final int type;

    MsgType(int type) {
        this.type = type;
    }

    /**
     * 根据 type 值，返回对应的枚举实例。
     */
    public static MsgType findByType(int type) {
        for (MsgType msgType : MsgType.values()) {
            if (msgType.getType() == type) {
                return msgType;
            }
        }
        return null;
    }
}
