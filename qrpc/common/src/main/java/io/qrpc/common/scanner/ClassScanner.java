package io.qrpc.common.scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @ClassName: ClassScanner
 * @Author: qiuzhiq
 * @Date: 2024/1/15 21:34
 * @Description: 通用包扫描器
 */

public class ClassScanner {
    private static final String PROTOCOL_FILE = "file";  //扫描当前工程中指定包下的所有类信息
    private static final String PROTOCOL_JAR = "jar";  //扫描jar包中指定包下的所有类信息
    private static final String CLASS_FILE_SUFFIX = ".class";  //指定扫描过程中要处理的文件的后缀信息


    /**
     * f
     * 扫描指定包获取所有类名
     *
     * @param packageName 指定的要扫描的包
     * @return 存放了类名的List
     */
    public static List<String> getClassNameList(String packageName) throws Exception {
        ArrayList<String> classNameList = new ArrayList<>(); //用来存放类名
        boolean recursive = true; //循环迭代，TODO 不太明白，这个布尔类型的变量写死了

        String packageDirName = packageName.replace('.', '/'); //包名改成目录形式
        //定义枚举集合，循环处理
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            //例子：url=file:/E:/job/project/qrpc/test/test-scanner/target/classes/io/qrpc/test/scanner
            //如果以文件的形式保存在服务器上，获取包路径遍历所有文件  TODO ？？保存在服务器上？
            if (PROTOCOL_FILE.equals(protocol)) {
                //示例：filePath=/E:/job/project/qrpc/test/test-scanner/target/classes/io/qrpc/test/scanner
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                findAndAddClassesInPackageByFile(packageName, filePath, recursive, classNameList);
                //如果以jar包形式保存
            } else if (PROTOCOL_JAR.equals(protocol)) {
                packageName = findAndAddClassesInPackageByJar(packageName, classNameList, recursive, packageDirName, url);
            }
        }
        return classNameList;
    }


    /**
     * f
     * 实现功能：扫描当前工程指定包下的所有类信息
     *
     * @param packageName：扫描包名
     * @param packagePath：包的全路径
     * @param recursive：是否为递归调用
     * @param classNameList：类名称集合
     */
    private static void findAndAddClassesInPackageByFile(String packageName,
                                                         String packagePath,
                                                         final boolean recursive,
                                                         List<String> classNameList) {
        //对路径建立对象
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] dirFiles = dir.listFiles(new FileFilter() {
            //过滤规则：如果是递归调用并且file是个目录，或者，file是个class文件
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(CLASS_FILE_SUFFIX));
            }
        });
        //遍历路径下的所有class文件和目录
        for (File file : dirFiles) {
            //如果是个目录就递归进入扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + '.' + file.getName(),
                        file.getAbsolutePath(),
                        recursive,
                        classNameList);
                //否则将所有类名加入集合
            } else {
                String classsName = file.getName().substring(0, file.getName().length() - CLASS_FILE_SUFFIX.length());
                classNameList.add(packageName + '.' + classsName);
            }
        }
    }

    /**
     * f
     * 实现功能：扫描Jar包中指定包下的类信息。TODO 详细作用后面看看
     *
     * @param packageName    要扫描的包名
     * @param classNameList  同上
     * @param recursive      同上
     * @param packageDirName 当前包名的前面部分名称
     * @param url            包的url地址 TODO ？？
     * @return 处理后的包名，以供下次调用 TODO ？？
     * @throws Exception
     */
    private static String findAndAddClassesInPackageByJar(String packageName,
                                                          List<String> classNameList,
                                                          boolean recursive,
                                                          String packageDirName,
                                                          URL url) throws Exception {
        //创建JarFile对象
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        //从JarFile对象中得到枚举类
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            //迭代遍历，jar中的实体，可能是目录，也可能是文件，比如META-INF文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            //如果是/开头就截掉这个/
            if (name.charAt(0) == '/') name = name.substring(1);

            //筛选以packageDirName为开头的实体
            if (name.startsWith(packageDirName)) {
                //如果以/结尾是个包，获取包名并将所有/换成.
                int idx = name.lastIndexOf('/');
                if (idx != -1) packageName = name.substring(0, idx).replace('/', '.');

                //如果可以迭代并且是个包，
                if ((idx != -1) || recursive) {
                    if (name.endsWith(CLASS_FILE_SUFFIX) && !entry.isDirectory()) {
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        classNameList.add(packageName + '.' + className);
                    }
                }
            }
        }
        return packageName;
    }
}
