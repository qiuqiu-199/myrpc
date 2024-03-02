package io.qrpc.provider.common.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.qrpc.codec.RpcDecoder;
import io.qrpc.codec.RpcEncoder;
import io.qrpc.provider.common.handler.RpcProviderHandler;
import io.qrpc.provider.common.server.api.Server;
import io.qrpc.registry.api.RegistryService;
import io.qrpc.registry.api.config.RegistryConfig;
import io.qrpc.registry.zookeeper.ZookeeperRegistryService;
import io.qrpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

    //netty服务端ip与端口
    protected String host = "127.0.0.1";
    protected int port = 27110;
    private String reflectType;  //反射类型，是jdk还是cglib
    protected Map<String, Object> handlerMap = new HashMap<>();

    protected RegistryService registryService;

    /**
     * @author: qiu
     * @date: 2024/2/24 16:59
     * @param: serverAddress
     * @param: registryAddress
     * @param: registryType
     * @param: reflectType
     * @return: null
     * @description: 22章修改，构造方法根据传入的注册中心地址和注册中心类型引入注册中心
     * 42章，构造方法增加负载均衡参数
     */
    public BaseServer(String serverAddress, String registryAddress, String registryType, String reflectType,String loadBalancer) {
        if (!StringUtils.isEmpty(serverAddress)) {
            this.host = serverAddress.split(":")[0];
            this.port = Integer.parseInt(serverAddress.split(":")[1]);
        }
        this.reflectType = reflectType;
        this.registryService = getRegistryService(registryAddress, registryType,loadBalancer);
    }

    /**
     * @author: qiu
     * @date: 2024/2/24 17:05
     * @param: registryAddress
     * @param: registryType
     * @return: io.qrpc.registry.api.RegistryService
     * @description: 22章新增，预留SPI扩展，目前先直接用new来给提供者引入注册中心
     * 42章，方法增加负载均衡参数
     */
    private RegistryService getRegistryService(String registryAddress, String registryType,String loadBalancer) {
        //TODO 22章预留SPI扩展
        RegistryService registryService = null;
        //根据传入的注册地址与注册类型创建对应的注册中心服务
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class,registryType);
            registryService.init(new RegistryConfig(registryAddress, registryType,loadBalancer));
        } catch (Exception e) {
            LOGGER.error("RPC Server启动失败！：{}", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //临时的编解码，后面会自己实现。
                                    //第7章实现
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    //由我们自定义的处理器来处理数据
                                    .addLast(new RpcProviderHandler(handlerMap, reflectType));
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
}
