package com.vzome.core.editor;

import com.vzome.core.editor.Manifestations.ConnectorIterator;
import com.vzome.core.editor.Manifestations.PanelIterator;
import com.vzome.core.editor.Manifestations.StrutIterator;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.Panel;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;
import java.util.function.Predicate;

/**
 * @author David Hall
 */
public abstract class SelectFromRealizedModel extends ChangeSelection
{
	protected final RealizedModel mRealizedModel;

	public SelectFromRealizedModel( Selection selection, RealizedModel realizedModel, boolean groupInSelection )
    {
		super(selection, groupInSelection);
		mRealizedModel  = realizedModel;
	}
    
    protected ConnectorIterator SelectedConnectors() {
        return Manifestations.Connectors(mSelection);
    }
	
    protected StrutIterator SelectedStruts() {
        return Manifestations.Struts(mSelection);
    }
	
    protected PanelIterator SelectedPanels() {
        return Manifestations.Panels(mSelection);
    }
	
    protected ConnectorIterator RealizedConnectors() {
        return Manifestations.Connectors(mRealizedModel);
    }
	
    protected StrutIterator RealizedStruts() {
        return Manifestations.Struts(mRealizedModel);
    }
	
    protected PanelIterator RealizedPanels() {
        return Manifestations.Panels(mRealizedModel);
    }
    
    protected ConnectorIterator VisibleConnectors() {
        return Manifestations.VisibleConnectors(mRealizedModel);
    }
	
    protected StrutIterator VisibleStruts() {
        return Manifestations.VisibleStruts(mRealizedModel);
    }
	
    protected PanelIterator VisiblePanels() {
        return Manifestations.VisiblePanels(mRealizedModel);
    }

    protected ConnectorIterator VisibleConnectors(Predicate<Connector> postFilter) {
        return Manifestations.VisibleConnectors(mRealizedModel, postFilter);
    }
	
    protected StrutIterator VisibleStruts(Predicate<Strut> postFilter) {
        return Manifestations.VisibleStruts(mRealizedModel, postFilter);
    }
	
    protected PanelIterator VisiblePanels(Predicate<Panel> postFilter) {
        return Manifestations.VisiblePanels(mRealizedModel, postFilter);
    }
	
    public boolean unselectConnectors() {
        boolean anySelected = false;
        for( Connector connector : SelectedConnectors() ) {
            anySelected = true;
            this.unselect(connector);
        }
        if(anySelected) {
            redo();
        }
        return anySelected;
    }
	
    public boolean unselectAll() {
        boolean anySelected = false;
        for( Manifestation man : mSelection ) {
            anySelected = true;
            this.unselect(man);
        }
        if(anySelected) {
            redo();
        }
        return anySelected;
    }
	
    public boolean unselectStruts() {
        boolean anySelected = false;
        for( Strut strut : SelectedStruts() ) {
            anySelected = true;
            this.unselect(strut);
        }
        if(anySelected) {
            redo();
        }
        return anySelected;
    }
	
    public boolean unselectPanels() {
        boolean anySelected = false;
        for( Panel panel : SelectedPanels() ) {
            anySelected = true;
            this.unselect(panel);
        }
        if(anySelected) {
            redo();
        }
        return anySelected;
    }
	
}
