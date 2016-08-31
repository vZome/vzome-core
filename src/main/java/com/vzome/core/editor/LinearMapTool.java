
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package com.vzome.core.editor;

import com.vzome.core.construction.ChangeOfBasis;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.Segment;
import com.vzome.core.construction.Transformation;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;

public class LinearMapTool extends TransformationTool
{
	private final boolean originalScaling;

    public LinearMapTool( String name, Selection selection, RealizedModel realized, Tool.Registry tools, Point originPoint )
    {
        this( name, selection, realized, tools, originPoint, false );
    }

    public LinearMapTool(
        String name, Selection selection, RealizedModel realized, Tool.Registry tools, Point originPoint, boolean originalScaling )
    {
        super( name, selection, realized, tools, originPoint );
        this.originalScaling = originalScaling;
    }

	// Not quite the same as overriding equals since the tool name is not compared
    // We're basically just checking if the tool's input parameters match
	public boolean hasEquivalentParameters( Object that )
	{
		if (this == that) {
			return true;
		}
		if (!super.equals(that)) {
			return false;
		}
		if (getClass() != that.getClass()) {
			return false;
		}
		LinearMapTool other = (LinearMapTool) that;
		if (originalScaling != other.originalScaling) {
			return false;
		}
		return true;
	}

    protected String checkSelection( boolean prepareTool )
    {
        {
            // This first part is called every time the selection changes,
            // so it should be optimized to fail ASAP
            // and to not do any more work than necessary to validate the selection.
            // All validation should occur before any real work is done
            final String errMsg = "linear map tool requires three adjacent, non-parallel struts (or two sets of three) and a single (optional) center ball";
            int balls = 0;
            int struts = 0;
            for (Manifestation man : mSelection) {
                if (man instanceof Connector) {
                    if((++balls) > 1)
                        return errMsg;
                } else if (man instanceof Strut) {
                    if((++struts) > 6 )
                        return errMsg;
                }
            }
            if (struts != 3 && struts != 6) {
                return errMsg;
            }
        }

        if ( prepareTool ) {
            // we know we have the right number of inputs,
            // so 're actually going to do the real work now...
            Segment[] oldBasis = new Segment[3];
            Segment[] newBasis = new Segment[3];
            Point center = null;
            int struts = 0;
            for (Manifestation man : mSelection) {
                unselect(man);
                if (man instanceof Connector) {
                    center = (Point) man.getConstructions().next();
                } else if (man instanceof Strut) {
                    Segment segment = (Segment) man.getConstructions().next();
                    if (struts < 3) {
                        oldBasis[struts] = segment;
                    } else {
                        newBasis[struts - 3] = segment;
                    }
                    ++struts;
                }
            }
            // TODO: Should we call redo() to commit all of the unselects?

        	if ( center == null )
        		center = this.originPoint;
        	this .transforms = new Transformation[ 1 ];
        	if ( struts == 6 )
        		transforms[ 0 ] = new ChangeOfBasis( oldBasis, newBasis, center );
        	else
        		transforms[ 0 ] = new ChangeOfBasis( oldBasis[ 0 ], oldBasis[ 1 ], oldBasis[ 2 ], center, originalScaling );
        }
        return null;
    }

    @Override
    protected String getXmlElementName()
    {
        return "LinearTransformTool";
    }

    @Override
    public String getCategory()
    {
        return "linear map";
    }

    @Override
    public String getDefaultName( String baseName )
    {
        return "SHOULD NEVER HAPPEN";
    }
}
