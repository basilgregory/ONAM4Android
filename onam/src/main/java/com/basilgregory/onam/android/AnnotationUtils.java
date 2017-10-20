package com.basilgregory.onam.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by donpeter on 9/14/17.
 */

class AnnotationUtils {
    static void executeAnnotationFunction(Entity entity, Class annotationClass){
        if (entity == null) return ;
        L.v("About to execute life cycle event "+annotationClass.getSimpleName()
                +" for "+entity.getClass().getSimpleName());
        Method[] declaredMethods = entity.getClass().getDeclaredMethods();
        for (Method method: declaredMethods) {
            if (method.getAnnotation(annotationClass) == null) continue;
            try {
                method.invoke(entity);
                L.v("Life cycle event "+annotationClass.getSimpleName()
                        +" for "+entity.getClass().getSimpleName()+" executed");
            } catch (IllegalAccessException e) {
                L.w("Error while executing life cycle event "+entity.getClass().getSimpleName()
                        +" using method "+method.getName());
                L.e(e);
            } catch (InvocationTargetException e) {
                L.w("Error while executing life cycle event "+entity.getClass().getSimpleName()
                        +" using method "+method.getName());
                L.e(e);
            }catch (Exception e) {
                L.w("Error while executing life cycle event "+entity.getClass().getSimpleName()
                        +" using method "+method.getName());
                L.e(e);
            }
        }
    }
}
