package org.lmmarise.rpc.cusumer.controller;

import org.lmmarise.rpc.common.RpcResponse;
import org.lmmarise.rpc.cusumer.annotation.RpcReference;
import org.lmmarise.rpc.facade.HelloFacade;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 22:12
 */
@RestController
//@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HelloController {

    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @RpcReference(serviceVersion = "1.0.0", timeout = 3000)
    private HelloFacade helloFacade;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String sayHello() {
        return helloFacade.hello("rpc sayHello");
    }

    @RequestMapping(value = "/homework", method = RequestMethod.GET)
    public String homework() throws ExecutionException, InterruptedException {
        RpcResponse rpcResponse = helloFacade.homework("rpc homework").getPromise().get();
        if (rpcResponse.getData() != null) {
            return (String) rpcResponse.getData();
        } else {
            return rpcResponse.getMessage();
        }
    }
}
