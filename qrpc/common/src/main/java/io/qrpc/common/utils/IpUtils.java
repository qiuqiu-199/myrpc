package io.qrpc.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @ClassName: IpUtils
 * @Author: qiuzhiq
 * @Date: 2024/3/1 9:32
 * @Description:
 */

public class IpUtils {
    private final static Logger log = LoggerFactory.getLogger(IpUtils.class);

    public static InetAddress getLoacalInetAddress(){
        try{
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("throw exception when get local ip address:{}",e);
        }
        return null;
    }

    public static String getLocalAddress(){
        return getLoacalInetAddress().toString();
    }

    public static String getLocalHostName(){
        return getLoacalInetAddress().getHostName();
    }

    public static String getLocalHostIp(){
        return getLoacalInetAddress().getHostAddress();
    }
}
