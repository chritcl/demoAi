package com.oa.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /** 模块标题 */
    String title() default "";

    /** 业务类型 0其它 1新增 2修改 3删除 4导出 5导入 6其它 */
    int businessType() default 0;
}
