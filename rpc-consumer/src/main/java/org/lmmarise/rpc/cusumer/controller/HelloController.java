package org.lmmarise.rpc.cusumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.cusumer.annotation.RpcReference;
import org.lmmarise.rpc.facade.HelloFacade;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 22:12
 */
@RestController
@Slf4j
//@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HelloController {

    private static final ExecutorService executor = new ThreadPoolExecutor(16, 32,
            1L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(100000),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @RpcReference(serviceVersion = "1.0.0", timeout = 3000)
    private HelloFacade helloFacade;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public void sayHello() {
        log.info(helloFacade.hello("rpc sayHello"));
    }

    @RequestMapping(value = "/homework", method = RequestMethod.GET)
    public void homework() {
        executor.submit(() -> {
            try {
                log.info(helloFacade.homework("rpc homework").getPromise().get().toString());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
