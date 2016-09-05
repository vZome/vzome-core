package com.vzome.core.editor;

import com.vzome.core.math.symmetry.IcosahedralSymmetry;
import com.vzome.core.monitor.ManifestationCountAggregator.ManifestationCounts;

public class IcosahedralToolFactory extends AbstractToolFactory implements ToolFactory
{
	private final IcosahedralSymmetry symmetry;

	public IcosahedralToolFactory( EditorModel model, UndoableEdit.Context context, IcosahedralSymmetry symmetry )
	{
		super( model, context );
		this .symmetry = symmetry;
	}

	@Override
	protected boolean countsAreValid( ManifestationCounts counts )
	{
        return counts.equalTo(1, 0, 0); // balls, struts, panels
	}

	@Override
	public Tool createToolInternal( int index )
	{
		return new SymmetryTool( "icosahedral." + index, symmetry, getSelection(), getModel(), null, null );
	}

	@Override
	protected boolean bindParameters(Selection selection, SymmetrySystem symmetry) {
		// TODO Auto-generated method stub
		return false;
	}
}