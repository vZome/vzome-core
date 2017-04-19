package com.vzome.core.algebra;

import static org.junit.Assert.*;

import org.junit.Test;

public class AlgebraicVectorTest {

	@Test
	public void testConstructors()
	{
  		AlgebraicField field = new PentagonField();
		AlgebraicNumber zero = field .zero();
		AlgebraicNumber one = field .one();
		AlgebraicNumber two = field .createAlgebraicNumber( 2 );
		AlgebraicNumber three = field .createAlgebraicNumber( 3 );
		AlgebraicNumber four = field .createAlgebraicNumber( 4 );
		AlgebraicNumber five = field .createAlgebraicNumber( 5 );

        // these two lines shouldn't compile...
		//  AlgebraicVector oneD = new AlgebraicVector( one );
		//  AlgebraicVector twoD = new AlgebraicVector( one, two );

        // ...so we must explicitly specify an array if we want an arbitrary number of dimensions
		AlgebraicVector oneD = new AlgebraicVector( new AlgebraicNumber[] {one} );
		AlgebraicVector twoD = new AlgebraicVector( new AlgebraicNumber[] {one, two} );
		AlgebraicVector threeD = new AlgebraicVector( new AlgebraicNumber[] {one, two, three } );
		AlgebraicVector fourD = new AlgebraicVector( new AlgebraicNumber[] {one, two, three, four } );
		AlgebraicVector fiveD = new AlgebraicVector( new AlgebraicNumber[] {one, two, three, four, five } );

		assertEquals( 1, oneD.dimension() );
		assertEquals( 2, twoD.dimension() );
		assertEquals( 3, threeD.dimension() );
		assertEquals( 4, fourD.dimension() );
		assertEquals( 5, fiveD.dimension() );

        // .. or we can use the optimized explicit 3D and 4D constructors without the cumbersome array syntax
		threeD = new AlgebraicVector( zero, one, two );
		fourD = new AlgebraicVector( zero, one, two, three );
		assertEquals( 3, threeD.dimension() );
		assertEquals( 4, fourD.dimension() );
    }

	@Test
	public void testCrossProduct()
	{
		AlgebraicField field = new PentagonField();
		AlgebraicNumber two = field .createAlgebraicNumber( 2 );
		AlgebraicNumber three = field .createAlgebraicNumber( 3 );
		AlgebraicVector x = new AlgebraicVector( two, field .one(), two );
		AlgebraicVector y = new AlgebraicVector( three, field .one() .negate(), three .negate() );
		AlgebraicVector result = x .cross( y );
		AlgebraicVector target = new AlgebraicVector( field .one() .negate(), field .createAlgebraicNumber( 12 ), field .createAlgebraicNumber( -5 ) );
		assertEquals( target, result );
	}

}
