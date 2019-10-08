package org.noear.snack.core.utils;

import org.noear.snack.core.exts.FieldWrap;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工具类
 * */
public class BeanUtil {

    public static final Map<String,Class<?>> clzCached = new ConcurrentHashMap<>();
    public static Class<?> loadClass(String clzName){
        try {
            Class<?> clz = clzCached.get(clzName);
            if(clz == null) {
                clz = Class.forName(clzName);
                clzCached.put(clzName,clz);
            }

            return clz;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /////////////////

    /**  */
    private static transient final Map<String,List<FieldWrap>> fieldsCached = new ConcurrentHashMap<>();

    /** 获取一个类的所有字段 （已实现缓存） */
    public static List<FieldWrap> getAllFields(Class<?> clz){
        String key = clz.getName();

        List<FieldWrap> list = fieldsCached.get(key);
        if(list == null){
            list = new ArrayList<>();
            scanAllFields(clz,list);
            fieldsCached.put(key, list);
        }

        return list;
    }

    /** 扫描一个类的所有字段 */
    private static void scanAllFields(Class<?> clz, List<FieldWrap> fields) {
        for (Field f : clz.getDeclaredFields()) {
            int mod = f.getModifiers();

            if (!Modifier.isTransient(mod) && !Modifier.isStatic(mod)) {
                f.setAccessible(true);
                fields.add(new FieldWrap(f));
            }
        }

        Class<?> sup = clz.getSuperclass();
        if (sup != Object.class) {
            scanAllFields(sup, fields);
        }
    }

    /** 将 Clob 转为 String */
    public static String clobToString(Clob clob) {

        Reader reader = null;
        StringBuilder buf = new StringBuilder();

        try {
            reader = clob.getCharacterStream();

            char[] chars = new char[2048];
            for (; ; ) {
                int len = reader.read(chars, 0, chars.length);
                if (len < 0) {
                    break;
                }
                buf.append(chars, 0, len);
            }
        } catch (Exception ex) {
            throw new RuntimeException("read string from reader error", ex);
        }

        String text = buf.toString();

        if (reader != null) {
            try {
                reader.close();
            }catch (Exception ex){
                throw new RuntimeException("read string from reader error", ex);
            }
        }

        return text;
    }
}