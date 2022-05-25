package org.lmmarise.rpc.common;

/**
 * 通用常量。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:49
 */
public class RpcConstants {
    /**
     * 指定 bean 的初始化方法名。
     *
     * {@code 例如：org.lmmarise.rpc.cusumer.RpcReferenceBean#init()}
     */
    public static final String INIT_METHOD_NAME = "init";

    public static final int RPC_IDLE_OVERTIME = 6;          // 连接超时时间

    public static final long RPC_PING_DELAY_TIME = 5;       // ping 间隔时间
}
