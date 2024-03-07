package io.qrpc.spring.annotation.consumer;

/**
 * @InterfaceName: ConsumerDemoService
 * @Author: qiuzhiq
 * @Date: 2024/3/6 19:25
 * @Description: 可以认为是其他业务的接口
 */

public interface ConsumerDemoService {
    String otherBusiness(String str);
}
