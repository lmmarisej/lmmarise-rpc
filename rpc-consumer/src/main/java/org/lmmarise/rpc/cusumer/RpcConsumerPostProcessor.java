package org.lmmarise.rpc.cusumer;

import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.common.RpcConstants;
import org.lmmarise.rpc.cusumer.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 处理 @RpcReference 注解，以便为 @RpcReference 使用者注入代理后的 bean 实例。
 *
 * @author lmmarise.j@gmail.com
 * @see RpcReference
 * @since 2022/5/24 15:53
 */
@Component
@Slf4j
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    private ApplicationContext context;

    private ClassLoader classLoader;

    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 处理所有的 BeanDefinition 中的 BeanClass，为所有使用了 @RpcReference 注解的字段添加一个对应的 BeanDefinition 到 IoC 容器。
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 找到所有使用了 @RpcReference 注解的字段
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
            }
        }

        // 为所有使用了 @RpcReference 注解的字段类型创建一个对应的实例，并在 IoC 容器中将该字段的属性名与实例进行映射
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);
            }
            // beanName + BeanDefinition 注册到容器，以便 @RpcReference 注解 byName 进行注入
            registry.registerBeanDefinition(beanName, beanDefinition);
            log.info("registered RpcReferenceBean {} success.", beanName);
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * 收集字段上的 @RpcReference 注解信息，由这些信息构造一个 BeanDefinition。
     */
    private void parseRpcReference(Field field) {
        RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class);     // 获得字段上 @RpcReference 注解的实例
        if (annotation != null) {
            // 设置 BeanDefinition 要生成的目标 Bean 类型，
            // 若该 beanClass 为 FactoryBean，将再调用 FactoryBean#getObject 生成 Bean 实例进行字段注入
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);

            // 设置 RpcReferenceBean 的初始化方法
            builder.setInitMethodName(RpcConstants.INIT_METHOD_NAME);

            // 设置 RpcReferenceBean 的默认属性值
            builder.addPropertyValue("interfaceClass", field.getType());     // 指定需要代理的接口；这里使用当前字段类型
            builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
            builder.addPropertyValue("registryType", annotation.registryType());
            builder.addPropertyValue("registryAddress", annotation.registryAddress());
            builder.addPropertyValue("timeout", annotation.timeout());
            builder.addPropertyValue("scope", annotation.scope());

            // 构造 BeanDefinition 实例
            BeanDefinition beanDefinition = builder.getBeanDefinition();

            // 将 BeanDefinition 实例注册到 IoC 容器，key 为被 @RpcReference 注解的字段名
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }
}
