package org.lmmarise.rpc.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负责具体的请求任务处理。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 01:15
 */
public class RpcRequestProcessor {

    /**
     * 延迟构建线程池。
     */
    private static final class ThreadPoolExecutorHolder {
        static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                10,
                10,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000)
        );
    }

    /**
     * 池化处理任务。
     */
    public static void submitRequest(Runnable task) {
        ThreadPoolExecutorHolder.threadPoolExecutor.submit(task);
    }
}
