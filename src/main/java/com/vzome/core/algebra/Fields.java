
//(c) Copyright 2011, Scott Vorthmann.

package com.vzome.core.algebra;

public class Fields
{
    public interface Element
    {
        Element times( Element that );

        Element plus( Element that );

        Element reciprocal();

        Element negate();

        boolean isZero();

        boolean isOne();
    }
    
    public static final void gaussJordanReduction( Element[][] matrix, Element[][] adjoined )
    {
        for ( int upleft = 0; upleft < matrix.length; upleft++ )
        {
            int pivot = -1;
            // find pivot: skip columns all zero, find highest non-zero row
            for ( int j = upleft; j < matrix[0].length; j++ ) {
                for ( int i = upleft; i < matrix.length; i++ ) {
                    if ( ! matrix[ i ][ j ] .isZero() ) {
                        pivot = i;
                        break;
                    }
                }
                if ( pivot >= 0 ) break;
            }
            if ( pivot < 0 )
                return; // all done, in ReREF

            // exchange pivot and top rows
            if ( pivot != upleft ) {
                for ( int j = upleft; j < matrix[0].length; j++ ) {
                    Element temp = matrix[ upleft ][ j ];
                    matrix[ upleft ][ j ] = matrix[ pivot ][ j ];
                    matrix[ pivot ][ j ] = temp;
                }
                for ( int j = 0; j < adjoined[0].length; j++ ) {
                	Element temp = adjoined[ upleft ][ j ];
                	adjoined[ upleft ][ j ] = adjoined[ pivot ][ j ];
                	adjoined[ pivot ][ j ] = temp;
                }
            }

            // normalize the top row
            if ( ! matrix[ upleft ][ upleft ] .isOne() ) {
                Element divisor = matrix[ upleft ][ upleft ] .reciprocal();
                for ( int j = upleft; j < matrix[0].length; j++ )
                    matrix[ upleft ][ j ] = matrix[ upleft ][ j ] .times( divisor );
                for ( int j = 0; j < adjoined[0].length; j++ )
                	adjoined[ upleft ][ j ] = adjoined[ upleft ][ j ] .times( divisor );
            }

            // zero out all other entries in the upleft column, subtracting m[i,upleft]*m[upleft,j] from m[i,j]
            for ( int i = 0; i < matrix.length; i++ )
                if ( i != upleft && ! matrix[ i ][ upleft ] .isZero() ) {
                    Element factor = matrix[ i ][ upleft ];
                    matrix[ i ][ upleft ] = factor .plus( factor .negate() ); // zero it out
                    for ( int j = upleft+1; j < matrix[0].length; j++ ) {
                        Element temp = matrix[ upleft ][ j ] .times( factor );
                        matrix[ i ][ j ] = matrix[ i ][ j ] .plus( temp .negate() );
                    }
                    for ( int j = 0; j < adjoined[0].length; j++ ) {
                    	Element temp = adjoined[ upleft ][ j ] .times( factor );
                    	adjoined[ i ][ j ] = adjoined[ i ][ j ] .plus( temp .negate() );
                    }
                }
        }
    }
}
