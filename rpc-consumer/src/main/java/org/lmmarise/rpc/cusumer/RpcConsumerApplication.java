package org.lmmarise.rpc.cusumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 15:51
 */
@EnableConfigurationProperties
@SpringBootApplication
public class RpcConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcConsumerApplication.class, args);
    }

}
