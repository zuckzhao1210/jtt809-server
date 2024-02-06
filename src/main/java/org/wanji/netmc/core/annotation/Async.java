package org.wanji.netmc.core.annotation;

import java.lang.annotation.*;

/**
 * 异步消息处理，该注解的用户代码将运行在业务线程组(businessGroup)
 * @author yezhihao
 * <a href="https://gitee.com/yezhihao/jt808-server">...</a>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {

}