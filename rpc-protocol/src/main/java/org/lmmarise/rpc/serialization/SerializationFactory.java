package org.lmmarise.rpc.serialization;

/**
 * 利用工厂模式构造序列化器。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 13:11
 */
public class SerializationFactory {

    /**
     * 根据序列化类型构造对应的序列化器。
     */
    public static RpcSerialization getRpcSerialization(byte serializationType) {
        SerializationTypeEnum typeEnum = SerializationTypeEnum.findByType(serializationType);
        switch (typeEnum) {
            case HESSIAN:
                return new HessianSerialization();
            case JSON:
                return new JsonSerialization();
            default:
                throw new IllegalArgumentException("serialization type is illegal, " + serializationType);
        }
    }

}
