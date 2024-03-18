package io.qrpc.provider.common.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.qrpc.codec.RpcDecoder;
import io.qrpc.codec.RpcEncoder;
import io.qrpc.constants.RpcConstants;
import io.qrpc.provider.common.handler.RpcProviderHandler;
import io.qrpc.provider.common.manager.ProviderConnectionManager;
import io.qrpc.provider.common.server.api.Server;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: BaseServer
 * @Author: qiuzhiq
 * @Date: 2024/1/17 10:41
 * @Description: Server接口的实现类分直接实现类和间接实现类。rpc的服务提供者启动时的通用功能封装在本类中。
 * 所以本类直接实现Server接口并实现startNettyServer()f方法。
 * 另外再创建RpcSingleServer类来继承本类，实现只是用Java启动rpc框架。
 */

public class BaseServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServer.class);

    //netty服务端的默认ip与端口
    protected String host = "127.0.0.1";
    protected int port = 27110;
    private String reflectType;  //反射类型，是jdk还是cglib
    //保存扫描到的服务接口实现类的Class对象
    protected Map<String, Object> handlerMap = new HashMap<>();

    //注册中心
    protected RegistryService registryService;

    //7节，用于提供者定时发送心跳信息和移除连接缓存中的非活跃连接
    private ScheduledExecutorService executorService;
    private int heartbeatInterval = 3000;
    private int scanNotActiveChannelInterval = 6000;

    //结果缓存相关变量
    private boolean enableCacheResult;
    private int cacheResultExpire = 5000;

    //连接管理相关变量
    private int maxConnectionCount;
    private String disuseStrategyType;

    //服务容错-服务限流
    private boolean enableRateLimiter;
    private String rateLimiterType;
    private int permits;
    private int rateLimiterMilliSeconds;
    private String rateLimiterFailStrategy;
    //服务容错-服务熔断
    private boolean enableFusing;
    private String fusingStrategyType;
    private int totalFailure;
    private int fusingMilliSeconds;

    /**
     * @author: qiu
     * @date: 2024/2/24 16:59
     * @description: 22章修改，构造方法根据传入的注册中心地址和注册中心类型引入注册中心
     * 42章，构造方法增加负载均衡参数
     */
    public BaseServer(
            String serverAddr,
            String registryType,
            String registryAddr,
            String registryLoadBalanceType,
            String reflectType,
            int heartbeatInterval,
            int scanNotActiveChannelInterval,
            boolean enableCacheResult,
            int cacheResultExpire,
            int maxConnectionCount,
            String disuseStrategyType,
            boolean enableRateLimiter,
            String rateLimiterType,
            int permits,
            int rateLimiterMilliSeconds,
            String rateLimiterFailStrategy,
            boolean enableFusing,
            String fusingStrategyType,
            int totalFailure,
            int fusingMilliSeconds
    ) {
        if (!StringUtils.isEmpty(serverAddr)) {
            this.host = serverAddr.split(":")[0];
            this.port = Integer.parseInt(serverAddr.split(":")[1]);
        }
        this.reflectType = reflectType;
        this.registryService = getRegistryService(registryAddr, registryType, registryLoadBalanceType);

        //参数小于0，则使用默认值
        if (heartbeatInterval > 0)
            this.heartbeatInterval = heartbeatInterval;
        if (scanNotActiveChannelInterval > 0)
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;

        if (cacheResultExpire > 0)
            this.cacheResultExpire = cacheResultExpire;
        this.enableCacheResult = enableCacheResult;

        this.maxConnectionCount = maxConnectionCount;
        this.disuseStrategyType = disuseStrategyType;

        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.rateLimiterMilliSeconds = rateLimiterMilliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;

        this.enableFusing = enableFusing;
        this.fusingStrategyType = fusingStrategyType;
        this.totalFailure = totalFailure;
        this.fusingMilliSeconds = fusingMilliSeconds;
    }

    /**
     * @author: qiu
     * @date: 2024/2/24 17:05
     * @description: 22章新增，预留SPI扩展，目前先直接用new来给提供者引入注册中心
     * 42章，方法增加负载均衡参数
     */
    private RegistryService getRegistryService(String registryAddress, String registryType, String loadBalancer) {
        // 22章预留SPI扩展
        RegistryService registryService = null;
        //根据传入的注册地址与注册类型创建对应的注册中心服务
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
            registryService.init(new RegistryConfig(registryAddress, registryType, loadBalancer));
        } catch (Exception e) {
            LOGGER.error("RPC Server启动失败！：{}", e);
        }
        return registryService;
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 13:48
     * @description: 7节修改，增加IdleStateHandler
     */
    @Override
    public void startNettyServer() {
        //服务端启动后，开启自定义的心跳机制
        //有了Netty的心跳，不开
//        this.startHeartbeat();

        //创建Netty服务端
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(RpcConstants.CODEC_DEVODER, new RpcDecoder())
                                    .addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder())
//                                    .addLast(RpcConstants.CODEC_SERVER_IDEL_HANDLER, new IdleStateHandler(0, 0, heartbeatInterval + 2000, TimeUnit.MILLISECONDS))
                                    //由我们自定义的处理器来处理数据
                                    .addLast(RpcConstants.CODEC_HANDLER, new RpcProviderHandler(
                                            handlerMap,
                                            reflectType,
                                            enableCacheResult,
                                            cacheResultExpire,
                                            maxConnectionCount,
                                            disuseStrategyType,
                                            enableRateLimiter,
                                            rateLimiterType,
                                            permits,
                                            rateLimiterMilliSeconds,
                                            rateLimiterFailStrategy,
                                            enableFusing,
                                            fusingStrategyType,
                                            totalFailure,
                                            fusingMilliSeconds
                                    ));
                        }
                    })
                    //下面的设置对应tcp/ip协议, listen函数中的 backlog 参数，用来初始化服务端可连接队列。
                    //backlog用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程满时用来临时存放已完成三次握手的请求的对列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.info("Netty Server start on {}:{}", host, port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("rpc服务器启动失败：", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * @author: qiu
     * @date: 2024/3/4 9:30
     * @description: 7节，定义两个定时任务，分别清理和发送心跳
     */
    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        //定时扫描清理连接缓存中的非活跃连接
        executorService.scheduleAtFixedRate(() -> {
            LOGGER.info("提供者正在扫描清理非活跃的连接...");
            ProviderConnectionManager.removeNotActiveChannel();
        }, 10, this.heartbeatInterval, TimeUnit.MILLISECONDS);

        //定时发送ping信息
        executorService.scheduleAtFixedRate(ProviderConnectionManager::sendPingFromProvider, 10, this.scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);
    }
}
