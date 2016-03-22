package com.vzome.core.algebra;

import java.util.Arrays;
import com.vzome.core.math.RealVector;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author vorth
 *
 */
public final class AlgebraicVector implements Comparable<AlgebraicVector>
{
    public static final int X = 0, Y = 1, Z = 2;

    public static final int W4 = 0, X4 = 1, Y4 = 2, Z4 = 3;

    private final AlgebraicNumber[] coordinates;
    private final AlgebraicField field;

    public AlgebraicVector( AlgebraicNumber... n )
    {
        coordinates = new AlgebraicNumber[ n.length ];
        for ( int i = 0; i < n.length; i++ ) {
            coordinates[ i ] = n[ i ];
        }
        this .field = n[ 0 ] .getField();
    }

    public AlgebraicVector( AlgebraicField field, int dims )
    {
        coordinates = new AlgebraicNumber[ dims ];
        for ( int i = 0; i < dims; i++ ) {
            coordinates[ i ] = field .zero();
        }
        this .field = field;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result 
                + Arrays.hashCode( coordinates );
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
        AlgebraicVector other = (AlgebraicVector) obj;
        if(!field.equals( other.field )) {
            String reason  = "Invalid comparison of " 
                    + getClass().getSimpleName() + "s"
                    + "with different fields: "
                    + field.getName()
                    + " and "
                    + other.field.getName();
            throw new IllegalStateException(reason);
        }
        return Arrays.equals( coordinates, other.coordinates );
    }

    @Override
    public int compareTo(AlgebraicVector other) {
        if ( this == other ) {
            return 0;
        }
        if (other.equals(this)) {
            // intentionally throws a NullPointerException if other is null
            // or an IllegalStateException if fields are different
            return 0;
        }
        int comparison = Integer.compare(coordinates.length, other.coordinates.length);
        if(comparison != 0) {
            return comparison;
        }
        for(int i=0; i < coordinates.length; i++) {
            AlgebraicNumber n1 = this. coordinates[i];
            AlgebraicNumber n2 = other.coordinates[i];
            comparison = n1.compareTo(n2);
            if (comparison != 0) {
                return comparison;
            }
        }
        return comparison;
    }

    public final RealVector toRealVector()
    {
    	return new RealVector( this .coordinates[ 0 ] .evaluate(), this .coordinates[ 1 ] .evaluate(), this .coordinates[ 2 ] .evaluate() );
    }

    /**
     * @return A String with no extended characters so it's suitable for writing
     * to an 8 bit stream such as System.out or an ASCII text log file in Windows.
     * Contrast this with {@link toString()} which contains extended characters (e.g. \u03C6 (phi))
     */
    public final String toASCIIString()
    {
        return this .getVectorExpression( AlgebraicField .EXPRESSION_FORMAT );
    }

    /**
     * @return A String representation that can be persisted to XML and parsed by XmlSaveFormat.parseRationalVector().
     */
    public final String toParsableString()
    {
        return this .getVectorExpression( AlgebraicField .ZOMIC_FORMAT );
    }

    @Override
    public final String toString()
    {
        return this .getVectorExpression( AlgebraicField .DEFAULT_FORMAT );
    }

    public AlgebraicNumber getComponent( int i )
    {
        return this .coordinates[ i ];
    }

    public AlgebraicVector setComponent( int component, AlgebraicNumber coord )
    {
        this .coordinates[ component ] = coord;
        return this;
    }

    public AlgebraicVector negate()
    {
        AlgebraicNumber[] result = new AlgebraicNumber[ this .coordinates .length ];
        for ( int i = 0; i < result .length; i++ ) {
            result[ i ] = this .coordinates[ i ] .negate();
        }
        return new AlgebraicVector( result );
    }

    public AlgebraicVector scale( AlgebraicNumber scale )
    {
        AlgebraicNumber[] result = new AlgebraicNumber[ this .coordinates .length ];
        for ( int i = 0; i < result .length; i++ ) {
            result[ i ] = this .coordinates[ i ] .times( scale );
        }
        return new AlgebraicVector( result );
    }

    public boolean isOrigin()
    {
        for (AlgebraicNumber coordinate : this .coordinates) {
            if (!coordinate.isZero()) {
                return false;
            }
        }
        return true;
    }

    public AlgebraicVector plus( AlgebraicVector that )
    {
        AlgebraicNumber[] result = new AlgebraicNumber[ this .coordinates .length ];
        for ( int i = 0; i < result .length; i++ ) {
            result[ i ] = this .coordinates[ i ] .plus( that .coordinates[ i ] );
        }
        return new AlgebraicVector( result );
    }

    public AlgebraicVector minus( AlgebraicVector that )
    {
        AlgebraicNumber[] result = new AlgebraicNumber[ this .coordinates .length ];
        for ( int i = 0; i < result .length; i++ ) {
            result[ i ] = this .coordinates[ i ] .minus( that .coordinates[ i ] );
        }
        return new AlgebraicVector( result );
    }

    public int dimension()
    {
        return this .coordinates .length;
    }

    public AlgebraicVector cross( AlgebraicVector that )
    {
        AlgebraicNumber[] result = new AlgebraicNumber[ this .coordinates .length ];

        for ( int i = 0; i < result.length; i++ ) {
            int j = ( i + 1 ) % 3;
            int k = ( i + 2 ) % 3;
            result[ i ] = this .coordinates[ j ] .times( that .coordinates[ k ] )
                    .minus( this .coordinates[ k ] .times( that .coordinates[ j ] ) );
        }
        return new AlgebraicVector( result );
    }

    public AlgebraicVector inflateTo4d()
    {
    	return this .inflateTo4d( true );
    }

    public AlgebraicVector inflateTo4d( boolean wFirst )
    {
        if ( this .coordinates .length == 4 ) {
            if ( wFirst )
                return this;
            else
                return new AlgebraicVector( this .coordinates[ 1 ], this .coordinates[ 2 ], this .coordinates[ 3 ], this .coordinates[ 0 ] );
        }
        if ( wFirst )
            return new AlgebraicVector( this .field .zero(), this .coordinates[ 0 ], this .coordinates[ 1 ], this .coordinates[ 2 ] );
        else // the older usage
            return new AlgebraicVector( this .coordinates[ 0 ], this .coordinates[ 1 ], this .coordinates[ 2 ], this .field .zero() );
    }

    public AlgebraicVector projectTo3d( boolean wFirst )
    {
        if ( dimension() == 3 )
            return this;
        if ( wFirst )
            return new AlgebraicVector( this .coordinates[ 1 ], this .coordinates[ 2 ], this .coordinates[ 3 ] );
        else
            return new AlgebraicVector( this .coordinates[ 0 ], this .coordinates[ 1 ], this .coordinates[ 2 ] );
    }

    public void getVectorExpression( StringBuffer buf, int format )
    {
        if ( format == AlgebraicField.DEFAULT_FORMAT )
            buf .append( "(" );
        for ( int i = 0; i < this.coordinates.length; i++ ) {
            if ( i > 0 )
                if ( format == AlgebraicField.VEF_FORMAT || format == AlgebraicField.ZOMIC_FORMAT )
                    buf.append( " " );
                else
                    buf.append( ", " );
            this .coordinates[ i ] .getNumberExpression( buf, format );
        }
        if ( format == AlgebraicField.DEFAULT_FORMAT )
            buf .append( ")" );
    }

    public String getVectorExpression( int format )
    {
        StringBuffer buf = new StringBuffer();
        this .getVectorExpression( buf, format );
        return buf.toString();
    }

    public AlgebraicNumber dot( AlgebraicVector that )
    {
        AlgebraicNumber result = this .field .zero();
        for ( int i = 0; i < that.dimension(); i++ ) {
            result = result .plus( this .coordinates[ i ] .times( that .coordinates[ i ] ) );
        }
        return result;
    }

    public AlgebraicNumber getMagnitudeSquared( )
    {
        return this.dot(this);
    }

    public AlgebraicNumber getLength( AlgebraicVector unit )
    {
        for ( int i = 0; i < this.coordinates.length; i++ ) {
            if ( this .coordinates[ i ] .isZero() )
                continue;
            return this .coordinates[ i ] .dividedBy( unit .coordinates[ i ] );
        }
        throw new IllegalStateException( "vector is the origin!" );
    }
    
    public static AlgebraicVector calculateNormal(final AlgebraicVector v0, final AlgebraicVector v1, final AlgebraicVector v2) {
        return v1.minus(v0).cross(v2.minus(v0));
    }

    public static boolean areCollinear(final AlgebraicVector v0, final AlgebraicVector v1, final AlgebraicVector v2) {
        return calculateNormal(v0, v1, v2).isOrigin();
    }

    public static AlgebraicVector calculateCentroid(Collection<AlgebraicVector> vectors) {
        return getCentroid( vectors.toArray( new AlgebraicVector[ vectors.size() ] ) );
    }

    public static AlgebraicVector getCentroid(AlgebraicVector[] vectors) {
        // Assert that vectors is neither null nor empty.
        AlgebraicField field = vectors[0] .getField();
        // Start with 0 as when calculating the average of any set of numbers...
        AlgebraicVector sum = new AlgebraicVector( field, vectors[0].dimension() ); // preinitialized to 0
        for ( AlgebraicVector vector : vectors ) {
            // add them all together
            sum = sum.plus( vector );
        }
        // then divide by the number of items we added (to divide is to scale by the reciprocal)
        return sum.scale( field.createRational( 1, vectors.length ) );
    }

    /** getMostDistantFromOrigin() is is used by a few ColorMapper classes,
     * but I think it can eventually be useful elsewhere as well,
     * for example, a zoom-to-fit command or in deriving a convex hull.
     * I've made it a static method of the AlgebraicVector class to encourage such reuse.
     *
     * @param vectors A collection of vectors to be evaluated.
     * @return A canonically sorted subset (maybe all) of the {@code vectors} collection. 
     * All of the returned vectors will be the same distance from the origin.
     * That distance will be the maximum distance from the origin of any of the vectors in the original collection.
     * If the original collection contains only the origin then so will the result.
     */
    public static TreeSet<AlgebraicVector> getMostDistantFromOrigin(Collection<AlgebraicVector> vectors) {
        TreeSet<AlgebraicVector> mostDistant = new TreeSet<>();
        double maxDistanceSquared = 0D;
        for (AlgebraicVector vector : vectors) {
            double magnitudeSquared = vector.getMagnitudeSquared().evaluate();
            if (magnitudeSquared >= maxDistanceSquared) {
                if (magnitudeSquared > maxDistanceSquared) {
                    mostDistant.clear();
                }
                maxDistanceSquared = magnitudeSquared;
                mostDistant.add(vector);
            }
        }
        return mostDistant;
    }

    public AlgebraicField getField()
    {
        return this .field;
    }

}
