package io.qrpc.spi.loader;

import io.qrpc.spi.annotation.SPI;
import io.qrpc.spi.annotation.SpiClass;
import io.qrpc.spi.factory.ExtensionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName: ExtensionLoader
 * @Author: qiuzhiq
 * @Date: 2024/2/27 14:30
 * @Description: 25章新增，仿写Dubbo的ExtensionLoader加载器，加载扩展配置问价解析接口对应的实现类
 */

public class ExtensionLoader<T> {
    private final static Logger log = LoggerFactory.getLogger(ExtensionLoader.class);

    //扩展配置文件应在目录的定义，扩展配置文件应以接口全类名命名
    private final static String SERVICES_DIRECTORY = "META-INF/services/";
    private final static String Q_DIRECTORY = "META-INF/q/";
    private final static String Q_DIRECTORY_EXTERNAL = "META-INF/q/external/";
    private final static String Q_DIRECTORY_INTERNAL = "META-INF/q/internal/";
    private final static String[] SPI_DIRECTORYS = new String[]{
            SERVICES_DIRECTORY,
            Q_DIRECTORY,
            Q_DIRECTORY_EXTERNAL,
            Q_DIRECTORY_INTERNAL
    };

    //当前扩展类加载器对应的Class
    private final Class<T> clazz;
    //当前扩展类加载器对应的类加载器
    private final ClassLoader classLoader;

    //存储每一种Class对应的扩展类加载器，每一个实现类的Class对象对应不同的ExtensionLoader
    private final static Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    //cachedClasses使用Holder存储一个map，map存储实现类标识及对应的Class对象。Holder用于减少锁冲突。
    //cachedInstance保存的是扩展配置文件中的key及其对应的实例容器
    //spiClassInstance保存真正的实现类的Class独享及对应的实例对象
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> spiClassInstances = new ConcurrentHashMap<>();
    //加载扩展类并获取实现类的对象的处理过程基本从上面三个map里寻找，若不存在，就使用Holder中的volatile+双重检测锁来创建
    // 其中cachedClass的逻辑最为复杂，因为需要处理扩展配置文件，解析出kv对保存起来

    //接口的默认实现类的标识
    private String cachedDefautName;

    //构造方法
    private ExtensionLoader(final Class<T> clazz, final ClassLoader classLoader) {
        this.clazz = clazz;
        this.classLoader = classLoader;

        //构造方法里先加载ExtensionFactory的扩展类 TODO 为什么这么做？
        if (!Objects.equals(clazz, ExtensionFactory.class)) {
            ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getExtensionClasses();
        }
    }

    /**
     * @author: qiu
     * @date: 2024/2/28 0:17
     * @param: clazz
     * @param: name
     * @return: T
     * @description: 25章新增，对外提供。根据入参Class对象和实现类标识从扩展配置文件中加载实现类，返回实现类对象。
     * 如果没有提供实现类标识，那么返回默认实现类的对象，否则返回指定的实现类的对象。
     */
    public static <T> T getExtension(final Class<T> clazz, String name) {

        return StringUtils.isEmpty(name) ?
                getExtensionLoader(clazz)
                        .getDefaultSpiClassInstance()
                :
                getExtensionLoader(clazz)
                        .getSpiClassInstance(name);
    }

    /**
     * @author: qiu
     * @date: 2024/2/28 0:22
     * @param: clazz
     * @return: io.qrpc.spi.loader.ExtensionLoader<T>
     * @description: 25章新增，对外提供。获取接口的扩展类加载器。
     * 根据入参Class对象返回对应的ExtensionLoader。
     * 实际上调用的是重载方法<T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz, ClassLoader cl)
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        return getExtensionLoader(clazz, ExtensionLoader.class.getClassLoader());
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 15:38
     * @param: clazz
     * @param: cl
     * @return: io.qrpc.spi.loader.ExtensionLoader<T>
     * @description: 25章新增，对外提供。获取接口的扩展类加载器。
     * 根据入参Class对象和类加载器（实际上就是ExtensionLoader的类加载器）为每一个属于接口并标记了SPI注解的接口的Class对象创建对应的扩展类加载器ExtensionLoader。
     * 因此一个接口对应一个ExtensionLoader。
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz, ClassLoader cl) {
        Objects.requireNonNull(clazz, "extension clazz is null");
        //过滤非接口的Class对象
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") is not interface");
        }
        //过滤没有标记SPI注解的接口
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") without @" + SPI.class + " Annotation");
        }

        //先从LOADER里获取，没有就创建对应的扩展类加载器
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (Objects.nonNull(extensionLoader)) {
            return extensionLoader;
        }

        LOADERS.putIfAbsent(clazz, new ExtensionLoader<>(clazz, cl));

        return (ExtensionLoader<T>) LOADERS.get(clazz);
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 15:44
     * @param:
     * @return: T
     * @description: 25章新增，创建扩展类加载器后才能调用。获取接口的默认实现类对象。
     * 先调用getExtensionClasses方法加载扩展配置文件解析实现类标识和实现类Class对象的键值对，解析过程中会另外配置好默认实现类名cachedDefaultName，如果不为空，调用getSpiClassInstance方法获取并返回默认实现类的对象？ TODO
     */
    public T getDefaultSpiClassInstance() {
        getExtensionClasses();  //这一步是解析扩展配置文件解析kv对，并附带地获取默认实现类的全类名存入cachedDefautName

        if (StringUtils.isBlank(cachedDefautName)) {
            return null;
        }
        return getSpiClassInstance(cachedDefautName);
    }


    /**
     * @author: qiu
     * @date: 2024/2/28 0:36
     * @param: name
     * @return: T
     * @description: 25章新增，创建扩展类加载器后才能调用。根据实现类标识获取实现类对象
     * 先从cachedInstances里获取Holder，如果没有就创建，并通过双重检查锁调用createExtension方法根据实现类标识创建实现类对象
     */
    public T getSpiClassInstance(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalStateException("get spi classname is null");
        }

        Holder<Object> objectHolder = cachedInstances.get(name);
        if (Objects.isNull(objectHolder)) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            objectHolder = cachedInstances.get(name);
        }

        Object value = objectHolder.getValue();
        if (Objects.isNull(value)) {
            synchronized (cachedInstances) {
                value = objectHolder.getValue();
                if (Objects.isNull(value)) {
                    value = createExtension(name);
                    objectHolder.setValue(value);
                }
            }
        }
        return (T) value;
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 23:15
     * @param:
     * @return: java.util.List<T>
     * @description: 25章新增，创建扩展类加载器后才能调用。获取接口所有的扩展类的实例对象。
     * 先调用getExtensionClasses方法从cachedClasses里获取加载和解析好的Class对象，为空则返回空列表
     * 再判断得到的kv对的大小和cachedInstances大小是否一样，这一步判断加载的扩展类是否都实例化了，如果一样，直接返回实例化好的扩展类的列表
     * 如果不一样就自己对从cachedClass里拿到的Class对象一个一个调用getSpiClassInstance去实例化，最终返回一个包含全部扩展类的实例对象的列表
     */
    public List<T> getSpiClassInstances() {
        Map<String, Class<?>> extensionClasses = this.getExtensionClasses();
        if (extensionClasses.isEmpty()) {
            return Collections.emptyList();
        }

        if (Objects.equals(extensionClasses.size(), cachedInstances.size())) {
            return (List<T>) this.cachedInstances.values().stream().map(e -> {
                return e.getValue();
            }).collect(Collectors.toList());
        }

        List<T> instances = new ArrayList<>();
        extensionClasses.forEach((name, v) -> {
            T instance = this.getSpiClassInstance(name);
            instances.add(instance);
        });

        return instances;
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 21:59
     * @param: name
     * @return: T
     * @description: 25章新增，根据实现类标识创建对于扩展类的对象。
     * 首先根据参数实现类标识从cachedClasses里得到对应的实现类的Class对象，
     * 再根据Class对象从spiClassInstances中得到对应的实现类的对象，如果没有就直接通过Class对象创建，最后返回实现类的对象
     */
    private T createExtension(final String name) {
        //这一步会加载扩展配置文件并解析kv对
        Class<?> aClass = getExtensionClasses()
                .get(name);
        if (Objects.isNull(aClass)) {
            System.out.println(name);
            throw new IllegalStateException("name is error");
        }
        //先试图从spiInstances中获取需要的扩展类的对象，如果为空说明还没有创建对象，就通过Class对象创建实现类的对象存入spiInstances中，创建失败就抛异常
        Object o = spiClassInstances.get(aClass);
        if (Objects.isNull(o)) {
            try {
                spiClassInstances.putIfAbsent(aClass, aClass.newInstance());
                o = spiClassInstances.get(aClass);
            } catch (IllegalAccessException | InstantiationException e) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: " + aClass + ") could not be instantiated: " + e.getMessage(), e);
            }
        }
        return (T) o;
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 21:37
     * @param:
     * @return: void
     * @description: 25章新增，获取
     * 首先从cachedClass中获取已经加载好的<key，对应的实现类的Class对象>对，如果为空说明还没加载，通过双重检查锁来加载扩展配置文件解析出<key，对应的实现类的Class对象>对
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if (Objects.isNull(classes)) {
            synchronized (cachedClasses) {
                classes = cachedClasses.getValue();
                if (Objects.isNull(classes)) {
                    //这一步根加载展配置文件并解析实现类标识和实现类的Class对象的键值对
                    classes = loadExtensionClass();
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 20:08
     * @param:
     * @return: java.util.Map<java.lang.String, java.lang.Class < ?>>
     * @description: 25章新增，根据接口的SPI注解的value属性获取接口的默认实现类的类名，然后调用loadExtension方法读取扩展配置文件将<key，对应的实现类的Class对象>返回
     */
    private Map<String, Class<?>> loadExtensionClass() {
        SPI annotation = clazz.getAnnotation(SPI.class);
        if (Objects.nonNull(annotation)) {
            String value = annotation.value();
            if (StringUtils.isBlank(value))
                cachedDefautName = value;
        }
        Map<String, Class<?>> classes = new HashMap<>();
        //这一步从定义好的目录中加载扩展类
        loadDiretory(classes);
        return classes;
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 20:07
     * @param: classes
     * @return: void
     * @description: 25章新增。遍历四个定义好的目录，加载目录中的扩展配置文件，从而加载文件中key对应的实现类Class对象
     */
    private void loadDiretory(final Map<String, Class<?>> classes) {
        for (String dir : SPI_DIRECTORYS) {
            String fileName = dir + clazz.getName();
            File file = new File(fileName);
            boolean res = file.exists();
            try {
                Enumeration<URL> urls;
                if( Objects.nonNull(this.classLoader))
                    urls = classLoader.getResources(fileName);
                else
                    urls = ClassLoader.getSystemResources(fileName);

                if (Objects.nonNull(urls)) {
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        //这一步读取扩展配置文件并解析实现类标识及实现类的Class对象
                        loadResource(classes, url);
                    }
                }
            } catch (IOException e) {
                log.error("load extension class error: {}", fileName, e);
            }
        }
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 17:06
     * @param: classes
     * @param: url
     * @return: void
     * @description: 23章新增，加载扩展配置文件，提取文件中的kv对，并加载key对应的实现类的class对象存入map中
     */
    private void loadResource(Map<String, Class<?>> classes, URL url) {
        //
        try (InputStream in = url.openStream()) {
            Properties properties = new Properties();
            properties.load(in);

            properties.forEach((k, v) -> {
                String name = (String) k;
                String classPath = (String) v;

                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(classPath)) {
                    try {
                        loadClass(classes, name, classPath);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("load extension resources error", e);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }

    /**
     * @author: qiu
     * @date: 2024/2/27 17:03
     * @param: classes
     * @param: name
     * @param: classPath
     * @return: void
     * @description: 23章新增。加载key对应的实现类的class对象，存入map中
     */
    private void loadClass(final Map<String, Class<?>> classes,
                           final String name,
                           final String classPath) throws ClassNotFoundException {
        Class<?> subClass = Objects.nonNull(this.classLoader) ?
                Class.forName(classPath, true, this.classLoader)
                :
                Class.forName(classPath);

        //如果不是指定接口的子类
        if (!clazz.isAssignableFrom(subClass)) {
            throw new IllegalStateException("load extension resources error," + subClass + "is not subType of" + clazz);
        }
        //如果没有SiClass注解
        if (!subClass.isAnnotationPresent(SpiClass.class)) {
            throw new IllegalStateException("load extension resource error," + subClass + " without @" + SpiClass.class + " annotation");
        }

        //如果map里没有直接存入，否则抛异常
        Class<?> oldClass = classes.get(name);
        if (Objects.isNull(oldClass)) {
            classes.put(name, subClass);
        } else if (!Objects.equals(oldClass, subClass)) {
            throw new IllegalStateException("load extension resource error, Duplicate class " + clazz.getName() + " name " + name + " on " + oldClass.getName() + " or " + subClass.getName());
        }
    }
//    private <T> T getSpiClassInstance(String name){
//        return null;
//    }


    /**
     * @author: qiu
     * @date: 2024/2/27 15:41
     * @description: 用于存储解析扩展配置文件得到的<key, 实现类的Class对象>键值对，用于减少锁冲突的出现。
     */
    public static class Holder<T> {
        private volatile T value;

        public T getValue() {
            return this.value;
        }

        public void setValue(final T v) {
            this.value = v;
        }
    }
}
