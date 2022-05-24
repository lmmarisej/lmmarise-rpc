package org.lmmarise.rpc.cusumer;

import org.lmmarise.rpc.common.RpcConstants;
import org.lmmarise.rpc.provider.registry.RegistryFactory;
import org.lmmarise.rpc.provider.registry.RegistryService;
import org.lmmarise.rpc.provider.registry.RegistryType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * 所有通过 @RpcReference 注解进行注入的 bean 实例都是 RpcReferenceBean 类型。
 * <p>
 * RpcReferenceBean 对所有调用 {@link RpcReferenceBean#interfaceClass} 属性指定的类型表中的所有类方法都会进行代理。
 *
 * <ul>
 * 原理：
 * <li>第一步，refresh：将 beanDefinitionMap 中的 beanDefinition 转为 BeanFactory 实例存入 beanFactory 中。
 * <li>第二步，getBean：根据 beanName 从 beanFactory 中获取 bean。
 * <li>第三步：如果发现 getBean 获取到的 Bean 类型是 BeanFactory 类型，再通过 BeanFactory#getObject 进一步获取 Bean 实例进行返回注入。
 * </ul>
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 16:17
 */
public class RpcReferenceBean implements FactoryBean<Object> {
    private Class<?> interfaceClass;
    private String serviceVersion;
    private String registryType;
    private String registryAddress;
    private long timeout;
    private Object object;

    @Override
    public Object getObject() throws Exception {         // 为 getBean 生成 Bean 实例
        return object;      // todo debug scope 范围是 Bean 实例 或者是 FactoryBean 又或者是 BeanDefinition
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    /**
     * 初始化方法，为本地接口生成实现，通过 {@link RpcReferenceBean#object} 属性引用实现。
     * <p>
     * 注意：bean 的初始化方法名要与 {@link RpcConstants#INIT_METHOD_NAME = "init"} 相同。
     */
    public void init() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddress, RegistryType.valueOf(this.registryType));
        this.object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService)
        );
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
