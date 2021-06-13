package com.github.freegeese.easymybatis.util;

import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 反射工具
 */
public class RefUtils {
    // 过滤出用户声明的方法
    public static final Function<Method, Boolean> USER_DECLARED_METHODS = method -> !method.isBridge() && !method.isSynthetic();
    // 过滤出可复制的字段
    public static final Function<Field, Boolean> COPYABLE_FIELDS = field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    // 空数组
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 所有声明的方法(排除: 桥接, 同步)
     *
     * @param leafClass
     * @return
     */
    public static List<Method> getAllDeclaredMethods(Class<?> leafClass) {
        final List<Method> methods = new ArrayList<>(32);
        doWithMethods(leafClass, methods::add);
        return methods;
    }

    /**
     * 所有声明的字段(排除: static, final)
     *
     * @param leafClass
     * @return
     */
    public static List<Field> getAllDeclaredFields(Class<?> leafClass) {
        final List<Field> fields = new ArrayList<>(32);
        doWithFields(leafClass, fields::add);
        return fields;
    }

    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    public static Object getFieldValue(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    private static void doWithMethods(Class<?> clazz, Consumer<Method> consumer) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!USER_DECLARED_METHODS.apply(method)) {
                continue;
            }
            consumer.accept(method);
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            doWithMethods(superclass, consumer);
        } else if (clazz.isInterface()) {
            for (Class<?> superInterface : clazz.getInterfaces()) {
                doWithMethods(superInterface, consumer);
            }
        }
    }

    private static void doWithFields(Class<?> clazz, Consumer<Field> consumer) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!COPYABLE_FIELDS.apply(field)) {
                continue;
            }
            consumer.accept(field);
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            doWithFields(superclass, consumer);
        }
    }

    /**
     * 获取第一个真实的泛型类型
     *
     * @param type
     * @return
     */
    public static Type getFirstRawGenericType(Type type) {
        List<Type> types = getRawGenericTypes(type);
        return Objects.isNull(types) || types.isEmpty() ? null : types.get(0);
    }

    /**
     * 获取真实的泛型类型
     *
     * @param type
     * @return
     */
    public static List<Type> getRawGenericTypes(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (Objects.isNull(actualTypeArguments) || actualTypeArguments.length == 0) {
            return null;
        }

        List<Type> returnTypes = new ArrayList<>();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (!(actualTypeArgument instanceof TypeVariable)) {
                returnTypes.add(actualTypeArgument);
                continue;
            }

            Type[] bounds = ((TypeVariableImpl) actualTypeArgument).getBounds();
            if (Objects.isNull(bounds) || bounds.length == 0) {
                continue;
            }
            Type bound = bounds[0];
            if (bound instanceof ParameterizedType) {
                returnTypes.add(((ParameterizedType) bound).getRawType());
                continue;
            }
            returnTypes.add(bound);
        }

        return returnTypes;
    }

    /**
     * 泛型类型：名称与类型的映射
     *
     * @param clazz
     * @return
     */
    public static Map<String, Class<?>> getGenericNameAndTypeMap(Class<?> clazz) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        Map<String, Class<?>> genericNameAndTypeMap = new HashMap<>();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                Class<?> rawType = (Class<?>) pt.getRawType();
                TypeVariable[] typeParameters = rawType.getTypeParameters();
                Type[] actualTypeArguments = pt.getActualTypeArguments();
                for (int i = 0; i < typeParameters.length; i++) {
                    TypeVariable typeParameter = typeParameters[i];
                    genericNameAndTypeMap.put(typeParameter.getName(), (Class<?>) actualTypeArguments[i]);
                }
            }
        }
        return genericNameAndTypeMap.isEmpty() ? null : genericNameAndTypeMap;
    }

    /**
     * 获取所有父类
     *
     * @param clazz
     * @return
     */
    public static List<Class<?>> getAllSuperclass(Class<?> clazz) {
        List<Class<?>> allSuperclasses = new ArrayList<>();
        Class<?> superclass;
        while (Objects.nonNull(superclass = clazz.getSuperclass())) {
            allSuperclasses.add(superclass);
        }
        return allSuperclasses.isEmpty() ? null : allSuperclasses;
    }

    /**
     * 获取所有父接口
     *
     * @param clazz
     * @return
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        List<Class<?>> allInterfaces = new ArrayList<>();
        getAllInterfaces(clazz, allInterfaces);
        return allInterfaces.isEmpty() ? null : allInterfaces;
    }

    private static void getAllInterfaces(Class<?> clazz, List<Class<?>> allInterfaces) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (Objects.isNull(interfaces) || interfaces.length == 0) {
            return;
        }
        allInterfaces.addAll(Arrays.asList(interfaces));
        Arrays.stream(interfaces).forEach(v -> getAllInterfaces(v, allInterfaces));
    }

    public static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
