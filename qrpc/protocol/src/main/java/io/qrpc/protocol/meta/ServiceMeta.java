package io.qrpc.protocol.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: ServiceMeta
 * @Author: qiuzhiq
 * @Date: 2024/2/22 10:22
 * @Description: 21章，服务注册到注册中心所需元数据
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMeta implements Serializable {
    private static final long serialVersionUID = -4337966962410555646L;
    private String serviceName;//服务名
    private String serviceVersion;//服务版本
    private String serviceGroup;//服务分组
    private String serviceAddr;//服务ip
    private int servicePort;//服务端口
    private int weight; //服务权重，权重越高，选中该服务的机会越大
}
