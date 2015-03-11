/*
 * Created on Jun 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.vzome.core.viewing;

import java.util.HashMap;
import java.util.Map;

import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.parts.DefaultStrutGeometry;
import com.vzome.core.parts.StrutGeometry;
import com.vzome.core.render.Shapes;


public abstract class AbstractShapes implements Shapes
{
    private final Map strutShapesByLengthAndOrbit = new HashMap();

    private final Map strutGeometriesByOrbit = new HashMap();

    protected final String mPkgName;

    final String mName, alias;
    
    protected final Symmetry mSymmetry;

    protected Polyhedron mConnectorGeometry;

    public AbstractShapes( String pkgName, String name, String alias, Symmetry symm )
    {
        mPkgName = pkgName;
        mName = name;
        this .alias = alias;
        mConnectorGeometry = null;
        mSymmetry = symm;
    }

    public AbstractShapes( String pkgName, String name, Symmetry symm )
    {
        this( pkgName, name, null, symm );
    }
    
    protected StrutGeometry createStrutGeometry( Direction dir )
    {
        return new DefaultStrutGeometry( dir );
    }

    public String getName()
    {
        return mName;
    }

    public String getAlias()
    {
        return this.alias;
    }

    public String getPackage()
    {
        return mPkgName;
    }

    public Polyhedron getConnectorShape()
    {
        if ( mConnectorGeometry == null ) {
            mConnectorGeometry = buildConnectorShape( mPkgName );
            mConnectorGeometry .setName( "ball" );
        }
        return mConnectorGeometry;
    }
    
    protected abstract Polyhedron buildConnectorShape( String pkgName );
    
    /*
     * TODO:
     * 
     * I need a scheme that does not involve division to render struts.
     * 
     * This could be accomplished by NOT trying to reuse one polyhedron per length,
     * for all orientations in the orbit, since the VEF-exported strut model does not
     * insist upon scaling, but simply offsets the end of the strut.  This week
     * I experimented with making all StrutGeometries work that way, and it is feasible
     * (since ModeledShapes is no longer used anywhere).  This approach, however, means
     * making no use of the orientation to render; that could even be a speedup, hard to say.
     * It doesn't work for non-DefaultStrutGeometry, since it requires that the vertices of the
     * prototype shape be transformed first, to match the offset vector direction.
     * 
     * (Basically, I changed "length" below to "offset", everywhere, and removed the scaling
     * in the caller of this method.  See "normSquared" below. )
     * 
     * I think the simplest thing to do is to inverse-transform the offset vector into alignment
     * with the prototype for the orbit, and use the absolute offset to create a representative
     * Polyhedron for the orbit+length, while continuing to use the orientation when rendering.
     * This requires a bit of up-front determination of inverse orientations, but should not require
     * any matrix inversion.
     * 
     * For now I'm reverting to the usual behavior, but I want to come back and implement what I
     * just described above.
     */

    public Polyhedron getStrutShape( Direction orbit, AlgebraicNumber length )
    {
        Map strutShapesByLength = (Map) strutShapesByLengthAndOrbit.get( orbit );
        if ( strutShapesByLength == null ) {
            strutShapesByLength = new HashMap();
            strutShapesByLengthAndOrbit.put( orbit, strutShapesByLength );
        }
//        int[] normSquared = mSymmetry .getField() .dot( offset, offset );
        Polyhedron lengthShape = (Polyhedron) strutShapesByLength.get( length );
        if ( lengthShape == null ) {
            StrutGeometry orbitStrutGeometry = (StrutGeometry) strutGeometriesByOrbit.get( orbit );

            if ( orbitStrutGeometry == null ) {
                orbitStrutGeometry = createStrutGeometry( orbit );
                strutGeometriesByOrbit.put( orbit, orbitStrutGeometry );
            }

            if ( orbit .getSymmetry() .getField() .getName() .equals( "snubDodec" ) )
            {
                System .out .println( "\n\n======== " + orbit .getName() );
            }
            lengthShape = orbitStrutGeometry .getStrutPolyhedron( length );
            strutShapesByLength.put( length, lengthShape );
            if ( lengthShape != null ) {
                lengthShape .setName( orbit .getName() + strutShapesByLength .size() );
                lengthShape .setOrbit( orbit );
                // reproduce the calculation in LengthModel .setActualLength()                
                lengthShape .setLength( orbit .getLengthInUnits( length ) );
            }
        }
        return lengthShape;
    }

    // no changes are ever generated

    public void addListener( Changes changes )
    {}

    public void removeListener( Changes changes )
    {}

    public Symmetry getSymmetry()
    {
        return mSymmetry;
    }

}
