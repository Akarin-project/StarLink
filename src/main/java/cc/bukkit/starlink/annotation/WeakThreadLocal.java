package cc.bukkit.starlink.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Presents it will only be accessed within its owner object, or
 * through the owner object, which is a common circumstance in object-oriented programming,
 * and will not be accessed by two threads currently.
 * 
 * In other words, a weak thread-local scenario, which shares one object but keeping safety.
 * Keep in mind, this no gurantee anything, and this is kinda a convention, also without locks,
 * to provide both safety and high performance, so use with caution.
 * 
 * This is also a markstone for multi-threading usage, to remain its non-concurrent usage.
 * The annotation value may describes the reason or special note about it.
 * 
 * @see ThreadLocal
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface WeakThreadLocal {
  String value() default "";
}
