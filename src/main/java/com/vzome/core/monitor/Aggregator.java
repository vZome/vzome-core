package com.vzome.core.monitor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;

public abstract class Aggregator<A extends Aggregator.Aggregate> {

    public static abstract class Aggregate {};

    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    private class NotificationHandler implements Runnable {
        @Override
        public void run() {
            propertyChangeSupport.firePropertyChange(getName(), null, getAggregate());
        }
    }

    private final NotificationHandler notifier = new NotificationHandler();

    /**
     * To be called by external classes who need to trigger a notification, perhaps to update a newly attached listener.
     * The notifications will always be made on the EventDispatchThread
     */
    public void notifyListeners()
    {
        if (SwingUtilities.isEventDispatchThread()) {
            notifier.run();
        } else {
            SwingUtilities.invokeLater(notifier);
        }
    }

    /**
     * To be called by subclasses that need to update their data before triggering a change notification.
     * The updates and notifications will always be made on the EventDispatchThread.
     * @param handler A Runnable class which updates the data for the subclass
     */
    protected void notifyListeners(Runnable handler) {
        if (SwingUtilities.isEventDispatchThread()) {
            handler.run();
        } else {
            SwingUtilities.invokeLater(handler);
        }
        notifyListeners();
    }

    public abstract String getName();

    public abstract A getAggregate();

    public void reset() {}
    
    public abstract void preset(A newValue); // {}
    
}
