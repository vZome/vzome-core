

package com.vzome.core.construction;

import com.vzome.core.algebra.AlgebraicVector;

public class PointToPointTranslation extends Transformation
{
    private Point p1, p2;

    public PointToPointTranslation( Point p1, Point p2 )
    {
        super( p1 .field );
        mOffset = field .projectTo3d( p2 .getLocation() .minus( p1 .getLocation() ), true );
        this.p1 = p1;
        this.p2 = p2;        
    }

    public AlgebraicVector transform( AlgebraicVector arg )
    {
        return arg .plus( mOffset );
    }

    public void attach()
    {
        p1 .addDerivative( this );
        p2 .addDerivative( this );
    }
    
    public void detach()
    {
        p1 .removeDerivative( this );
        p2 .removeDerivative( this );
    }

    protected boolean mapParamsToState()
    {
//        AlgebraicField factory = (AlgebraicField) mOffset .getFactory();
        return setStateVariables( null, null, /*factory .identity(), factory .origin(),*/ false );
    }

    public void accept( Visitor v )
    {
//        v .visitTranslation( this );
    }
}
