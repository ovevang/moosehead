package no.java.moosehead.eventstore;

/**
 * Created by a9791 on 22.04.2014.
 */
public interface EventListener {
    public void eventAdded(Event event);
}