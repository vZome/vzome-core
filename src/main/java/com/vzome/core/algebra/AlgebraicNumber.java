
//(c) Copyright 2011, Scott Vorthmann.

package com.vzome.core.algebra;

import java.math.BigInteger;
import java.util.Arrays;


public class AlgebraicNumber implements Fields.Element
{
    private final AlgebraicField field;
    private final BigRational[] factors;

    AlgebraicNumber( AlgebraicField field, BigRational... factors )
    {
        if ( factors.length > field .getOrder() )
            throw new IllegalStateException( factors.length + " is too many coordinates for field \"" + field.getName() + "\"" );
        this .field = field;
        this .factors = new BigRational[ field .getOrder() ];
        for ( int i = 0; i < factors.length; i++ ) {
            this .factors[ i ] = factors[ i ];
        }
        for ( int i = factors.length; i < this.factors.length; i++ ) {
            this .factors[ i ] = BigRational.ZERO;
        }
    }

    /**
     * Extract the least common multiple of the divisors.
     * @param value
     * @return
     */
    public final BigInteger getDivisor()
    {
        BigInteger lcm = BigInteger.ONE;
        for ( int i = 0; i < this.factors.length; i++ ) {
            BigInteger aDivisor = this.factors[ i ] .getDenominator();
            lcm = lcm .multiply( aDivisor ) .abs() .divide( lcm .gcd( aDivisor ) );
        }
        return lcm;
    }
    
    public BigRational[] getFactors()
    {
        return this .factors;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( factors );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AlgebraicNumber other = (AlgebraicNumber) obj;
        if ( !Arrays.equals( factors, other.factors ) )
            return false;
        return true;
    }

    public AlgebraicField getField()
    {
        return this .field;
    }

    public AlgebraicNumber plus( AlgebraicNumber that )
    {
        int order = this .factors .length;
        BigRational[] sum = new BigRational[ order ];
        for ( int i = 0; i < order; i++ ) {
            sum[ i ] = this .factors[ i ] .plus( that .factors[ i ] );
        }
        return new AlgebraicNumber( this .field, sum );
    }

    public AlgebraicNumber times( AlgebraicNumber that )
    {
        return new AlgebraicNumber( this .field, this .field .multiply( this .factors, that .factors ) );
    }

    public AlgebraicNumber minus( AlgebraicNumber that )
    {
        return this .plus( that .negate() );
    }
    
    public AlgebraicNumber dividedBy( AlgebraicNumber that )
    {
        return this .times( that .reciprocal() );
    }

    public double evaluate()
    {
        return this .field .evaluateNumber( factors );
    }

    public boolean isZero()
    {
        for ( BigRational factor : this .factors ) {
            if ( ! factor .isZero() )
                return false;
        }
        return true;
    }

    @Override
    public boolean isOne()
    {
        if ( ! this .factors[ 0 ] .isOne() )
            return false;
        for ( int i = 1; i < this .factors.length; i++ ) {
            if ( ! this .factors[ i ] .isZero() )
                return false;
        }
        return false;
    }

    public AlgebraicNumber negate()
    {
        BigRational[] result = new BigRational[ this .factors .length ];
        for ( int i = 0; i < result.length; i++ ) {
            result[ i ] = this .factors[ i ] .negate();
        }
        return new AlgebraicNumber( this .field, result );
    }

    public AlgebraicNumber reciprocal()
    {
        return new AlgebraicNumber( this .field, this .field .reciprocal( this .factors ) );
    }

    public void getNumberExpression( StringBuffer buf, int format )
    {
        this .field .getNumberExpression( buf, this .factors, format );
    }

    public String toString( int format )
    {
        StringBuffer buf = new StringBuffer();
        this .getNumberExpression( buf, format );
        return buf .toString();
    }

    public String toString()
    {
        return this .toString( AlgebraicField .DEFAULT_FORMAT );
    }

    @Override
    public Fields.Element times( Fields.Element that )
    {
        return this .times( (AlgebraicNumber) that );
    }

    @Override
    public Fields.Element plus( Fields.Element that )
    {
        return this .plus( (AlgebraicNumber) that );
    }
}
