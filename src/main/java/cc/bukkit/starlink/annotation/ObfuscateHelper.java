package cc.bukkit.starlink.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is more an enhanced version of comment,
 * to mark the real-name of a field or method when necessary.
 * It is still recommended to create a real thing with real name,
 * when there is a need to use them in new written codes.
 * 
 * The value of this annotation suggest its deobfuscated name,
 * but may not accord with any vested specification such as <i>MCP</i>.
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface ObfuscateHelper {
  String value();
}
