package com.test.support.log.annotation;

import com.test.support.log.emun.LogType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * @author Shoven
 * @date 2019-07-25 16:17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Log(type = LogType.DELETE)
@Inherited
@Documented
public @interface DeleteLog {

    @AliasFor("message")
    String value() default "";

    @AliasFor("value")
    String message() default "";

    String module() default "";
}
