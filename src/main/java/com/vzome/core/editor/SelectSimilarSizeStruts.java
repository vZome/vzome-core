
//(c) Copyright 2011, Scott Vorthmann.

package com.vzome.core.editor;

import java.util.Arrays;
import java.util.Iterator;

import org.w3c.dom.Element;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.math.DomUtils;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;

public class SelectSimilarSizeStruts extends ChangeSelection
{
    private Direction orbit;
    private int[] length;
    private RealizedModel model;
    private Symmetry symmetry;
    private AlgebraicField field;

    public SelectSimilarSizeStruts( Direction orbit, int[] length,
            Selection selection, RealizedModel model, AlgebraicField field )
    {
        super( selection, false );
        this .field = field;
        this .model = model;
        this .symmetry = (orbit==null)? null : orbit .getSymmetry();
        this .orbit = orbit;
        this .length = length;
    }

    public void perform() throws Failure
    {
        for ( Iterator ms = model .getAllManifestations(); ms .hasNext(); ) {
            Manifestation man = (Manifestation) ms .next();
            if ( man .getRenderedObject() == null )
                continue;  // hidden!
            if ( man instanceof Strut ) {
                Strut strut = (Strut) man;
                int[] offset = strut .getOffset();
                Axis zone = symmetry .getAxis( offset );
                if ( zone == null ) // non-standard axis for the current symmetry group
                    // TODO: should we look in other symmetry groups? (e.g. icosahedral when current is octahedral)
                    // or should we use symmetry group independent logic for checking if the axis is the same?
                    // To reporoduce this bug, make 2 purple -15 struts in icosa symm, switch to octa symm, select one and then run this command on it.
                    // It should select both of them but it doesn't with this logic.
                    continue; // for now, at least avoid the null pointer exception, but this won't make the selection we requested. 
                Direction orbit = zone .getOrbit();
                if ( orbit != this .orbit )
                    continue;
                int[] length = zone .getLength( offset );
                if ( Arrays .equals( length, this .length ) )
                    select( strut );
            }
        }
        super .perform();
    }

    protected String getXmlElementName()
    {
        return "SelectSimilarSize";
    }

    protected void getXmlAttributes( Element element )
    {
        if ( symmetry != null )
            DomUtils .addAttribute( element, "symmetry", symmetry .getName() );
        if ( orbit != null )
        	DomUtils .addAttribute( element, "orbit", orbit .getName() );
        if ( length != null )
            XmlSaveFormat .serializeNumber( element, "length", length );
    }

    protected void setXmlAttributes( Element xml, XmlSaveFormat format )
            throws Failure
    {
        length = format .parseNumber( xml, "length" );
        symmetry = this .field .getSymmetry( xml .getAttribute( "symmetry" ) );
        orbit = symmetry .getDirection( xml .getAttribute( "orbit" ) );
    }
}
