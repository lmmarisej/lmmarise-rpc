package org.lmmarise.rpc.provider.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者使用。
 *
 * 通过注解在类上，将类实现的指定接口暴露到注册中心。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component          // @RpcService 继承 @Component 将被注解类自动实例化并放入容器等功能
public @interface RpcService {

    Class<?> serviceInterface() default Object.class;

    String serviceVersion() default "1.0";

}
