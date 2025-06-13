package tn.portfolio.reactive.common;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReactiveEventListener {
    Class<?> value();
}
