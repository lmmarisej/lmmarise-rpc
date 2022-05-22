package org.lmmarise.rpc.serialization;

import lombok.Getter;

/**
 * 描述 RPC 协议支持的序列化协议。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 00:55
 */
public enum SerializationTypeEnum {
    HESSIAN(0x10),
    JSON(0x20);

    @Getter
    private final int type;         // 用于在 RPC 报文请求头中传递

    SerializationTypeEnum(int type) {
        this.type = type;
    }

    /**
     * 根据 RPC 报文在协议头中指定的序列化类型值，以 Java 实例的方式进行描述。
     */
    public static SerializationTypeEnum findByType(byte serializationType) {
        for (SerializationTypeEnum typeEnum : SerializationTypeEnum.values()) {
            if (typeEnum.getType() == serializationType) {
                return typeEnum;
            }
        }
        return HESSIAN;
    }
}
