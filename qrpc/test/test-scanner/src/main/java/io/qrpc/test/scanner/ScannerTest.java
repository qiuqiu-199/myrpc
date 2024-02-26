package io.qrpc.test.scanner;

import io.qrpc.common.scanner.ClassScanner;
import io.qrpc.common.scanner.reference.ReferenceScanner;
import io.qrpc.common.scanner.service.RpcServiceScanner;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ScannerTest
 * @Author: qiuzhiq
 * @Date: 2024/1/16 15:55
 * @Description:
 */

public class ScannerTest {

    @Test
    //通用包扫描器，扫描指定包下所有类的路径存入list
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("io.qrpc.test.scanner");
        classNameList.forEach(System.out::println);
    }

    @Test
    public void testScannerClassNameListByRpcService() throws Exception {
        //map暂时是空的，因为还没有生成对应的代理对象
        Map<String, Object> map = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService("0", 0, "io.qrpc.test.scanner");
    }

    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        //map暂时是空的，因为还没有生成对应的代理对象
        Map<String, Object> map = ReferenceScanner.doScannerWithReferenceAnnotationFilter("io.qrpc.test.scanner");
    }

}
