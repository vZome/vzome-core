
//(c) Copyright 2015, Scott Vorthmann.

package com.vzome.core.construction;

import java.util.ArrayList;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.HeptagonField;
import com.vzome.core.algebra.PentagonField;
import com.vzome.core.algebra.RootThreeField;
import com.vzome.core.algebra.RootTwoField;
import com.vzome.core.algebra.SnubDodecField;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class VefToModelTest
{
    @Test
    public void testAutomaticField() {
        final AlgebraicVector quaternion = null;
        final NewConstructions effects = new NewConstructions();
        final AlgebraicVector offset = null;
        // we're testing that the keyword "automatic" here can be applied to all types of AlgebraicField
        final String vefData = "vZome VEF 7 field automatic "
                + "actual scale 1 "
                + "3 "
                + "0 0 0 0 "
                + "0 1 2 3 "
                + "4 5 6 7 "
                ;
        
        final AlgebraicField pentagonField = new PentagonField();
        final AlgebraicField[] fields = { 
            pentagonField, 
            new RootTwoField(), 
            new RootThreeField(), 
            new HeptagonField(), 
            new SnubDodecField( pentagonField ),
        };

        int testsPassed = 0;
        try {
            for(AlgebraicField field : fields ) {
                effects.clear();
                AlgebraicNumber scale = field.createPower(0);
                assertTrue(scale.isOne());
                VefToModel parser = new VefToModel(quaternion, effects, scale, offset);
                parser.parseVEF(vefData, field);
                assertEquals(effects.size(), 3);
                testsPassed++;
            }
        }
        catch(Exception ex) {
            fail(ex.toString());
        }
        assertEquals(testsPassed, 5);
    }

    @Test
    public void testScaling() {
        final AlgebraicVector quaternion = null;
        final NewConstructions effects = new NewConstructions();
        final AlgebraicVector offset = null;
        final AlgebraicField field = new RootTwoField();

        AlgebraicNumber scale = field.createPower(6); // rootTwo^6 = 2^3 = 8
        assertEquals(scale, field.createRational(8));

        VefToModel parser = new VefToModel(quaternion, effects, scale, offset);

        String vefData = "vZome VEF 7 field rootTwo "
                + "actual scale 1 "
                + "3 "
                + "0 0 0 0 "
                + "0 1 2 3 "
                + "4 5 6 7 "
                ;

        // The scale parameter passed to parser should be ignored for now
        // because of the keyword "actual" in vefData
        AlgebraicVector expected = field.createVector(new int[] {
            1, 1, 0, 1,  
            2, 1, 0, 1,  
            3, 1, 0, 1
        });
        parser.parseVEF(vefData, field);
        assertTrue(parser.usesActualScale());
        Point p0 = (Point) effects.get(1);
        AlgebraicVector v0 = p0.getLocation();
        assertEquals(expected, v0);

        // The scale parameter passed to parser is still being ignored
        // because of the keyword "actual" in vefData
        // but the scale in vefData is changed to 3, so we multiply our expected value by 3 as well.
        vefData = vefData.replace("scale 1 ", "scale 3 ");
        expected = expected.scale(field.createRational(3));
        effects.clear();
        parser.parseVEF(vefData, field);
        assertTrue(parser.usesActualScale());
        p0 = (Point) effects.get(1);
        v0 = p0.getLocation();
        assertEquals(expected, v0);

        // Now we'll allow the parser to apply its scale parameter
        // by removing the keyword "actual" from vefData
        // so we must also multiply our expected value by scale, which is 8.
        vefData = vefData.replace("actual ", "");
        expected = expected.scale(scale);
        effects.clear();
        parser.parseVEF(vefData, field);
        assertFalse(parser.usesActualScale());
        p0 = (Point) effects.get(1);
        v0 = p0.getLocation();
        assertEquals(expected, v0);

        // just to be sure we ended up where we expected to be...
        assertEquals(expected, field.createVector(new int[] {
            3*8*1, 1, 0, 1,
            3*8*2, 1, 0, 1,
            3*8*3, 1, 0, 1
        }));
    }

    @Test
    public void testMixedVectorFormat()
    {
        final AlgebraicVector quaternion = null;
        final NewConstructions effects = new NewConstructions();
        final AlgebraicVector offset = null;
        final AlgebraicField[] order2Fields = {
            new PentagonField(),
            new RootTwoField(),
            new RootThreeField(),
        };

        int testsPassed = 0;
        // Verify that a mix of rational and irrational formatted values can be parsed to provide the correct values
        final String vefData = "vZome VEF 7 field automatic "
                + "actual scale 1 "
                + "4 "
                + "1 2 3 4 " // all integers
                + "0/1 5/6 7/8 9/10 " // all rational
                + "(0,0) (1/2,-3) (0,5/5) (12/6,-1) " // all with parenthesis containing irrational components, some as unreduced fractions
                + "12 (3/4,5) (-6,0) 7 " // mixed rational and irrationals with parenthesis
                ;

        try {
            for(AlgebraicField field : order2Fields ) {
                if(field.getOrder() != 2) {
                    fail("This test is only designed for order 2 fields.");
                }
                effects.clear();
                AlgebraicVector expected[] = new AlgebraicVector[] {
                    field .createVector( new int[]{ 2, 1, 0, 1,   3, 1, 0, 1,   4, 1, 0, 1 } ),
                    field .createVector( new int[]{ 5, 6, 0, 1,   7, 8, 0, 1,   9,10, 0, 1 } ),
                    field .createVector( new int[]{-3, 1, 1, 2,   1, 1, 0, 1,  -1, 1, 2, 1 } ), // note that 12/6 and 5/5 are now reduced
                    field .createVector( new int[]{ 5, 1, 3, 4,   0, 1,-6, 1,   7, 1, 0, 1 } ),
                };
                AlgebraicNumber scale = field.createRational(1);
                VefToModel parser = new VefToModel(quaternion, effects, scale, offset);
                parser.parseVEF(vefData, field);
                assertEquals(effects.size(), expected.length);
                for( int i = 0; i < effects.size(); i++) {
                    Point p1 = (Point) effects.get(i);
                    AlgebraicVector v1 = p1.getLocation();
                    assertEquals( expected[i], v1 );
                    testsPassed++;
                }
            }
        }
        catch(Exception ex) {
            fail(ex.toString());
        }
        assertEquals(testsPassed, 3 * 4 );
    }

    @Test
    public void testParse()
    {
        AlgebraicField field = new RootTwoField();
        AlgebraicVector quaternion = field .createVector( new int[]{ 2,1,1,1, 2,1,1,1, 2,1,1,1, 2,1,1,1 } );
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

        VefToModel parser = new VefToModel( quaternion, effects, field .createPower( 5 ), null );
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

    private static class NewConstructions extends ArrayList<Construction> implements ConstructionChanges
    {
        @Override
        public void constructionAdded( Construction c )
        {
            add( c );
        }
    }
}
