package com.vzome.core.monitor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class Aggregator {

    public static abstract class Aggregate {};

    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void notifyListeners()
    {
        propertyChangeSupport.firePropertyChange(getName(), null, getAggregate());
    }

    public abstract String getName();

    public abstract Aggregate getAggregate();

    public void reset() {}
    
}
