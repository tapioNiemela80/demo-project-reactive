package tn.portfolio.reactive.common;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

@Component
public class ReactiveEventListenerRegistrar implements InitializingBean {

    private final ReactiveDomainEventPublisher publisher;
    private final List<Object> allBeans;

    public ReactiveEventListenerRegistrar(
            ReactiveDomainEventPublisher publisher,
            List<Object> allBeans // Spring passes all beans here!
    ) {
        this.publisher = publisher;
        this.allBeans = allBeans;
    }

    @Override
    public void afterPropertiesSet() {
        for (Object bean : allBeans) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                var annotation = method.getAnnotation(ReactiveEventListener.class);
                if (annotation != null) {
                    Class<?> eventType = annotation.value();
                    method.setAccessible(true);
                    publisher.onEvent(eventType)
                            .flatMap(event -> {
                                try {
                                    Object result = method.invoke(bean, event);
                                    if (result instanceof Mono<?> mono) {
                                        return mono;
                                    } else {
                                        return Mono.error(new IllegalStateException("Handler must return Mono"));
                                    }
                                } catch (Exception e) {
                                    return Mono.error(e);
                                }
                            })
                            .subscribe();
                }
            }
        }
    }
}