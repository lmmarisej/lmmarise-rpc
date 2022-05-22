package org.lmmarise.rpc.protocol;

/**
 * 规定 RPC 协议头固定位置区域内的固定数据。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 23:29
 */
public class ProtocolConstants {
    public static final int HEADER_TOTAL_LEN = 18;
    public static final short MAGIC = 0x10;
    public static final byte VERSION = 0x1;
}
