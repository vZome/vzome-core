package com.vzome.core.monitor;

import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.ManifestationChanges;
import com.vzome.core.model.Panel;
import com.vzome.core.model.Strut;
import com.vzome.core.render.Color;

public class ManifestationCountAggregator extends Aggregator<ManifestationCountAggregator.ManifestationCounts> implements ManifestationChanges {

    public class ManifestationCounts extends Aggregator.Aggregate {
        protected int balls = 0;
        protected int struts = 0;
        protected int panels = 0;

        protected ManifestationCounts copy()
        {
            ManifestationCounts copy = new ManifestationCounts();
            copy .balls = this.balls;
            copy .struts = this.struts;
            copy .panels = this.panels;
            return copy;
        }

        public int balls() {
            return balls;
        }

        public int struts() {
            return struts;
        }

        public int panels() {
            return panels;
        }

        public int total() {
            return balls + struts + panels;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + this.balls;
            hash = 11 * hash + this.struts;
            hash = 11 * hash + this.panels;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ManifestationCounts other = (ManifestationCounts) obj;
            if (this.balls != other.balls) {
                return false;
            }
            if (this.struts != other.struts) {
                return false;
            }
            if (this.panels != other.panels) {
                return false;
            }
            return true;
        }

        public boolean equalTo(int nBalls, int nStruts, int nPanels) {
            return ( balls == nBalls && struts == nStruts && panels == nPanels );
        }

        @Override
        public String toString() {
            return String.format("%1$d balls, %2$d struts, %3$d panels", balls, struts, panels );
        }
    }

    protected class ManifestationChangeHandler implements Runnable {
        private final Class<? extends Manifestation> manifestationClass;
        private final int delta;

        ManifestationChangeHandler(Class<? extends Manifestation> manClass, int delta) {
            this.manifestationClass = manClass;
            this.delta = delta;
        }

        @Override
        public void run() {
            // delta will be either +1 or -1 for add or remove events
            if ( manifestationClass.equals(Connector.class) )
                counts.balls += delta;
            else if ( manifestationClass.equals(Strut.class) )
                counts.struts += delta;
            else if ( manifestationClass.equals(Panel.class) )
                counts.panels += delta;
            else
                throw new IllegalStateException("Unknown class: " + manifestationClass.toString());
            notifyListeners();
        }
    }

    protected class BulkChangeHandler implements Runnable {
        private final ManifestationCounts newCounts;

        BulkChangeHandler(ManifestationCounts newValues) {
            newCounts = newValues.copy();
        }

        @Override
        public void run() {
            counts = newCounts;
            notifyListeners();
        }

    }

    private final String name;
    protected ManifestationCounts counts = new ManifestationCounts();

    /**
     * @param sourceName passed to the listeners to identify the specific item being aggregated. (e.g. "selection" or "model")
     */
    public ManifestationCountAggregator(String sourceName) {
        this.name = sourceName + ".counts"; // suffix identifies what type of aggregation this aggregator provides
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + counts.toString();
    }

    @Override
    public void reset() {
        preset(new ManifestationCounts());
    }

    @Override
    public void preset(ManifestationCounts newCounts) {
        super.notifyListeners(new BulkChangeHandler(newCounts));
    }

    @Override
    public ManifestationCounts getAggregate() {
        return counts.copy(); // so a caller can't access our instance.
    }

    @Override
    public void manifestationAdded(Manifestation m) {
        super.notifyListeners(new ManifestationChangeHandler(m.getClass(), 1));
    }

    @Override
    public void manifestationRemoved(Manifestation m) {
        super.notifyListeners(new ManifestationChangeHandler(m.getClass(), -1));
    }

    @Override
    public void manifestationColored(Manifestation m, Color color) {} // ignored
    
}
