package org.lmmarise.rpc.facade;

import java.util.concurrent.Future;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:10
 */
public interface HelloFacade {

    String hello(String name);

    Future<String> work(String thing);

}
