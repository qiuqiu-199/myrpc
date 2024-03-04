package io.qrpc.provider.common.cache;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @ClassName: ProviderConnectionCache
 * @Author: qiuzhiq
 * @Date: 2024/3/3 23:26
 * @Description:
 */

public class ProviderConnectionCache {
    private static volatile Set<Channel> channelCache = new CopyOnWriteArraySet<>();

    //添加channel缓存
    public static void addChannel(Channel channel){channelCache.add(channel);}
    //移除channel缓存
    public static void removeChannel(Channel channel){channelCache.remove(channel);}
    //获取channel
    public static Set<Channel> getChannelCache(){return channelCache;}
}
