
//(c) Copyright 2015, Scott Vorthmann.

package com.vzome.core.construction;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.RootTwoField;

public class VefToModelTest extends TestCase
{
    public void testParse()
    {
        AlgebraicField field = new RootTwoField();
        AlgebraicVector quaternion = field .createVector( new int[]{ 2,1,1,1, 2,1,1,1, 2,1,1,1, 2,1,1,1 } );
        ModelRoot root = new ModelRoot( field );
        NewConstructions effects = new NewConstructions();
        String vefData = "64 (-1,1) (0,1) (0,1) (0,1) (1,-1) (0,1) (0,1) (0,1) " +
               "(0,1) (-1,1) (0,1) (0,1) (0,1) (1,-1) (0,1) (0,1) (0,1) " +
               "(0,1) (-1,1) (0,1) (0,1) (0,1) (1,-1) (0,1) (0,1) (0,1) " +
               "(0,1) (-1,1) (0,1) (0,1) (0,1) (1,-1) (0,-1) (-1,1) (0,1) " +
               "(0,1) (0,-1) (1,-1) (0,1) (0,1) (0,-1) (0,1) (-1,1) (0,1) " +
               "(0,-1) (0,1) (1,-1) (0,1) (0,-1) (0,1) (0,1) (-1,1) (0,-1) " +
               "(0,1) (0,1) (1,-1) (-1,1) (0,-1) (0,1) (0,1) (1,-1) (0,-1) " +
               "(0,1) (0,1) (0,1) (0,-1) (-1,1) (0,1) (0,1) (0,-1) (1,-1) " +
               "(0,1) (0,1) (0,-1) (0,1) (-1,1) (0,1) (0,-1) (0,1) (1,-1) " +
               "(0,-1) (0,-1) (-1,1) (0,1) (0,-1) (0,-1) (1,-1) (0,1) (0,-1) " +
               "(0,-1) (0,1) (-1,1) (0,-1) (0,-1) (0,1) (1,-1) (-1,1) (0,1) " +
               "(0,-1) (0,1) (1,-1) (0,1) (0,-1) (0,1) (0,1) (-1,1) (0,-1) " +
               "(0,1) (0,1) (1,-1) (0,-1) (0,1) (0,1) (0,1) (0,-1) (-1,1) " +
               "(0,1) (0,1) (0,-1) (1,-1) (0,-1) (-1,1) (0,-1) (0,1) (0,-1) " +
               "(1,-1) (0,-1) (0,1) (0,-1) (0,1) (0,-1) (-1,1) (0,-1) (0,1) " +
               "(0,-1) (1,-1) (-1,1) (0,-1) (0,-1) (0,1) (1,-1) (0,-1) (0,-1) " +
               "(0,1) (0,1) (0,-1) (0,-1) (-1,1) (0,1) (0,-1) (0,-1) (1,-1) " +
               "(0,-1) (0,-1) (0,-1) (-1,1) (0,-1) (0,-1) (0,-1) (1,-1) " +
               "(-1,1) (0,1) (0,1) (0,-1) (1,-1) (0,1) (0,1) (0,-1) (0,1) " +
               "(-1,1) (0,1) (0,-1) (0,1) (1,-1) (0,1) (0,-1) (0,1) (0,1) " +
               "(-1,1) (0,-1) (0,1) (0,1) (1,-1) (0,-1) (0,-1) (-1,1) (0,1) " +
               "(0,-1) (0,-1) (1,-1) (0,1) (0,-1) (0,-1) (0,1) (-1,1) (0,-1) " +
               "(0,-1) (0,1) (1,-1) (0,-1) (-1,1) (0,-1) (0,1) (0,-1) (1,-1) " +
               "(0,-1) (0,1) (0,-1) (0,1) (0,-1) (-1,1) (0,-1) (0,1) (0,-1) " +
               "(1,-1) (0,-1) (0,-1) (0,-1) (-1,1) (0,-1) (0,-1) (0,-1) " +
               "(1,-1) (0,-1) (-1,1) (0,1) (0,-1) (0,-1) (1,-1) (0,1) (0,-1) " +
               "(0,-1) (0,1) (-1,1) (0,-1) (0,-1) (0,1) (1,-1) (0,-1) (0,-1) " +
               "(0,-1) (-1,1) (0,-1) (0,-1) (0,-1) (1,-1) (0,-1) (0,-1) " +
               "(-1,1) (0,-1) (0,-1) (0,-1) (1,-1) (0,-1) (0,-1) (0,-1)";

        VefToModel parser = new VefToModel( quaternion, root, effects, field .createPower( 5 ), null );
        parser .parseVEF( vefData, field );
        
        Point p0 = (Point) effects .get( 20 );
        AlgebraicVector v0 = p0 .getLocation();
        AlgebraicVector expected = field .createVector( new int[]{ 0, 1, -8, 1, -32, 1, -24, 1, 0, 1, 8, 1 } );
        assertEquals( expected, v0 );
        
        Point p1 = (Point) effects .get( 39 );
        AlgebraicVector v1 = p1 .getLocation();
        expected = field .createVector( new int[]{ 0, 1, 8, 1, -32, 1, -24, 1, 0, 1, -8, 1 } );
        assertEquals( expected, v1 );
    }

    private static class NewConstructions extends ArrayList implements ConstructionChanges
    {
        public void constructionAdded( Construction c )
        {
            add( c );
        }

        public void constructionHidden( Construction c )
        {
            throw new UnsupportedOperationException();
        }

        public void constructionRemoved( Construction c )
        {
            throw new UnsupportedOperationException();
        }

        public void constructionRevealed( Construction c )
        {
            throw new UnsupportedOperationException();
        }
    }
}
