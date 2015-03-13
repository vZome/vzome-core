

package com.vzome.core.commands;

import java.util.Map;

import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.construction.Construction;
import com.vzome.core.construction.ConstructionChanges;
import com.vzome.core.construction.ConstructionList;
import com.vzome.core.construction.ModelRoot;
import com.vzome.core.construction.Segment;
import com.vzome.core.construction.Transformation;
import com.vzome.core.construction.Translation;

/**
 * @author Scott Vorthmann
 */
public class CommandTranslate extends CommandTransform
{
    
    public ConstructionList apply( ConstructionList parameters, Map attributes, final ConstructionChanges effects ) throws Failure
    {
        final Segment norm = (Segment) attributes .get( SYMMETRY_AXIS_ATTR_NAME );
        if ( norm == null ) {
            throw new Command.Failure( "no symmetry axis provided" );
        }
        ModelRoot root = (ModelRoot) attributes .get( MODEL_ROOT_ATTR_NAME );
        final Construction[] params = parameters .getConstructions();
        AlgebraicVector offset = norm .getField() .projectTo3d( norm .getOffset(), true );
        Transformation transform = new Translation( offset, root );
        return transform( params, transform, effects );
    }
}
