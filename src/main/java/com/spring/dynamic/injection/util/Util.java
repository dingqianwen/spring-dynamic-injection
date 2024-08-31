package com.spring.dynamic.injection.util;


import java.lang.reflect.Array;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
public class Util {

    /**
     * 是否为表达式配置
     *
     * @param value ${value} or value
     * @return true
     */
    public static boolean isExpression(String value) {
        return value.startsWith("${") && value.endsWith("}");
    }


    /**
     * 获取数组对象中指定index的值，支持负数，例如-1表示倒数第一个值<br>
     * 如果数组下标越界，返回null
     *
     * @param array 数组对象
     * @param index 下标，支持负数
     * @return 值
     */
    public static String get(String[] array, int index) {
        if (null == array) {
            return null;
        }

        if (index < 0) {
            index += Array.getLength(array);
        }
        try {
            return array[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

}
