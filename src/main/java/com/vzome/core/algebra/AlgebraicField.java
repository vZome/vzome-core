//(c) Copyright 2005, Scott Vorthmann.  All rights reserved.

package com.vzome.core.algebra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.vzome.core.math.symmetry.QuaternionicSymmetry;
import com.vzome.core.math.symmetry.Symmetry;

public abstract class AlgebraicField
{
    public abstract AlgebraicNumber getDefaultStrutScaling();

    abstract BigRational[] multiply( BigRational[] v1, BigRational[] v2 );

    abstract double evaluateNumber( BigRational[] factors );

    abstract BigRational[] scaleBy( BigRational[] factors, int whichIrrational );

    public abstract void defineMultiplier( StringBuffer instances, int w );

    public abstract int getOrder();
    
    public int getNumIrrationals()
    {
        return this .getOrder() - 1;
    }
    
    public abstract String getIrrational( int i, int format );

    public String getIrrational( int which )
    {
        return this .getIrrational( which, DEFAULT_FORMAT );
    }

    private final String name;

    private final ArrayList symmetries = new ArrayList();
    
    private final Map quaternionSymmetries = new HashMap();
    
    private final AlgebraicNumber one = this .createRational( 1 );

    private final AlgebraicNumber zero = this .createRational( 0 );

    private AlgebraicField subfield = null;

    /**
     * Positive powers of the first irrational.
     */
	private ArrayList<AlgebraicNumber> positivePowers = new ArrayList<AlgebraicNumber>( 8 );

    /**
     * Negative powers of the first irrational.
     */
	private ArrayList<AlgebraicNumber> negativePowers = new ArrayList<AlgebraicNumber>( 8 );

    public AlgebraicField( String name )
    {
        this.name = name;
        this.positivePowers .add( this .one );
        this.negativePowers .add( this .one );
        AlgebraicNumber firstIrrat = this .createAlgebraicNumber( 0, 1 );
        this.positivePowers .add( firstIrrat );
        this.negativePowers .add( firstIrrat .reciprocal() );
    }

    public AlgebraicField( String name, AlgebraicField subfield )
    {
        this( name );
        this .subfield  = subfield;
    }

    public String getName()
    {
        return this.name;
    }

    public AlgebraicField getSubfield()
    {
        return subfield;
    }

    public AlgebraicNumber createAlgebraicNumber( BigRational[] factors )
    {
        return new AlgebraicNumber( this, factors );
    }
    
    public AlgebraicNumber createAlgebraicNumber( int... factors )
    {
        BigRational[] brs = new BigRational[ factors .length ];
        for ( int j = 0; j < factors.length; j++ ) {
            brs[ j ] = new BigRational( factors[ j ] );
        }
        return new AlgebraicNumber( this, brs );
    }

    public AlgebraicNumber createAlgebraicNumber( int ones, int irrat, int denominator, int power )
    {
        BigRational[] factors = new BigRational[ this .getOrder() ];
        factors[ 0 ] = new BigRational( ones, denominator );
        factors[ 1 ] = new BigRational( irrat, denominator );
        for ( int i = 2; i < factors.length; i++ ) {
            factors[ i ] = new BigRational( 0 );
        }
        if ( power != 0 ) {
            AlgebraicNumber multiplier = this .createPower( power );
            return new AlgebraicNumber( this, factors ) .times( multiplier );
        }
        else
        	return new AlgebraicNumber( this, factors );
    }

    public final AlgebraicNumber createPower( int power )
    {
    	if ( power == 0 )
    		return this .one;
    	if ( power > 0 )
    	{
    		// first, fill in the missing powers in the list
    		int size = this .positivePowers .size();
    		AlgebraicNumber irrat = this .positivePowers .get( 1 );
    		AlgebraicNumber last = this .positivePowers .get( size - 1 );
    		for (int i = size; i <= power; i++) {
    			AlgebraicNumber next = last .times( irrat );
				this .positivePowers .add( next );
				last = next;
			}
    		return positivePowers .get( power );
    	}
    	else
    	{
    		power = - power;
    		// first, fill in the missing powers in the list
    		int size = this .negativePowers .size();
    		AlgebraicNumber irrat = this .negativePowers .get( 1 );
    		AlgebraicNumber last = this .negativePowers .get( size - 1 );
    		for (int i = size; i <= power; i++) {
    			AlgebraicNumber next = last .times( irrat );
				this .negativePowers .add( next );
				last = next;
			}
    		return negativePowers .get( power );
    	}
    }

    public final AlgebraicNumber createRational( int... value )
    {
    	int denom = value.length == 2 ? value[ 1 ] : 1;
        return createAlgebraicNumber( value[0], 0, denom, 0 );
    }

	public BigRational[] negate( BigRational[] e )
	{
    	BigRational[] result = new BigRational[ e.length ];
    	for (int i = 0; i < e.length; i++) {
			result[ i ] = e[ i ] .negate();
		}
    	return result;
	}

	public boolean isZero( BigRational[] e )
	{
    	for ( int i = 0; i < e.length; i++ )
    		if ( ! e[ i ] .isZero() )
    			return false;
		return true;
	}

    public BigRational[] add( BigRational[] v1, BigRational[] v2 )
    {
    	if ( v1.length != v2.length )
    		throw new IllegalArgumentException( "arguments don't match" );
    	BigRational[] result = new BigRational[ v1.length ];
    	for (int i = 0; i < result.length; i++) {
			result[ i ] = v1[ i ] .plus( v2[ i ] );
		}
    	return result;
    }

    public BigRational[] subtract( BigRational[] v1, BigRational[] v2 )
    {
    	if ( v1.length != v2.length )
    		throw new IllegalArgumentException( "arguments don't match" );
    	BigRational[] result = new BigRational[ v1.length ];
    	for (int i = 0; i < result.length; i++) {
			result[ i ] = v1[ i ] .minus( v2[ i ] );
		}
    	return result;
    }

    public void addSymmetry( Symmetry symmetry )
    {
        this.symmetries.add( symmetry );
    }
    
    public Symmetry getSymmetry( String name )
    {
        for ( Iterator iterator = symmetries.iterator(); iterator.hasNext(); ) {
            Symmetry symm = (Symmetry) iterator.next();
            if ( symm .getName() .equals( name ) )
                return symm;
        }
        return null;
    }

    public Symmetry[] getSymmetries()
    {
        return (Symmetry[]) symmetries.toArray( new Symmetry[0] );
    }
    
    public void addQuaternionSymmetry( QuaternionicSymmetry symm )
    {
    	quaternionSymmetries .put( symm .getName(), symm );
    }

    public QuaternionicSymmetry getQuaternionSymmetry( String name )
    {
        return (QuaternionicSymmetry) quaternionSymmetries .get( name );
    }

    /**
     * Drop one coordinate from the 4D vector. If wFirst (the usual), then drop
     * the first coordinate, taking the "imaginary part" of the vector. If
     * !wFirst (for old VEF import, etc.), drop the last coordinate.
     * 
     * @param source
     * @param wFirst
     * @return
     */
    public final AlgebraicVector projectTo3d( AlgebraicVector source, boolean wFirst )
    {
        if ( source .dimension() == 3 )
            return source;
        else {
            AlgebraicVector result = this .origin( 3 );
            for ( int i = 0; i < 3; i++ )
                result .setComponent( i, source .getComponent( wFirst? i+1 : i ) );
            return result;
        }
    }

    public final AlgebraicVector origin( int dims )
    {
        return new AlgebraicVector( this, dims );
    }

    public final AlgebraicVector basisVector( int dims, int axis )
    {
        AlgebraicVector result = origin( dims );
        result .setComponent( axis, this .one() );
        return result;
    }

    // ======================================================================================
    // number operations
    // ======================================================================================


    protected BigRational[] reciprocal( BigRational[] fieldElement )
    {
        int order = fieldElement .length;
        BigRational[][] representation = new BigRational[ order ][ order ];
        for ( int i = 0; i < order; i++ ) {
            representation[ 0 ][ i ] = fieldElement[ i ];
        }
        for ( int j = 1; j < order; j++ ) {
            BigRational[] column = this .scaleBy( fieldElement, j );
            for ( int i = 0; i < order; i++ ) {
                representation[ j ][ i ] = column[ i ];
            }
        }
        BigRational[][] reciprocal = new BigRational[ order ][ order ];
        // create an identity matrix
        for ( int j = 0; j < order; j++ ) {
            for ( int i = 0; i < order; i++ ) {
                if ( i == j )
                    reciprocal[ j ][ i ] = new BigRational( 1 );
                else
                    reciprocal[ j ][ i ] = new BigRational( 0 );
            }
        }
        Fields .gaussJordanReduction( representation, reciprocal );
        BigRational[] reciprocalFactors = new BigRational[ order ];
        for ( int i = 0; i < order; i++ ) {
            reciprocalFactors[ i ] = reciprocal[ 0 ][ i ];
        }
        return reciprocalFactors;
    }
        
    public final static int DEFAULT_FORMAT = 0; // 4 + 3 \u03C4

    public final static int EXPRESSION_FORMAT = 1; // 4+\u03C4*3

    public final static int ZOMIC_FORMAT = 2; // 4 3

    public final static int VEF_FORMAT = 3; // (3,4)

    public AlgebraicNumber zero()
    {
        return this .zero;
    }

    public AlgebraicNumber one()
    {
        return this .one;
    }

    public AlgebraicVector createVector( int[] is )
    {
        int order = this .getOrder();
        if ( is.length % order != 0 )
            throw new IllegalStateException( "Field order (" + order + ") does not divide length for " + is );
        
        int dims = is.length / ( 2 * order );
        AlgebraicNumber[] coords = new AlgebraicNumber[ dims ];
        for ( int i = 0; i < dims; i++ ) {
            BigRational[] factors = new BigRational[ order ];
            for ( int j = 0; j < order; j++ ) {
                int numeratorIndex = 2 * ( i * order + j );
                factors[ j ] = new BigRational( is[ numeratorIndex ], is[ numeratorIndex+1 ] );
            }
            coords[ i ] = new AlgebraicNumber( this, factors );
        }
        return new AlgebraicVector( coords );
    }

    void getNumberExpression( StringBuffer buf, BigRational[] factors, int format )
    {
        switch ( format )
        {
        case ZOMIC_FORMAT:
            for ( int i = 0; i < factors.length; i++ )
            {
                if ( i > 0 )
                    buf.append( " " );
                buf .append( factors[ i ] .toString() );
            }
            break;

        case VEF_FORMAT:
            buf.append( "(" );
            for ( int i = factors.length; i > 0; i-- )
            { // note that we go backwards!
                buf .append( factors[ i - 1 ] .toString() );
                if ( i == 1 )
                    buf.append( "," );
            }
            buf.append( ")" );
            break;

        default:
            for ( int i = 0; i < factors.length; i++ )
            {
                String multiplier = this .getIrrational( i, format );
                if ( i > 0 )
                    buf .append( " + " );
                if ( i > 0 && format == EXPRESSION_FORMAT )
                    buf .append( multiplier + "*" );
                buf .append( factors[ i ] .toString() );
                if ( i > 0 && format == DEFAULT_FORMAT )
                    buf .append( " " + multiplier );
            }
            break;
        }
    }

    public AlgebraicNumber parseLegacyNumber( String val )
    {
        throw new IllegalStateException( "This field does not support vZome 2.x files." );
    }

    public AlgebraicNumber parseNumber( String nums )
    {
        StringTokenizer tokens = new StringTokenizer( nums, " " );
        return this .parseNumber( tokens );
    }

    /**
     * Consumes this.getOrder() tokens from the tokenizer
     * @param tokens
     * @return
     */
    private AlgebraicNumber parseNumber( StringTokenizer tokens )
    {
        BigRational[] rats = new BigRational[ this .getOrder() ];
        for ( int i = 0; i < rats.length; i++ ) {
            rats[ i ] = new BigRational( tokens .nextToken() );
        }
        return new AlgebraicNumber( this, rats );
    }
    
    public AlgebraicVector parseVector( String nums )
    {
        StringTokenizer tokens = new StringTokenizer( nums, " " );
        int numToks = tokens .countTokens();
        int order = this .getOrder();
        if ( numToks % order != 0 )
            throw new IllegalStateException( "Field order (" + order + ") does not divide token count: " + numToks + ", for '" + nums + "'" );
        
        int dims = numToks / order;
        AlgebraicNumber[] coords = new AlgebraicNumber[ dims ];
        for ( int i = 0; i < dims; i++ ) {
            coords[ i ] = this .parseNumber( tokens );
        }
        return new AlgebraicVector( coords );
    }

    public AlgebraicMatrix identityMatrix( int dims )
    {
        AlgebraicVector[] columns = new AlgebraicVector[ dims ];
        for ( int i = 0; i < columns.length; i++ ) {
            columns[ i ] = this .basisVector( dims, i );
        }
        return new AlgebraicMatrix( columns );
    }    
}
