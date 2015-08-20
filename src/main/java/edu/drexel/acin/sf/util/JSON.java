package edu.drexel.acin.sf.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/22/12
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */

public class JSON {
    private static final Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(JsonTransient.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getAnnotation(JsonTransient.class) != null;
        }
    }).create();

    public static <T> T read(Reader reader, Class<? extends T> clazz) {
        return gson.fromJson(reader, clazz);
    }

    public static <T> T read(Reader reader, Type type) {
        return gson.<T>fromJson(reader, type);
    }

    public static void write(Object o, Writer writer) {
        gson.toJson(o, writer);
    }

    public static String stringify(Object o) {
        final StringWriter writer = new StringWriter();
        JSON.write(o, writer);
        return writer.toString();
    }

    public static <T> T parse(String s, Class<? extends T> clazz) {
        final Reader reader = new StringReader(s);
        return JSON.read(reader, clazz);
    }

    public static <T> T parse(InputStream in, Class<? extends T> clazz) {
        final Reader reader = new InputStreamReader(in);
        return JSON.read(reader, clazz);
    }

    public static <T> T parse(Reader reader, Class<? extends T> clazz) {
        return JSON.read(reader, clazz);
    }

    public static <T> T parse(String s, Type type) {
        final Reader reader = new StringReader(s);
        return JSON.<T>read(reader, type);
    }

    public static <T> T parse(InputStream in, Type type) {
        final Reader reader = new InputStreamReader(in);
        return JSON.<T>read(reader, type);
    }

    public static <T> T parse(Reader reader, Type type) {
        return JSON.<T>read(reader, type);
    }
}
