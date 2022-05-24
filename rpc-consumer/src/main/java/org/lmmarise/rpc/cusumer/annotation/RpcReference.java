package org.lmmarise.rpc.cusumer.annotation;

import org.lmmarise.rpc.cusumer.RpcConsumerPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端引用远程服务的注解，在注解属性中指定要引用的具体远程服务信息。
 *
 * @see RpcConsumerPostProcessor 为本注解提供支持。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 15:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired      // @RpcReference 继承 @Autowired 的自动注入等功能
public @interface RpcReference {

    String serviceVersion() default "1.0";

    String registryType() default "ZOOKEEPER";

    String registryAddress() default "127.0.0.1:2181";      // 指定注册中心默认地址

    long timeout() default 5000;

    String scope() default BeanDefinition.SCOPE_SINGLETON;

    String scope() default BeanDefinition.SCOPE_SINGLETON;      // 作用域，注意：多例模式不能在单例 bean 中引用
}
