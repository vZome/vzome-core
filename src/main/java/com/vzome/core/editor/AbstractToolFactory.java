package com.vzome.core.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.vzome.core.editor.UndoableEdit.Context;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.monitor.ManifestationCountAggregator.ManifestationCounts;
import java.beans.PropertyChangeEvent;

public abstract class AbstractToolFactory implements ToolFactory
{
	private boolean enabled = false;
	private final PropertyChangeSupport pcs;
	private Context context;
	private EditorModel model;
	
	public AbstractToolFactory( EditorModel model, UndoableEdit.Context context )
	{
		this.model = model;
		this.context = context;
		this .pcs = new PropertyChangeSupport( this );
		model .addSelectionSummaryListener( new PropertyChangeListener()
		{
			
//			public void selectionChanged( int total, int balls, int struts, int panels )
//			{
//				boolean wasEnabled = enabled;
//				if ( countsAreValid( total, balls, struts, panels ) )
//					enabled = bindParameters( model .getSelection(), model .getSymmetrySystem() );
//				else
//					enabled = false;
//				if ( wasEnabled != enabled )
//					pcs .firePropertyChange( "enabled", wasEnabled, enabled );
//			}
//
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( evt.getPropertyName().equals("selection.counts") ) {
                    Object newValue = evt.getNewValue();
                    if(newValue instanceof ManifestationCounts ) {
                        ManifestationCounts counts = ManifestationCounts.class.cast(newValue);
                        boolean wasEnabled = enabled;
                        if ( countsAreValid( counts ) )
                            enabled = bindParameters( model .getSelection(), model .getSymmetrySystem() );
                        else
                            enabled = false;

                        if ( wasEnabled != enabled )
                            pcs .firePropertyChange( "enabled", wasEnabled, enabled );
                    }
                }
            }
        });
	}

	protected Selection getSelection()
	{
		return model .getSelection();
	}
	
	protected RealizedModel getModel()
	{
		return model .getRealizedModel();
	}
	
	@Override
	public boolean isEnabled()
	{
		return this .enabled;
	}

	public void addListener( PropertyChangeListener listener )
	{
		this .pcs .addPropertyChangeListener( listener );
	}
	
	@Override
	public Tool createTool( int index )
	{
		Tool tool = createToolInternal( index );
		if ( tool instanceof UndoableEdit )
			context .performAndRecord( (UndoableEdit) tool );
		return tool;
	}
	
	protected abstract Tool createToolInternal( int index );

	protected abstract boolean countsAreValid( ManifestationCounts counts );
	
	protected abstract boolean bindParameters( Selection selection, SymmetrySystem symmetry );
}
