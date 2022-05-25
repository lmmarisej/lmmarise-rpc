package org.lmmarise.rpc.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/25 16:39
 */
@Accessors(chain = true)
@Getter
@Setter
public class HeartBeatData implements Serializable {
    public static byte STATUS_OK = 0x01;        // 直接对应请求头中 status

    private byte status;
}