package io.qrpc.consumer.spring;

import io.qrpc.annotation.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @ClassName: RpcConsumerPostProcessor
 * @Author: qiuzhiq
 * @Date: 2024/3/6 12:57
 * @Description: 消费者端的后置处理器，主要为@RpcReference注解标注的字段创建bean
 */
@Component
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {
    private final static Logger log = LoggerFactory.getLogger(RpcConsumerPostProcessor.class);

    //通过ApplicationContextAware接口和BeanClassLoaderAware获取
    private ApplicationContext context;
    private ClassLoader classLoader;

    //缓存字段全类名及其对应的RpcReferenceBean的BeanDefinition
    private final Map<String, BeanDefinition> rpcRefBeanDefinitoins = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @author: qiu
     * @date: 2024/3/7 10:57
     * @description: 扫描标注RpcReference注解的字段并通过RpcReferenceBean注册字段的代理对象
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //beanFactory可以看成就是容器；
        //for循环遍历所有组件的BeanDefinition，准备对组件中标注了RpcReference注解的字段通过parseRpcReference方法生成对应的BeanDefinition
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                log.error("beanDefinitionName:{}",beanDefinitionName);
                log.error("beanClassName:{}",beanClassName);
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
            }
        }

        //在容器中注册字段对应的RpcReferenceBean的BeanDefinition
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.rpcRefBeanDefinitoins.forEach((beanName, beanDefinition) -> {
            log.error("beanName:{}",beanName);
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("容器已经包含有相同名的bean：" + beanName);
            }
            //注册BeanDefinition，后续会根据BeanDefinition注册组件
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitoins.get(beanName));
            log.info("在spring中注册RpcReferenceBean类型的bean：{}成功！", beanName);
        });
    }

    /**
     * @author: qiu
     * @date: 2024/3/7 10:32
     * @description: 解析字段标注@RpcReference注解，构建对应的FactoryBean组件的BeanDefinition
     */
    private void parseRpcReference(Field field) {
        log.error("field name :{}",field.getName() );
        RpcReference anno = AnnotationUtils.getAnnotation(field, RpcReference.class);
        //以下，根据字段标注的@RpcReference注解信息，创建字段对应的FactoryBean，通过FactoryBean获取到的就是该字段的代理对象。
        if (anno != null){
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);

            //设置初始化方法，就是调用哪个方法定制bean
            builder.setInitMethodName("init");

            //传递初始化所需属性
            builder.addPropertyValue("interfaceClass",field.getType());
            builder.addPropertyValue("version",anno.version());
            builder.addPropertyValue("group",anno.group());
            builder.addPropertyValue("registryType",anno.registryType());
            builder.addPropertyValue("registryAddr",anno.registryAddress());
            builder.addPropertyValue("registryLoadbalanceType",anno.registryLoadbalanceType());
            builder.addPropertyValue("serializationType",anno.serializationType());
            builder.addPropertyValue("proxyType",anno.proxyType());
            builder.addPropertyValue("timeout",anno.timeout());
            builder.addPropertyValue("async",anno.async());
            builder.addPropertyValue("oneway",anno.oneway());
            builder.addPropertyValue("heartbeatInterval",anno.heartbeatInterval());
            builder.addPropertyValue("scanNotActiveChannelInterval",anno.scanNotActiveChannelInterval());
            builder.addPropertyValue("maxRetryTimes",anno.maxRetryTimes());
            builder.addPropertyValue("retryInterval",anno.retryInterval());

            //缓存起来，<字段全类名，其BeanDefinition>
            this.rpcRefBeanDefinitoins.put(field.getName(),builder.getBeanDefinition());
        }
    }
}
