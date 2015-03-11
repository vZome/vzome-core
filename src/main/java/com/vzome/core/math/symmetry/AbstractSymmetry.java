
//(c) Copyright 2005, Scott Vorthmann.  All rights reserved.

package com.vzome.core.math.symmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicMatrix;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.math.RealVector;

/**
 * @author Scott Vorthmann
 *
 */
public abstract class AbstractSymmetry implements Symmetry
{
    protected final Map mDirectionMap = new HashMap();
    
    protected final List mDirectionList = new ArrayList(); // TODO remove, redundant with orbitSet
    
    protected final OrbitSet orbitSet = new OrbitSet( this );

    protected final Permutation[] mOrientations;
    
    protected final AlgebraicMatrix[] mMatrices;
    
    protected final AlgebraicField mField;
    
    protected final String defaultStyle;    
        
    protected AbstractSymmetry( int order, AlgebraicField field, String frameColor, String defaultStyle )
    {
        mField = field;
        mField .addSymmetry( this );
        
        this .defaultStyle = defaultStyle;
        
        mOrientations = new Permutation[ order ];
        mMatrices = new AlgebraicMatrix[ order ];
        
        createInitialPermutations();

//        boolean[] initialPerms = new boolean[ order ];
//        for ( int i = 0; i < order; i++ ) {
//            Permutation p1 = mOrientations[ i ];
//            if ( p1 == null )
//                continue;
//            initialPerms[ i ] = true;
//        }
        
        // now, discover all possible compositions of these
        boolean done = false;
        while ( ! done ) {
            done = true;
            for ( int i = 1; i < order; i++ ) { // skip 0, the identity
                Permutation p1 = mOrientations[ i ];
                if ( p1 == null ) {
                    done = false;
                    continue;
                }
                done = true;
                for ( int j = 1; j < order; j++ ) {
                    Permutation p2 = mOrientations[ j ];
                    if ( p2 == null ) {
                        done = false;
                        continue;
                    }
                    int result = p1 .mapIndex( p2 .mapIndex( 0 ) );
                    if ( mOrientations[ result ] != null )
                        continue;
//                    System .out .println( result + " = " + i + " * " + j );
                    int[] map = new int[order];
                    for ( int k = 0; k < order; k++ )
                        map[ k ] = p1 .mapIndex( p2 .mapIndex( k ) );
                    mOrientations[ result ] = new Permutation( this, map );
                }
                if ( done ) break;
            }
        }
        
        createFrameOrbit( frameColor );
        createOtherOrbits();

//        for ( int i = 0; i < order; i++ ) {
//            if ( initialPerms[ i ] )
//                System .out .println( i + " = " + mMatrices[ i ] .toString() );
//        }        
    }
    
    protected abstract void createFrameOrbit( String frameColor );

    protected abstract void createOtherOrbits();

    protected abstract void createInitialPermutations();
    
    public String getDefaultStyle()
    {
        return this .defaultStyle;
    }

    public AlgebraicField getField()
    {
        return mField;
    }
    
    public Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, int[] norm )
    {
        AlgebraicVector aNorm = mField .createVector( norm );
        return createZoneOrbit( name, prototype, rotatedPrototype, aNorm, false );
    }

    public Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, AlgebraicVector norm )
    {
        return createZoneOrbit( name, prototype, rotatedPrototype, norm, false );
    }

    protected Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, int[] norm, boolean standard )
    {
        AlgebraicVector aNorm = mField .createVector( norm );
        return createZoneOrbit( name, prototype, rotatedPrototype, aNorm, standard, false );
    }

    protected Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, AlgebraicVector norm, boolean standard )
    {
        return createZoneOrbit( name, prototype, rotatedPrototype, norm, standard, false );
    }

    protected Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, int[] norm, boolean standard, boolean halfSizes )
    {
        AlgebraicVector aNorm = mField .createVector( norm );
        return createZoneOrbit( name, prototype, rotatedPrototype, aNorm, standard, false, null );
    }

    protected Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, AlgebraicVector norm, boolean standard, boolean halfSizes )
    {
        return createZoneOrbit( name, prototype, rotatedPrototype, norm, standard, false, mField .one() );
    }

    protected Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, int[] norm, boolean standard, boolean halfSizes, AlgebraicNumber unitLength )
    {
        AlgebraicVector aNorm = mField .createVector( norm );
        return createZoneOrbit( name, prototype, rotatedPrototype, aNorm, standard, halfSizes, unitLength );
    }

    protected Direction createZoneOrbit( String name, int prototype, int rotatedPrototype, AlgebraicVector norm, boolean standard, boolean halfSizes, AlgebraicNumber unitLength )
    {
        Direction dir = new Direction( name, this, prototype, rotatedPrototype, norm, standard );
        if ( halfSizes )
            dir .setHalfSizes( true );
        dir .setUnitLength( unitLength );
        mDirectionMap .put( dir .getName(), dir );
        mDirectionList .add( dir );
        orbitSet .add( dir );
        return dir;
    }
    
    public Direction createNewZoneOrbit( String name, int prototype, int rotatedPrototype, AlgebraicVector norm )
    {
        return new Direction( name, this, prototype, rotatedPrototype, norm, false );
    }



    // =================== public stuff =========================================

    public OrbitSet getOrbitSet()
    {
        return this.orbitSet;
    }
    
    /**
	 * @param unit
	 * @param rot
	 * @return
	 */
	public int getMapping( int from, int to )
	{
	    if ( to == NO_ROTATION )
	        return NO_ROTATION;
		for ( int p = 0; p < mOrientations.length; p++ )
			if ( mOrientations[ p ] .mapIndex( from ) == to )
				return p;
		return NO_ROTATION;
	}

	public Permutation mapAxis( Axis from, Axis to )
    {
        return mapAxes( new Axis[]{ from }, new Axis[]{ to } );
    }

    public Permutation mapAxes( final Axis[] from, final Axis[] to )
    throws MismatchedAxes
    {
        if ( from .length != to .length )
            throw new MismatchedAxes( "must map to equal number of axes" );
        if ( from .length > 3 )
            throw new MismatchedAxes( "must map three or fewer axes" );
        for ( int i = 0; i < from .length; i++ )
            if ( from[i] .getDirection() .equals( to[i] .getDirection() ) )
                throw new MismatchedAxes( "must map between same color axes" );
        final Permutation[] result = new Permutation[1];
        // try right handed ones first!
//		new ForEachOrientation( true, false, this ){
//			public boolean doOrientation( Permutation current ){
//				for ( int i = 0; i < from .length; i++ )
//					if ( ! to[i] .equals( current .permute( from[i] ) ) )
//						return true;
//				result[0] = current;
//				return false;
//			}
//		} .doEach();
//		// now try left-handed ones
//		if ( result[0] == null )
//			new ForEachOrientation( false, true, this ){
//				public boolean doOrientation( Permutation current ){
//					for ( int i = 0; i < from .length; i++ )
//						if ( ! to[i] .equals( current .permute( from[i] ) ) )
//							return true;
//					result[0] = current;
//					return false;
//				}
//			} .doEach();
//		if ( result[0] == null )
//            throw new MismatchedAxes( "map is impossible" );
        return result[0];
    }

    public static class MismatchedAxes extends RuntimeException
    {
        private static final long serialVersionUID = 2610579323321804987L;

        public MismatchedAxes( String message ){
            super( message );
        }
    }

    public Iterator getDirections()
    {
        return mDirectionList .iterator();
    }
    
    
    // TODO this is now partly redundant with SymmetryController .getAxis()
    //
    public Axis getAxis( AlgebraicVector vector )
    {
        if ( vector .isOrigin() ) {
            return null;
        }
        for ( Iterator dirs = mDirectionList .iterator(); dirs .hasNext(); ) {
            Direction dir = (Direction) dirs .next();
            Axis line = dir .getAxis( vector );
            if ( line != null )
            	return line;
        }
        return null;

//        String axisName = "unnamed_" + NEXT_NEW_AXIS++;
//        Direction dir = this .createZoneOrbit( axisName, 0, Symmetry.NO_ROTATION, vector );
//        dir .setAutomatic( true );
//        return dir .getAxis( PLUS, 0 );
    }


	/**
	 * Returns the axis nearest the given the direction,
	 * subject to the mask of directions to accept.
	 *
	 */
	public Axis getAxis( RealVector vector, Set dirMask )
	{
		if ( RealVector .ORIGIN .equals( vector ) ) {
			return null;
		}
		// largest cosine means smallest angle
		//  and cosine is (a . b ) / |a| |b|
		double maxCosine = - 1d;
		Axis closest = null;
		Iterator dirs = orbitSet .iterator();
		if ( dirMask != null )
		    dirs = dirMask .iterator();
        while ( dirs .hasNext() ) {
            Direction dir = (Direction) dirs .next();
            // now iterate over axes
			for ( Iterator axes = dir .getAxes(); axes .hasNext(); ) {
				Axis axis = (Axis) axes .next();
				RealVector axisV = axis .normal() .toRealVector();
				double cosine = vector .dot( axisV ) / (vector .length() * axisV .length());
				if ( cosine > maxCosine ) {
					maxCosine = cosine;
					closest = axis;
				}
			}
        }
		return closest;
	}
	
    public int getChiralOrder()
    {
        return mOrientations .length;
    }

    public Permutation getPermutation( int i )
    {
    	if ( ( i < 0 ) || ( i > mOrientations .length ) )
    		return null;
        return mOrientations[ i ];
    }


	public AlgebraicMatrix getMatrix(int i)
	{
		return mMatrices[ i ];
	}

	public int inverse( int orientation )
	{
    	if ( ( orientation < 0 ) || ( orientation > mOrientations .length ) )
    		return Symmetry .NO_ROTATION;
		return mOrientations[ orientation ] .inverse() .mapIndex( 0 );
	}

	public Direction getDirection( String color )
	{
		return (Direction) mDirectionMap .get( color );
	}
    
    public String[] getDirectionNames()
    {
        ArrayList list = new ArrayList();
        for ( int i = 0; i < mDirectionList .size(); i++ ) {
            Direction dir = (Direction) mDirectionList .get( i );
            if ( ! dir .isAutomatic() )
                list .add( dir .getName() );
        }
        return (String[]) list .toArray( new String[]{} );
    }

    public int[] closure( int[] perms )
    {
        List newPerms = new ArrayList();
        Permutation[] closure = new Permutation[ mOrientations .length ];
        int closureSize = 0;
        
        for ( int i = 0; i < perms.length; i++ ) {
            Permutation perm = mOrientations[ perms[i] ];
            closure[ perms[i] ] = perm;
            newPerms .add( perm );
            ++ closureSize;
        }
        
        while ( !newPerms .isEmpty() ) {
            Permutation perm = (Permutation) newPerms .remove(0);
            for ( int i = 0; i < closure.length; i++ ) 
                if ( closure[i] != null ) {
                    Permutation composition = perm .compose( closure[i] );
                    int j = composition .mapIndex( 0 );
                    if ( closure[ j ] == null ) {
                        newPerms .add( composition );
                        closure[ j ] = composition;
                        ++ closureSize;
                    }
                    composition = closure[i] .compose( perm );
                    j = composition .mapIndex( 0 );
                    if ( closure[ j ] == null ) {
                        newPerms .add( composition );
                        closure[ j ] = composition;
                        ++ closureSize;
                    }
                }
        }
        
        int[] result = new int[ closureSize ];
        int j = 0;
        for ( int i = 0; i < closure.length; i++ )
            if ( closure[ i ] != null )
                result[ j++ ] = i;
        return result;
    }
    
}
