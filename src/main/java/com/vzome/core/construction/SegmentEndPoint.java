

package com.vzome.core.construction;

import com.vzome.core.algebra.AlgebraicVector;


/**
 * @author Scott Vorthmann
 */
public class SegmentEndPoint extends Point
{
    private Segment mSegment;

    /**
     * @param loc
     */
    public SegmentEndPoint( Segment seg )
    {
        super( seg .field );
        mSegment = seg;
        mapParamsToState();
    }

    public void attach()
    {
        mSegment .addDerivative( this );
    }
    
    public void detach()
    {
        mSegment .removeDerivative( this );
    }
    
    public void accept( Visitor v )
    {
        v .visitSegmentEndPoint( this );
    }

    protected boolean mapParamsToState()
    {
        if ( mSegment .isImpossible() )
            return setStateVariable( null, true );
        AlgebraicVector loc = mSegment .getEnd();
        return setStateVariable( loc, false );
    }
    
}
