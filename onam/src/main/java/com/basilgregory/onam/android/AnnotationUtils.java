package com.basilgregory.onam.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by donpeter on 9/14/17.
 */

public class AnnotationUtils {
    static void executeAnnotationFunction(Entity entity, Class annotationClass){
        if (entity == null) return ;
        Method[] declaredMethods = entity.getClass().getDeclaredMethods();
        for (Method method: declaredMethods) {
            if (method.getAnnotation(annotationClass) == null) continue;
            try {
                method.invoke(entity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
