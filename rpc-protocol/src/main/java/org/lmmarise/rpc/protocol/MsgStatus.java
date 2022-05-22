package org.lmmarise.rpc.protocol;

import lombok.Getter;

/**
 * 对 RPC 请求操作的响应状态进行描述。
 *
 * @see MsgHeader#getStatus()
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 23:14
 */
public enum MsgStatus {
    SUCCESS(0),
    FAIL(1);

    @Getter
    private final int code;

    MsgStatus(int code) {
        this.code = code;
    }
}
