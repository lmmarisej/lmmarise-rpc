package org.lmmarise.rpc.serialization;

/**
 * 序列化异常。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 00:50
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = 3365624081242234230L;

    public SerializationException() {
        super();
    }

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
