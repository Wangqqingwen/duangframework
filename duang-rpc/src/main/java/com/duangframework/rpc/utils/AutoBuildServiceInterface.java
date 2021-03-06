/**
 * FileName:     AutoBuildServiceInterface.java
 * All rights Reserved,
 * Copyright:    Copyright(C) 2010-2017
 * Company       sythealth
 *
 * @author: laotang
 * Createdate:  2017/12/2217:08
 */
package com.duangframework.rpc.utils;

import com.duangframework.core.annotation.mvc.Service;
import com.duangframework.core.annotation.rpc.Rpc;
import com.duangframework.core.exceptions.EmptyNullException;
import com.duangframework.core.exceptions.RpcException;
import com.duangframework.core.interfaces.IRpc;
import com.duangframework.core.kit.ConfigKit;
import com.duangframework.core.kit.ObjectKit;
import com.duangframework.core.kit.ToolsKit;
import com.duangframework.core.utils.BeanUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * 自动创建所有Service类的接口类文件
 * @author Created by laotang
 * @date on 2017/12/22.
 * @since 1.0
 */
public class AutoBuildServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(AutoBuildServiceInterface.class);

    /**
     *  批量创建Service类接口文件到指定目录下
     * @param interFaceDirPath      存放RPC接口文件的目录
     * @param  customizeDir           自定义目录
     * @return
     * @throws Exception
     */
    public static boolean createBatchInterface(String interFaceDirPath, String customizeDir)  throws Exception {
        Map<Class<?>, Object> serviceMap = BeanUtils.getAllBeanMaps().get(Service.class.getSimpleName());
        if(ToolsKit.isEmpty(serviceMap)) {
            throw new EmptyNullException("serviceMap is null");
        }
        try {
            for (Iterator<Class<?>> iterator = serviceMap.keySet().iterator(); iterator.hasNext(); ) {
                Class<?> clazz = iterator.next();
                String packagePath = RpcUtils.getRpcPackagePath(customizeDir);
                createInterface(clazz, interFaceDirPath, packagePath);
            }
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new RpcException("batch create service interface is fail: " + e.getMessage(), e);
        }
    }

    /**
     * 根据参数，创建接口类文件
     * @param clazz		要创建接口文件的类
     * @param interFaceDirPath  接口文件路径，不能包括文件名
     * @param packagePath 包路径名
     * @return
     */
    public static boolean createInterface(Class<?> clazz, String interFaceDirPath, String packagePath)  throws Exception {
        if(ToolsKit.isEmpty(interFaceDirPath)) { throw new RpcException("interFaceFilePath is null"); }
        if(interFaceDirPath.contains(".")) { throw new RpcException("interFaceFilePath only dir path"); }
        Set<String> excludedMethodName = ObjectKit.buildExcludedMethodName();
        Method[] methods = clazz.getMethods(); //只取public的方法
        String classPath = clazz.getName()+".";
        StringBuilder sb = new StringBuilder();
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get(clazz.getName());
            for(Method method : methods) {
                // 过滤掉Object.class里的公用方法及静态方法
                if (excludedMethodName.contains(method.getName()) || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                // 反射取出方法里的参数名
                List<String> variableNames = getLocalVariableAttributeName(cc, method);
                sb.append("\t").append(toGenericString(method, variableNames).replace(classPath, "")).append(";").append("\n\n");
            }
            // Service接口名
            String fileName = "I"+clazz.getSimpleName();
            // 创建接口类内容
            String fileContext = createInterfaceContextString(clazz.getName(), fileName, packagePath, sb.toString());

            File interFaceFileDir = new File(interFaceDirPath);
            // 文件夹不存在则创建
            if(!interFaceFileDir.exists() && interFaceFileDir.isDirectory()){
                logger.warn("dir is not exists, create it...");
                interFaceFileDir.mkdirs();
            }
            File interFaceFile = createInterFaceFileOnDisk(interFaceDirPath, fileName, fileContext);
            if(interFaceFile.isFile()) {
                logger.warn("create " + interFaceDirPath+"/"+fileName+".java is success!");
            }
            return true;
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        }
    }

    /**
     * 创建接口文件到指定目录下
     * @param interFaceFilePath     接口文件存放的目录地址
     * @param fileName  文件名
     * @param fileContext       文件内容
     * @return
     * @throws Exception
     */
    public static File createInterFaceFileOnDisk(String interFaceFilePath, String fileName, String fileContext) throws Exception {
        interFaceFilePath = interFaceFilePath.endsWith("/") ? interFaceFilePath.substring(0, interFaceFilePath.length()-1) : interFaceFilePath;
        interFaceFilePath = interFaceFilePath + "/"+ (fileName.endsWith(".java") ? fileName : fileName+".java");
        // 如果文件存在则先删除后再创建
        File interFaceFile = new File(interFaceFilePath);
        if(interFaceFile.exists() &&interFaceFile.isFile()){
            logger.warn("file is exists, delete it...");
            interFaceFile.delete();
        }
        FileUtils.writeStringToFile(interFaceFile, fileContext);
        return interFaceFile;
    }

    /**
     * 创建包路径
     * @param path
     * @return
     */
    private static String createPackagePath(String path) {
        int startIndex = path.contains("com") ? path.indexOf("com") : 0;
        path = path.substring(startIndex, path.length());
        File file = new File(path);
        return file.getPath().replace(File.separator, ".");
    }

    /**
     * 创建文件内容
     * @param clsName           Service类文件名
     * @param fileName          接口类文件名
     * @param packagePath     包路径
     * @param body                  文件内容
     * @return
     */
    private static String createInterfaceContextString(String clsName, String fileName, String packagePath, String body) {
        StringBuilder sb = new StringBuilder();
        String rpcPackage = Rpc.class.getName();
        String iRpcPackage = IRpc.class.getName();
        sb.append("package "+packagePath+";\n\n");
        sb.append("import "+rpcPackage+";\n");
        sb.append("import "+iRpcPackage+";\n");
        sb.append("/**\n");
        sb.append("*  根据"+clsName+"类文件自动构建接口文件\n");
        sb.append("*  自动对该类下所有public的方法进行处理(如非必须，无需要更改内容)\n\n");
        sb.append("*  @author duangframework\n");
        sb.append("*  @since 1.0\n");
        sb.append("*/\n");
        sb.append("@Rpc(service=\""+clsName+"\", productcode=\""+ ConfigKit.duang().key("product.code").defaultValue("dunagdunagduang").asString()+"\")\n");
        sb.append("public interface ").append(fileName).append(" extends IRpc {\n\n");
        sb.append(body);
        sb.append("}");
        return sb.toString();
    }

    /**
     * 构建方法内容字符串，包括方法名，参数名<br/>
     * @param method                 方法对象
     * @param variableNames      参数集合
     * @return
     */
    private static String toGenericString(Method method, List<String> variableNames)  {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = method.getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(" ");
            }
            TypeVariable<?>[] typeparms = method.getTypeParameters();
            if (ToolsKit.isNotEmpty(typeparms) && typeparms.length > 0) {
                boolean first = true;
                sb.append("<");
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            // 方法的返回类型
            Type genRetType = method.getGenericReturnType();
            sb.append(((genRetType instanceof Class<?>) ? getTypeName((Class<?>) genRetType) : genRetType.toString())).append(" ");
            sb.append(getTypeName(method.getDeclaringClass())).append(".");
            sb.append(method.getName()).append("(");
            Type[] params = method.getGenericParameterTypes();
            if(ToolsKit.isNotEmpty(params)) {
                for (int j = 0; j < params.length; j++) {
                    String param = (params[j] instanceof Class) ? getTypeName((Class) params[j]) : params[j].toString();
                    if (method.isVarArgs() && (j == params.length - 1)) {
                        param = param.replaceFirst("\\[\\]$", "...");
                    }
                    sb.append(param);
                    if (j < (params.length - 1)) {
                        sb.append(" " + variableNames.get(j) + ", ");
                    }
                }
                sb.append(" "+variableNames.get(params.length-1));
            }
            sb.append(")");
            Type[] exceptions = method.getGenericExceptionTypes();
            if (ToolsKit.isNotEmpty(exceptions) && exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class) ? ((Class) exceptions[k]).getName() : exceptions[k].toString());
                    if (k < (exceptions.length - 1)) {
                        sb.append(",");
                    }
                }
            }
            return (sb.length() > -1) ? sb.toString() : "";
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        }
    }

    /**
     * 反射取出方法里的参数名
     * @param cc                类对象
     * @param method        方法名
     * @return      方法名集合
     * @throws Exception
     */
    private static List<String> getLocalVariableAttributeName(CtClass cc, Method method)  throws Exception {
        List<String> paramNames = null;
        try {
            CtMethod cm = cc.getDeclaredMethod(method.getName());
            MethodInfo methodInfo = cm.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            if (attr != null) {
                int size = cm.getParameterTypes().length;
                paramNames = new ArrayList<>(size);
                int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
                for (int i = 0; i < size; i++)  {
                    paramNames.add(attr.variableName(i + pos));
                }
            }
        } catch (NotFoundException e) {
           throw new RpcException(e.getMessage(), e);
        }
        return paramNames;
    }

    /**
     * 根据参数类型取出参数类型名称字符串
     * @param type      参数类型
     * @return
     * @throws Exception
     */
    private static String getTypeName(Class<?> type)  throws Exception {
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuffer sb = new StringBuffer();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
                throw new RpcException(e.getMessage(), e);
            }
        }
        return type.getName();
    }
}
