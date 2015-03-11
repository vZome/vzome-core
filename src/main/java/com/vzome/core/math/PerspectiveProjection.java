
//(c) Copyright 2011, Scott Vorthmann.

package com.vzome.core.math;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;

public class PerspectiveProjection implements Projection
{
    private final AlgebraicField field;
    
    private final AlgebraicNumber cameraDist;
    
    public PerspectiveProjection( AlgebraicField field, AlgebraicNumber cameraDist )
    {
        super();
        this .field = field;
        this .cameraDist = cameraDist;
    }
    
    private AlgebraicNumber minDenom;
    private double minDenomValue;

    public AlgebraicVector projectImage( AlgebraicVector source, boolean wFirst )
    {
        /*
         * from my WebGL vertex shader:
         * 
            float denom = cameraDist - position .w;
            denom = max( denom, 0.0001 );
            position3d .x = position .x / denom;
            position3d .y = position .y / denom;
            position3d .z = position .z / denom;
            position3d .w = 1.0;
         */
        AlgebraicVector result = this .field .origin( 4 );
        AlgebraicNumber w = source .getComponent( 0 );
        AlgebraicNumber denom = cameraDist .minus( w );
        if ( minDenom == null )
        {
            minDenom = field .createPower( -5 );
            minDenomValue = minDenom .evaluate();
        }
        double denomValue = denom .evaluate();
        if ( denomValue < minDenomValue )
        {
            denom = minDenom;
        }
        AlgebraicNumber numerator = denom .reciprocal(); // do the matrix inversion once
        
        result .setComponent( 0, field .createPower( 0 ) );
        result .setComponent( 1, source .getComponent( 1 ) .times( numerator ) );
        result .setComponent( 2, source .getComponent( 2 ) .times( numerator ) );
        result .setComponent( 3, source .getComponent( 3 ) .times( numerator ) );
        return result;
    }
}
