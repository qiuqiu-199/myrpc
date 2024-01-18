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
 *               所以本类直接实现Server接口并实现startNettyServer()f方法。
 *               另外再创建RpcSingleServer类来继承本类，实现只是用Java启动rpc框架。
 */

public class BaseServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServer.class);

    protected String host = "127.0.0.1";
    protected int port = 27110;
    protected Map<String,Object> handlerMap = new HashMap<>();

    public BaseServer(String serverAddress){
        if (!StringUtils.isEmpty(serverAddress)){
            this.host = serverAddress.split(":")[0];
            this.port = Integer.parseInt(serverAddress.split(":")[1]);
        }
    }

    @Override
    public void startNettyServer() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //临时的编解码，后面会自己实现。
                                    //第7章实现
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    //由我们自定义的处理器来处理数据
                                    .addLast(new RpcProviderHandler(handlerMap));
                        }
                    })
                    //下面的设置对应tcp/ip协议, listen函数中的 backlog 参数，用来初始化服务端可连接队列。
                    //backlog用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程满时用来临时存放已完成三次握手的请求的对列的最大长度
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.info("Server start on {}:{}",host,port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("rpc服务器启动失败：",e);
//            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
