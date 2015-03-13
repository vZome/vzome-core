
//(c) Copyright 2015, Scott Vorthmann.

package com.vzome.core.algebra;

import java.util.Arrays;

public class AlgebraicMatrix
{
    final AlgebraicNumber[][] matrix;

    /**
     * Create a new nXn identity matrix.
     * @param field
     * @param dim
     */
    public AlgebraicMatrix( AlgebraicField field, int dim )
    {
        this .matrix = new AlgebraicNumber[ dim ][ dim ];
        for ( int i = 0; i < dim; i++ ) {
            for ( int j = 0; j < dim; j++ ) {
                if ( i == j )
                    this .matrix[ i ][ j ] = field .one();
                else
                    this .matrix[ i ][ j ] = field .zero();
            }
        }
    }
    
    public AlgebraicMatrix( AlgebraicVector... columns )
    {
        this .matrix = new AlgebraicNumber[ columns[ 0 ] .dimension() ][ columns .length ];
        for ( int i = 0; i < columns.length; i++ ) {
            for ( int j = 0; j < columns.length; j++ ) {
                matrix[ i ][ j ] = columns[ j ] .getComponent( i ); // note the transpose,
                //  as we make row vectors rather than copying column vectors
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < this .matrix .length; i++ ) {
            buf .append( Arrays.toString( this.matrix[i] ) );
            buf .append( ", " );
        }
        return "[ " + buf.toString() + " ]";
    }

    public AlgebraicMatrix negate()
    {
        AlgebraicField field = this .matrix[ 0 ][ 0 ] .getField();
        AlgebraicMatrix result = new AlgebraicMatrix( field, this .matrix .length );

        for ( int i = 0; i < this .matrix .length; i++ ) {
            for ( int j = 0; j < this .matrix[ i ] .length; j++ ) {
                result .matrix[ i ][ j ] = this .matrix[ i ][ j ] .negate();
            }
        }
        return result;
    }

    public AlgebraicMatrix transpose()
    {
        // TODO assert matrix is square
        AlgebraicField field = this .matrix[ 0 ][ 0 ] .getField();
        AlgebraicMatrix result = new AlgebraicMatrix( field, this .matrix .length );

        for ( int i = 0; i < this .matrix .length; i++ ) {
            for ( int j = 0; j < this .matrix[ i ] .length; j++ ) {
                result .matrix[ i ][ j ] = this .matrix[ j ][ i ];
            }
        }
        return result;
    }

    // rowVector * matrix
    public AlgebraicVector timesRow( AlgebraicVector rowVector )
    {
        int colLength = rowVector .dimension();
        if ( this.matrix .length != colLength )
            throw new IllegalArgumentException( "vector length incorrect for this matrix: " + rowVector );
        int rowLength = this .matrix[ 0 ] .length;

        AlgebraicNumber[] resultComponents = new AlgebraicNumber[ rowLength ];
        AlgebraicField field = this .matrix[ 0 ][ 0 ] .getField();
        for ( int j = 0; j < rowLength; j++ ) {
            resultComponents[ j ] = field .zero();
            for ( int i = 0; i < colLength; i++ ) {
                AlgebraicNumber product = rowVector .getComponent( i ) .times( this .matrix[ i ][ j ] );
                resultComponents[ j ] = resultComponents[ j ] .plus( product );
            }
        }
        return new AlgebraicVector( resultComponents );
    }

    public AlgebraicVector timesColumn( AlgebraicVector columnVector )
    {
        int rowLength = columnVector .dimension();
        if ( this.matrix[ 0 ] .length != rowLength )
            throw new IllegalArgumentException( "vector length incorrect for this matrix: " + columnVector );
        int colLength = this .matrix .length;

        AlgebraicNumber[] resultComponents = new AlgebraicNumber[ colLength ];
        AlgebraicField field = this .matrix[ 0 ][ 0 ] .getField();
        for ( int i = 0; i < colLength; i++ ) {
            resultComponents[ i ] = field .zero();
            for ( int j = 0; j < rowLength; j++ ) {
                AlgebraicNumber product = columnVector .getComponent( j ) .times( this .matrix[ i ][ j ] );
                resultComponents[ i ] = resultComponents[ i ] .plus( product );
            }
        }
        return new AlgebraicVector( resultComponents );
    }

    public AlgebraicMatrix timesScalar( AlgebraicNumber scalar )
    {
        AlgebraicMatrix result = new AlgebraicMatrix( scalar .getField(), this .matrix .length );

        for ( int i = 0; i < this .matrix .length; i++ ) {
            for ( int j = 0; j < this .matrix[ i ] .length; j++ ) {
                result .matrix[ i ][ j ] = this .matrix[ i ][ j ] .times( scalar );
            }
        }
        return result;
    }

    public void setElement( int i, int j, AlgebraicNumber value )
    {
        this .matrix[ i ][ j ] = value;
    }

    public AlgebraicNumber getElement( int i, int j )
    {
        return this .matrix[ i ][ j ];
    }
}
