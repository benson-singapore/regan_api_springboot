package com.regan.api.jboot.api.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Tag;
import org.springframework.util.ResourceUtils;
import com.regan.api.jboot.api.aop.ApiIgnore;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * java注释解析
 *
 * @author zhangby
 * @date 2018/5/23 下午1:55
 */
public class AnnotationParse {
    //解析实例
    private static AnnotationParse annotationParse;
    //源码路径
    private static String SRC_PAth;

    static {
        try {
            SRC_PAth = (ResourceUtils.getURL("classpath:").getPath().contains("/target/classes")?
                    CommUtil.splitStr4Temp(ResourceUtils.getURL("classpath:").getPath(),"{}/target/classes").get(0):
                        ResourceUtils.getURL("classpath:").getPath().contains("\\target\\classes")?
                        CommUtil.splitStr4Temp(ResourceUtils.getURL("classpath:").getPath(),"{}\\target\\classes").get(0):
                        ResourceUtils.getURL("classpath:").getPath()) + "/src/main/java/";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //解析的package
    private List<String> packages = Lists.newArrayList();
    //需要过滤的Controller
    private List<String> filters = Lists.newArrayList();

    /**
     * 创建扫描解析实例
     * @param packages 需要解析的包
     */
    public static AnnotationParse getInstance(String... packages) {
        if (Objects.isNull(annotationParse)) {
            annotationParse = new AnnotationParse();
            annotationParse.packages = Lists.newArrayList(packages);
            return annotationParse;
        } else {
            annotationParse.packages = Lists.newArrayList(packages);
        }
        return annotationParse;
    }

    /**
     * 创建扫描解析实例
     * @param packages 需要解析的包
     */
    public static AnnotationParse getInstance(List<String> packages) {
        if (Objects.isNull(annotationParse)) {
            annotationParse = new AnnotationParse();
            annotationParse.packages = packages;
            return annotationParse;
        } else {
            annotationParse.packages = packages;
        }
        return annotationParse;
    }

    /**
     * 增加过滤controller，需要controller的名称
     * @param filter controllerName
     * @return
     */
    public AnnotationParse filter(String... filter) {
        //过滤controller，统一转成小写
        if (Objects.isNull(annotationParse)) {
            annotationParse = new AnnotationParse();
            annotationParse.filters = Lists.newArrayList(filter).stream().map(f->f.toLowerCase().trim()).collect(Collectors.toList());
            return annotationParse;
        } else {
            annotationParse.filters = Lists.newArrayList(filter).stream().map(f->f.toLowerCase().trim()).collect(Collectors.toList());;
        }
        return annotationParse;
    }

    /**
     * 增加过滤controller，需要controller的名称
     * @param filter controllerName
     * @return
     */
    public AnnotationParse filter(List<String> filter) {
        //过滤controller，统一转成小写
        if (Objects.isNull(annotationParse)) {
            annotationParse = new AnnotationParse();
            annotationParse.filters = filter.stream().map(f->f.toLowerCase()).collect(Collectors.toList());
            return annotationParse;
        } else {
            annotationParse.filters = filter.stream().map(f->f.toLowerCase()).collect(Collectors.toList());;
        }
        return annotationParse;
    }

    /**
     * 数据加载与读取
     */
    public ApiClassDoc getApiData() {
        ApiClassDoc apiClassDoc = new ApiClassDoc();
        if (!packages.isEmpty()) {
            baseParseApiData(apiClassDoc,null,null,false);
        }
        return apiClassDoc;
    }

    /**
     * 数据加载与读取
     * @param action controller名称 UserController -> userController
     */
    public ApiClassDoc getApiData(String action) {
        ApiClassDoc apiClassDoc = new ApiClassDoc();
        if (!packages.isEmpty()) {
            baseParseApiData(apiClassDoc,f->getLowName4EndWithJava(f.getName()).equals(action.toLowerCase()),null,false);
        }
        return apiClassDoc;
    }



    /**
     * 此方法会读取Controller内部的所有方法
     * @return
     */
    public ApiClassDoc getApiData4Depth() {
        ApiClassDoc apiClassDoc = new ApiClassDoc();
        if (!packages.isEmpty()) {
            baseParseApiData(apiClassDoc,null,null,true);
        }
        return apiClassDoc;
    }

    /**
     * 此方法会读取Controller内部的所有方法
     * @param action controller名称 UserController -> userController
     * @return
     */
    public ApiClassDoc getApiData4Depth(String action) {
        ApiClassDoc apiClassDoc = new ApiClassDoc();
        if (!packages.isEmpty()) {
            baseParseApiData(apiClassDoc,f->getLowName4EndWithJava(f.getName()).equals(action.toLowerCase()),null,true);
        }
        return apiClassDoc.setMethod(action);
    }

    /**
     * 此方法会读取Controller内部的所有方法
     * @param action controller名称 UserController -> userController
     * @param method method方法名称
     * @return
     */
    public ApiClassDoc getApiData4Depth(String action,String method) {
        ApiClassDoc apiClassDoc = new ApiClassDoc();
        if (!packages.isEmpty()) {
            baseParseApiData(apiClassDoc,
                    f->getLowName4EndWithJava(f.getName()).equals(action.toLowerCase()),
                    m->m.name().equals(method),
                    true);
        }
        return apiClassDoc.setAction(action).setMethod(method);
    }

    /**
     * 数据解析，过滤
     * @param apiClassDoc
     * @param controllerPred controller过滤条件
     * @param methodPred method过滤条件
     * @param isDepth true:内部方法全部提取 false:不做提取
     */
    private void baseParseApiData(ApiClassDoc apiClassDoc, Predicate<File> controllerPred
            , Predicate<MethodDoc> methodPred, boolean isDepth) {
        Predicate<File> pre = ObjectUtil.isNull(controllerPred) ? file -> true : controllerPred;
        packages.forEach(pk->{
            File[] files = FileUtil.ls(SRC_PAth + pk.replace(".", "/"));
            //数据过滤
            Stream.of(files).filter(f->!filters.contains(getLowName4EndWithJava(f.getName())) && new File(f.getPath()).isFile()).filter(pre)
                    .forEach(f-> {
                        //调用解析方法
                        ClassDoc[] data = JavaDocReader.show(f.getPath());
                        apiClassDoc.putClassDoc(data[0].name(), parseClassDoc(data,methodPred,isDepth));
                    });
        });
    }


    /**
     * 内部数据注释提取
     * @param data 数据
     * @param isDepth true:内部方法全部提取 false:不做提取
     * @return
     */
    private Dict parseClassDoc(ClassDoc[] data, Predicate<MethodDoc> methodPred, Boolean isDepth) {
        Predicate<MethodDoc> pre = ObjectUtil.isNull(methodPred) ? file -> true : methodPred;
        Dict dict = Dict.create();
        ClassDoc classDoc = data[0];
        dict.set("name", classDoc.name());
        dict.set("commentText", classDoc.commentText());
        dict.putAll(parseTags(classDoc.tags()));
        Map<String, Dict> methodList = Maps.newLinkedHashMap();
        //转换method
        if (classDoc.methods().length > 0) {
            //注解忽略 ApiIgnore
            Stream.of(classDoc.methods()).filter(pre).forEach(f->{
                try {
                    Class<?> ControllerClass = Class.forName(classDoc.toString());
                    ApiIgnore annotation = ControllerClass.getMethod(f.name()).getAnnotation(ApiIgnore.class);
                    if (ObjectUtil.isNotNull(annotation)) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Dict methodDict = Dict.create();
                methodDict.set("name", f.name());
                methodDict.set("commentText", f.commentText());
                methodDict.putAll(parseTags(f.tags()));
                if (isDepth) {
                    methodList.put(f.name(), methodDict);
                }
            });
        }
        dict.set("methods", methodList);
        return dict;
    }

    /**
     * 解析tages
     * @param tags
     * @return
     */
    private Dict parseTags(Tag[] tags) {
        Dict dict = Dict.create();
        Stream.of(tags).forEach(tag->{
            String key = CommUtil.splitStr4Temp(tag.name(), "@{}").get(0);
            //验证是否为多个参数，如果为多个转换为数组存储
            if (dict.keySet().contains(key)) {
                if (dict.get(key) instanceof List) {
                    List list = Convert.convert(List.class, dict.get(key));
                    list.add(tag.text());
                    dict.set(key, list);
                } else {
                    dict.set(key, Lists.newArrayList(dict.get(key), tag.text()));
                }
            } else {
                dict.set(key, tag.text());
            }
        });
        return dict;
    }

    private String getLowName4EndWithJava(String f) {
        return StrUtil.removeSuffix(f, ".java").toLowerCase();
    }

    private AnnotationParse(){}
}
