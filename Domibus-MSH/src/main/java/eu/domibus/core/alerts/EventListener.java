package eu.domibus.core.alerts;

public interface EventListener<E> {

    void onEvent(E e);
}
