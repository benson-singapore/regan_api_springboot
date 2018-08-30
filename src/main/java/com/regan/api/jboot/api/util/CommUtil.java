package com.regan.api.jboot.api.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * 常用工具类
 *
 * @author zhangby
 * @date 2018/6/12 下午3:39
 */
public class CommUtil {
    /**
     * 字符串模板替换 截取
     * ClassTest::getDictList4Function,{}::{} ->[ClassTest,getDictList4Function]
     *
     * @param str
     * @param temp
     * @return
     */
    public static List<String> splitStr4Temp(String str, String temp) {
        List<String> rsList = Lists.newArrayList();
        Iterator<String> iterator = Splitter.on("{}").omitEmptyStrings().split(temp).iterator();
        while (iterator.hasNext()) {
            str = str.replace(iterator.next(), "〆");
        }
        Iterator<String> split = Splitter.on("〆").omitEmptyStrings().split(str).iterator();
        while (split.hasNext()) {
            rsList.add(split.next());
        }
        return rsList;
    }

    /**
     * 字符串分割
     *
     * @param Str  字符串
     * @param temp 分割字符串
     * @return
     */
    public static List<String> splitStr(String Str, String temp) {
        if (StrUtil.isBlank(Str)) {
            return Collections.EMPTY_LIST;
        }
        List<String> li = Lists.newArrayList();
        Iterator<String> split = Splitter.on(temp).omitEmptyStrings().trimResults().split(Str).iterator();
        while (split.hasNext()) {
            li.add(split.next());
        }
        return li;
    }

    /**
     * list数据转换
     * @param list list对象
     * @param func lamdba 表达式 function
     * @param <E> 原对象
     * @param <T> 转换完的对象
     * @return
     */
    public static <E,T> List<E> convers(Collection<T> list, Function<T, E> func) {
        if (ObjectUtil.isNull(list)) return Collections.EMPTY_LIST;
        return list.stream().collect(ArrayList::new, (li, p) -> li.add(function(p, func)), List::addAll);
    }
    /**
     * 接收T对象，返回E对象
     * @param t
     * @param func
     * @param <T>
     * @param <E>
     * @return
     */
    public static  <T,E> E function(T t,Function<T,E> func){
        return func.apply(t);
    }

    /**
     * JsonArray list -> Dict list
     * @param list
     * @return
     *
     * @author zhangby
     * @date 2018/4/26 上午11:40
     */
    public static List<Dict> jsonArray2DictList(Object list) {
        Function<Object, Dict> func = d -> JSON.parseObject(JSON.toJSONString(d), Dict.class);
        List<Object> li = Convert.convert(List.class, list);
        return convers(li,func);
    }

    /**
     * 将日期字符串转换为{@link DateTime}对象，格式：<br>
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     *
     * <li>yyyy/M/d</li>
     * <li>yyyy年M月d日</li>
     * </ol>
     *
     * @param dateStr 日期字符串
     * @return 日期
     */
    public static DateTime parseDate(String dateStr) {
        if (null == dateStr) {
            return null;
        }
        //去掉两边空格并去掉中文日期中的“日”，以规范长度
        dateStr = dateStr.trim().replace("日", "");
        int length = dateStr.length();

        if(Validator.isNumber(dateStr)) {
            //纯数字形式
            if(length == DatePattern.PURE_DATETIME_PATTERN.length()) {
                return DateUtil.parse(dateStr, DatePattern.PURE_DATETIME_FORMAT);
            } else if(length == DatePattern.PURE_DATETIME_MS_PATTERN.length()) {
                return DateUtil.parse(dateStr, DatePattern.PURE_DATETIME_MS_FORMAT);
            } else if(length == DatePattern.PURE_DATE_PATTERN.length()) {
                return DateUtil.parse(dateStr, DatePattern.PURE_DATE_FORMAT);
            } else if(length == DatePattern.PURE_TIME_PATTERN.length()) {
                return DateUtil.parse(dateStr, DatePattern.PURE_TIME_FORMAT);
            }
        }

        if (length == DatePattern.NORM_DATETIME_PATTERN.length() || length == DatePattern.NORM_DATETIME_PATTERN.length()+1) {
            return DateUtil.parseDateTime(dateStr);
        } else if (length == DatePattern.NORM_DATE_PATTERN.length()) {
            DateTime dateTime = null;
            try {
                dateTime = DateUtil.parseDate(dateStr);
            } catch (Exception e) {
                dateTime = DateUtil.parse(normalize(dateStr),"yyyyMd");
            }
            return dateTime;
        } else if (length == DatePattern.NORM_TIME_PATTERN.length() || length == DatePattern.NORM_TIME_PATTERN.length()+1) {
            return DateUtil.parseDate(dateStr);
        } else if (length == DatePattern.NORM_DATETIME_MINUTE_PATTERN.length() || length == DatePattern.NORM_DATETIME_MINUTE_PATTERN.length()+1) {
            return DateUtil.parse(normalize(dateStr), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        } else if (length >= DatePattern.NORM_DATETIME_MS_PATTERN.length() - 2) {
            return DateUtil.parse(normalize(dateStr), DatePattern.NORM_DATETIME_MS_PATTERN);
        }

        // 没有更多匹配的时间格式
        throw new DateException("No format fit for date String [{}] !", dateStr);
    }

    /**
     * 标准化日期，默认处理以空格区分的日期时间格式，空格前为日期，空格后为时间：<br>
     * 将以下字符替换为"-"
     * <pre>
     * "."
     * "/"
     * "年"
     * "月"
     * </pre>
     *
     * 将以下字符去除
     * <pre>
     * "日"
     * </pre>
     *
     * 将以下字符替换为":"
     * <pre>
     * "时"
     * "分"
     * "秒"
     * </pre>
     * 当末位是":"时去除之（不存在毫秒时）
     *
     * @param dateStr 日期时间字符串
     * @return 格式化后的日期字符串
     */
    private static String normalize(String dateStr) {
        if(StrUtil.isBlank(dateStr)) {
            return dateStr;
        }

        //日期时间分开处理
        final List<String> dateAndTime = StrUtil.splitTrim(dateStr, ' ');
        final int size = dateAndTime.size();
        if(size < 1 || size > 2) {
            //非可被标准处理的格式
            return dateStr;
        }

        final StringBuilder builder = StrUtil.builder();

        //日期部分（"\"、"/"、"."、"年"、"月"都替换为"-"）
        String datePart = dateAndTime.get(0).replaceAll("[\\/.年月]", "-");
        datePart = StrUtil.removeSuffix(datePart, "日");
        builder.append(datePart);

        //时间部分
        if(size  == 2) {
            builder.append(' ');
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", ":");
            timePart = StrUtil.removeSuffix(timePart, ":");
            builder.append(timePart);
        }

        return builder.toString();
    }
}
