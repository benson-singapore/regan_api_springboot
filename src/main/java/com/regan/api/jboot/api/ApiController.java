package com.regan.api.jboot.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.regan.api.jboot.api.util.ApiClassDoc;
import com.regan.api.jboot.api.util.CommUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yaml.snakeyaml.Yaml;
import com.regan.api.jboot.api.entity.ApiConfig;
import com.regan.api.jboot.api.util.AnnotationParse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Struct;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 生成API调用接口
 *
 * @author zhangby
 * @date 2018/5/17 下午1:24
 */
@Controller
public class ApiController{

    /** 生成解析实例 */
    private AnnotationParse annotationParse = AnnotationParse.getInstance(readApiConfig().getPackages()).filter(readApiConfig().getFilters());
    /** 主题 */


    /**
     * hello
     *
     * @return
     */
    @RequestMapping("/api")
    public String index(){
        return "api/index";
    }

    /**
     * 获取主题色，暂时只支持：dark、light
     *
     * @author zhangby
     * @date 2018/6/21 下午2:59
     */
    @RequestMapping("/api/getTheme")
    @ResponseBody
    public String getTheme() {
       return JSON.toJSONString(Dict.create().set("theme",readApiConfig().getTheme()));
    }

    /**
     * 获取菜单数据
     *
     * @author zhangby
     * @date 2018/5/19 下午1:55
     */
    @RequestMapping("/api/getMenuData")
    @ResponseBody
    public String getMenuData() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        // => 生成返回数据
        List<Dict> menuList = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Dict kv = Dict.create()
                    .set("menuName", StrUtil.isBlank(dict.getStr("commentText")) ? dict.get("action") : dict.get("commentText"))
                    .set("href", dict.get("action"))
                    .set("actionName", dict.get("name"));
            List<Dict> children = Lists.newArrayList();
            //添加方法
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                Dict child = Dict.create()
                        .set("menuName", StrUtil.isBlank(dt.getStr("title")) ? dt.get("name") : dt.getStr("title"))
                        .set("href", dt.get("name"))
                        .set("methodName", dt.get("name"))
                        .set("author", dt.get("author"));
                children.add(child);
            });
            kv.set("children", children);
            menuList.add(kv);
        });
        return JSON.toJSONString(menuList);
    }

    /**
     * 首页统计
     *
     * @author zhangby
     * @date 2018/6/12 下午4:55
     */
    @RequestMapping("/api/getApiHome")
    @ResponseBody
    public String getApiHome() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<String> actionList = Lists.newArrayList();
        List<String> methodList = Lists.newArrayList();
        Set<String> authorList = Sets.newHashSet();
        dataMap.values().forEach(d -> {
            actionList.add(d.getStr("name"));
            Convert.convert(Map.class, d.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                methodList.add(dt.getStr("name"));
                authorList.add(dt.getStr("author"));
            });
        });
        Dict dict = Dict.create()
                .set("actionNum", actionList.size())
                .set("methodNum", methodList.size())
                .set("authorNum", authorList.size())
                .set("authorList", authorList);
        return JSON.toJSONString(dict);
    }

    /**
     * 接口贡献度查询
     *
     * @author zhangby
     * @date 2018/6/19 下午6:53
     */
    @RequestMapping("/api/getCalendarData")
    @ResponseBody
    public String getCalendarData() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Dict> methodAll = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                if (StrUtil.isNotBlank(dt.getStr("date"))) {
                    String api_date = CommUtil.splitStr(dt.getStr("date"), " ").get(0);
                    methodAll.add(Dict.create()
                            .set("date", DateUtil.formatDate(CommUtil.parseDate(api_date))));
                }
            });
        });
        Map<String, Long> date = methodAll.stream()
                .collect(Collectors.groupingBy(dt -> dt.getStr("date"), Collectors.counting()));
        List<List<String>> rsList = Lists.newArrayList();
        date.forEach((k, v) -> rsList.add(Lists.newArrayList(k, v.toString())));
        return JSON.toJSONString(
                Dict.create()
                        .set("date", rsList)
                        .set("year", Lists.newArrayList(DateUtil.formatDate(new Date()), DateUtil.formatDate(DateUtil.offsetDay(new Date(), -365))))
        );
    }

    /**
     * 周更新
     *
     * @author zhangby
     * @date 2018/6/20 下午4:25
     */
    @RequestMapping("/api/getWeekGroup")
    @ResponseBody
    public String getWeekGroup() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Dict> methodAll = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                if (StrUtil.isNotBlank(dt.getStr("date")) || StrUtil.isNotBlank(dt.getStr("author"))) {
                    String api_date = CommUtil.splitStr(dt.getStr("date"), " ").get(0);
                    methodAll.add(Dict.create()
                            .set("date", DateUtil.formatDate(CommUtil.parseDate(api_date))));
                }
            });
        });
        Dict weekDate = Dict.create();
        for (int i = 6; i >= 0; i--) {
            weekDate.put(DateUtil.formatDate(DateUtil.offsetDay(new Date(), -i)), 0);
        }
        methodAll.stream()
                .filter(kv ->
                        kv.getStr("date").compareTo(DateUtil.formatDate(new Date())) <= 0 &&
                                kv.getStr("date").compareTo(DateUtil.formatDate(DateUtil.offsetWeek(new Date(), -1))) >= 0)
                .collect(Collectors.groupingBy(kv -> kv.getStr("date"), Collectors.counting()))
                .forEach((k, v) -> weekDate.put(k, v));
        Dict weekGroup = Dict.create()
                .set("xAxis", weekDate.keySet())
                .set("series", weekDate.values());

        return JSON.toJSONString(weekGroup);
    }

    /**
     * 模块分组
     */
    @RequestMapping("/api/getModelGroup")
    @ResponseBody
    public String getModelGroup() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Dict> modelGroup = CommUtil.convers(dataMap.values(), dict ->
                Dict.create()
                        .set("name", StrUtil.isBlank(dict.getStr("commentText")) ? dict.get("action") : dict.get("commentText"))
                        .set("value", Convert.convert(Map.class, dict.get("methods")).size()));
        return JSON.toJSONString(modelGroup);
    }


    /**
     * 获取接口分组数据 [作者统计]
     *
     * @author zhangby
     * @date 2018/6/20 上午11:49
     */
    @RequestMapping("/api/getAuthorGroup")
    @ResponseBody
    public String getAuthorGroup() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Dict> methodAll = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                if (StrUtil.isNotBlank(dt.getStr("date")) || StrUtil.isNotBlank(dt.getStr("author"))) {
                    String api_date = CommUtil.splitStr(dt.getStr("date"), " ").get(0);
                    methodAll.add(Dict.create()
                            .set("author", dt.get("author")));
                }
            });
        });

        /** 作者分组 */
        List<Dict> list = Lists.newArrayList();
        methodAll.stream()
                .collect(Collectors.groupingBy(kv -> kv.getStr("author"), Collectors.counting()))
                .forEach((k, v) -> list.add(Dict.create().set("author", k).set("num", v)));
        //排序
        List<Dict> authorGroup = list.stream().sorted((k1, k2) -> -k1.getStr("num").compareTo(k2.getStr("num"))).collect(Collectors.toList());
        //添加头像
        List<String> headImgs = Lists.newArrayList(
                "https://img.alicdn.com/tfs/TB1j159r21TBuNjy0FjXXajyXXa-499-498.png_80x80.jpg",
                "https://img.alicdn.com/tfs/TB1FGimr1SSBuNjy0FlXXbBpVXa-499-498.png_80x80.jpg",
                "https://img.alicdn.com/tfs/TB1AdOerVOWBuNjy0FiXXXFxVXa-499-498.png_80x80.jpg");
        for (int i=0;i<authorGroup.size();i++) {
            authorGroup.get(i).set("headImg", headImgs.get(i % 3));
        }
        return JSON.toJSONString(authorGroup);
    }

    /**
     * 获取API名称
     *
     * @author zhangby
     * @date 2018/5/19 下午1:56
     */
    @RequestMapping("/api/getMethodTitle")
    @ResponseBody
    public String getMethodTitle(String actionName,String method) {
        Dict methodTitle = Dict.create();
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth(actionName, method).getApiDataMap();
        System.out.println(dataMap);
        if (!dataMap.isEmpty()) {
            Dict actionDict = dataMap.get(actionName);
            Dict methodDict = Convert.convert(Dict.class, Convert.convert(Map.class, actionDict.get("methods")).get(method));
            methodTitle.set("title", StrUtil.isBlank(methodDict.getStr("title")) ? methodDict.get("name") : methodDict.getStr("title"))
                    .set("actionTitle", StrUtil.isBlank(actionDict.getStr("commentText")) ? actionDict.get("action") : actionDict.get("commentText"))
                    .set("desc", methodDict.get("commentText"))
                    .set("url", ("/".equals(actionDict.get("action")) ? "" : actionDict.get("action")) + "/" + methodDict.get("name"))
                    .set("respText", ObjectUtil.isNull(methodDict.get("respBody")) ? "{}" : methodDict.get("respBody"))
                    .set("author", methodDict.get("author"))
                    .set("requestType", StrUtil.isNotBlank(methodDict.getStr("requestType")) ?
                            methodDict.getStr("requestType").split(",") : new String[]{"post"});
        }
        System.out.println(JSON.toJSONString(methodTitle));
        return JSON.toJSONString(methodTitle);
    }

    /**
     * 获取参数数据
     * action controller 名称
     * method 方法名称
     * paramType 参数类型：1 入参，2 出参
     *
     * @author zhangby
     * @date 2018/5/19 下午10:54
     */
    @RequestMapping("/api/getReqParam")
    @ResponseBody
    public String getReqParam(String actionName,String method,String paramType) {
        String json = "";
        Dict dataMap = annotationParse.getApiData4Depth(actionName, method).getMethodInfo();
        if (Objects.equals("1", paramType)) {
            List<String> param = Convert.convert(List.class, dataMap.get("param") instanceof String ? Lists.newArrayList(dataMap.get("param")) : dataMap.get("param"));
            json = parseApiTableJson(param, (l, p) -> {
                List<String> splitStr4Temp = CommUtil.splitStr4Temp(p, "{}|");
                List<String> keys = CommUtil.splitStr(splitStr4Temp.get(0), ".");
                l.put(splitStr4Temp.get(0),
                        Dict.create()
                                .set("name", keys.get(keys.size() - 1))
                                .set("desc", splitStr4Temp.get(1))
                                .set("type", splitStr4Temp.get(2))
                                .set("required", splitStr4Temp.get(3))
                );
            });
        } else {
            List<String> param = Convert.convert(List.class, dataMap.get("resqParam") instanceof String ? Lists.newArrayList(dataMap.get("resqParam")) : dataMap.get("resqParam"));
            json = parseApiTableJson(param, (l, p) -> {
                List<String> splitStr4Temp = CommUtil.splitStr4Temp(p, "{}|");
                List<String> keys = CommUtil.splitStr(splitStr4Temp.get(0), ".");
                l.put(splitStr4Temp.get(0),
                        Dict.create()
                                .set("name", keys.get(keys.size() - 1))
                                .set("desc", splitStr4Temp.get(1))
                                .set("type", splitStr4Temp.get(2))
                                .set("required", splitStr4Temp.size() > 3 ? splitStr4Temp.get(3) : "")
                );
            });
        }
        return json;
    }

    /**
     * 获取数据请求数据
     *
     * @author zhangby
     * @date 2018/5/20 下午12:24
     */
    @RequestMapping("/api/getPostManData")
    @ResponseBody
    public String getPostManData(String actionName,String method) {
        //调用解析方法
        ApiClassDoc apiData4Depth = annotationParse.getApiData4Depth(actionName, method);
        Dict controllerInfo = apiData4Depth.getControllerInfo();
        Dict methodInfo = apiData4Depth.getMethodInfo();
        List<String> reqParams = Convert.convert(List.class, methodInfo.get("param") instanceof String ? Lists.newArrayList(methodInfo.get("param")) : methodInfo.get("param"));
        Dict kv = Dict.create()
                .set("url", ("/".equals(controllerInfo.getStr("action")) ? "" : controllerInfo.getStr("action")) + "/" + methodInfo.getStr("name"))
                .set("requestType", CommUtil.convers(CommUtil.splitStr(methodInfo.getStr("requestType"), ","),
                        type -> Dict.create().set("text", type.toUpperCase()).set("value", type)));
        String json = parseApiTableJson(reqParams, (l, p) -> {
            List<String> splitStr4Temp = CommUtil.splitStr4Temp(p, "{}|");
            List<String> keys = CommUtil.splitStr(splitStr4Temp.get(0), ".");
            l.put(splitStr4Temp.get(0),
                    Dict.create().set("name", keys.get(keys.size() - 1))
            );
        });
        Dict dict = recursive(JSON.parseArray(json, Dict.class));
        List<String> filterList = filterList(dict, "");
        List<Dict> reqParamsList = CommUtil.convers(reqParams,
                param -> {
                    List<String> params = CommUtil.splitStr(Convert.convert(String.class, param), "|");
                    return Dict.create()
                            .set("id", params.get(0)).set("key", params.get(0)).set("desc", params.get(1));
                }).stream().filter(p -> filterList.contains(p.getStr("key"))).collect(Collectors.toList());
        kv.set("jsonParams", dict)
                .set("reqParams", reqParamsList)
                .set("selectedRowKeys", CommUtil.convers(reqParamsList, param -> param.getStr("id")));
        return JSON.toJSONString(kv);
    }

    private Dict recursive(List<Dict> list) {
        Dict dict = Dict.create();
        if (list.isEmpty()) {
            return dict;
        } else {
            list.forEach(d -> {
                Object children = d.get("children");
                dict.set(d.getStr("name"), ObjectUtil.isNotNull(children) ? recursive(CommUtil.jsonArray2DictList(children)) : "");
            });
        }
        return dict;
    }

    private List<String> filterList(Dict dict, String pre) {
        List<String> list = Lists.newArrayList();
        if (dict.isEmpty()) {
            return list;
        } else {
            dict.forEach((k, v) -> {
                if (ObjectUtil.isNotNull(v) && StrUtil.isNotBlank(v.toString())) {
                    list.addAll(filterList(Convert.convert(Dict.class, v), StrUtil.isBlank(pre) ? k : pre + "." + k));
                } else {
                    list.add(StrUtil.isBlank(pre) ? k : pre + "." + k);
                }
            });
        }
        return list;
    }

    /**
     * 解析表格数据json
     *
     * @return
     */
    private String parseApiTableJson(List<String> paramList, BiConsumer<Dict, String> accumulator) {
        if (ObjectUtil.isNull(paramList) || paramList.isEmpty()) {
            return JSON.toJSONString(Lists.newArrayList());
        }
        List<String> sortParamList = paramList.stream().map(p -> p.split("\\|")[0]).sorted().collect(Collectors.toList());
        Dict okv = paramList.stream().collect(Dict::create, accumulator, Dict::putAll);
        List<Dict> list = Lists.newArrayList();
        sortParamList.forEach(p -> {
            List<String> splitKey = CommUtil.splitStr4Temp(Convert.toStr(p), ".");
            String remove = splitKey.remove(splitKey.size() - 1);
            if (!splitKey.isEmpty()) {
                Dict kv = getLastKv(list, splitKey);
                if (ObjectUtil.isNotNull(kv)) {
                    if (ObjectUtil.isNotNull(kv) && ObjectUtil.isNull(kv.get("children"))) {
                        kv.set("children", Lists.newArrayList(okv.get(p)));
                    } else {
                        List children = Convert.convert(List.class, kv.get("children"));
                        children.add(okv.get(p));
                        kv.set("children", children);
                    }
                }
            } else {
                list.add(Convert.convert(Dict.class, okv.get(p)));
            }
        });
        return JSON.toJSONString(list);
    }

    private static Dict getLastKv(List<Dict> list, List<String> keys) {
        List<Dict> rsList = list;
        Dict kv = Dict.create();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                kv = getKvByName(rsList, key);
                if (ObjectUtil.isNotNull(kv) && ObjectUtil.isNotNull(kv.get("children"))) {
                    rsList = Convert.convert(List.class, kv.get("children"));
                }
            }
        }
        return kv;
    }

    private static Dict getKvByName(List<Dict> list, String name) {
        Dict kv = null;
        try {
            kv = list.stream().filter(p -> p.get("name").equals(name)).findFirst().get();
        } catch (Exception e) {
        }
        return kv;
    }

    /**
     * 读取配置信息
     */
    public static ApiConfig readApiConfig(){
        ApiConfig apiConfig = new ApiConfig();
        Yaml yaml = new Yaml();
        URL url = ApiController.class.getClassLoader().getResource("application.yml");
        if (url != null) {
            try {
                //获取test.yaml文件中的配置数据，然后转换为obj，
                Object obj = yaml.load(new FileInputStream(url.getFile()));
                Map map = Convert.convert(Map.class, obj);
                Map apiMap = Convert.convert(Map.class, map.get("api"));
                apiConfig = JSON.parseObject(JSON.toJSONString(apiMap), ApiConfig.class);
                System.out.println(apiConfig.getPackages());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return apiConfig;
    }
}
