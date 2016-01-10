package com.vzome.core.editor;

import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.Panel;
import com.vzome.core.model.Strut;
import java.util.function.Predicate;

/**
 * @author David Hall
 */
public class Manifestations {
    
    public static boolean IsVisible(Manifestation man) {
        return !man.isHidden();
    }

    // Manifestations
    public static ManifestationIterator VisibleManifestations(Iterable<Manifestation> manifestations) {
        return new ManifestationIterator(Manifestations::IsVisible, manifestations, null);
    }
    public static ManifestationIterator VisibleManifestations(Predicate<Manifestation> preTest, Iterable<Manifestation> manifestations) {
        return new ManifestationIterator(preTest, manifestations, Manifestations::IsVisible);
    }
    public static ManifestationIterator VisibleManifestations(Iterable<Manifestation> manifestations, Predicate<Manifestation> postTest) {
        return new ManifestationIterator(Manifestations::IsVisible, manifestations, postTest);
    }
    public static class ManifestationIterator extends FilteredIterator<Manifestation, Manifestation> {
        public ManifestationIterator(Predicate<Manifestation> preTest, Iterable<Manifestation> manifestations, Predicate<Manifestation> postTest) {
            super(preTest, manifestations, postTest);
        }
        @Override
        protected Manifestation convert(Manifestation element) {
            return element;
        }
    }
    
    // Connectors        
    public static ConnectorIterator Connectors(Iterable<Manifestation> manifestations) {
        return new ConnectorIterator(null, manifestations, null);
    }
    public static ConnectorIterator Connectors(Iterable<Manifestation> manifestations, Predicate<Connector> postFilter) {
        return new ConnectorIterator(null, manifestations, postFilter);
    }
    public static ConnectorIterator Connectors(Predicate<Manifestation> preFilter, Iterable<Manifestation> manifestations, Predicate<Connector> postFilter) {
        return new ConnectorIterator(preFilter, manifestations, postFilter);
    }
    public static ConnectorIterator VisibleConnectors(Iterable<Manifestation> manifestations) {
        return VisibleConnectors(manifestations, null);
    }
    public static ConnectorIterator VisibleConnectors(Iterable<Manifestation> manifestations, Predicate<Connector> postFilter) {
        return new ConnectorIterator(Manifestations::IsVisible, manifestations, postFilter);
    }
    public static class ConnectorIterator extends SubClassIterator<Manifestation, Connector> {
        private ConnectorIterator(Predicate<Manifestation> preFilter, Iterable<Manifestation> manifestations, Predicate<Connector> postFilter) {
            super(Connector.class, preFilter, manifestations, postFilter);
        }
    }

    // Struts
    public static StrutIterator Struts(Iterable<Manifestation> manifestations) {
        return new StrutIterator(null, manifestations, null);
    }
    public static StrutIterator Struts(Iterable<Manifestation> manifestations, Predicate<Strut> postFilter) {
        return new StrutIterator(null, manifestations, postFilter);
    }
    public static StrutIterator Struts(Predicate<Manifestation> preFilter, Iterable<Manifestation> manifestations, Predicate<Strut> postFilter) {
        return new StrutIterator(preFilter, manifestations, postFilter);
    }
    public static StrutIterator VisibleStruts(Iterable<Manifestation> manifestations) {
        return VisibleStruts(manifestations, null);
    }
    public static StrutIterator VisibleStruts(Iterable<Manifestation> manifestations, Predicate<Strut> postFilter) {
        return new StrutIterator(Manifestations::IsVisible, manifestations, postFilter);
    }
    public static class StrutIterator extends SubClassIterator<Manifestation, Strut> {
        private StrutIterator(Predicate<Manifestation> preFilter, Iterable<Manifestation> manifestations, Predicate<Strut> postFilter) {
            super(Strut.class, preFilter, manifestations, postFilter);
        }
    }

    // Panels
    public static PanelIterator Panels(Iterable<Manifestation> manifestations) {
        return new PanelIterator(null, manifestations, null);
    }
    public static PanelIterator Panels(Iterable<Manifestation> manifestations, Predicate<Panel> postFilter) {
        return new PanelIterator(null, manifestations, postFilter);
    }
    public static PanelIterator Panels(Predicate<Manifestation> preFilter, Iterable<Manifestation> manifestations, Predicate<Panel> postFilter) {
        return new PanelIterator(preFilter, manifestations, postFilter);
    }
    public static PanelIterator VisiblePanels(Iterable<Manifestation> manifestations) {
        return VisiblePanels(manifestations, null);
    }
    public static PanelIterator VisiblePanels(Iterable<Manifestation> manifestations, Predicate<Panel> postFilter) {
        return new PanelIterator(Manifestations::IsVisible, manifestations, postFilter);
    }
    public static class PanelIterator extends SubClassIterator<Manifestation, Panel> {
        private PanelIterator(Predicate<Manifestation> preFilter, Iterable<Manifestation> manifestations, Predicate<Panel> postFilter) {
            super(Panel.class, preFilter, manifestations, postFilter);
        }
    }
}
