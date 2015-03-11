
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package com.vzome.core.editor;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.construction.ModelRoot;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.VefToModel;
import com.vzome.core.math.DomUtils;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;

public class LoadVEF extends ChangeConstructions
{
    private String vefData;
    private AlgebraicVector quaternion;
    private AlgebraicNumber scale;
    private ModelRoot root;


    public LoadVEF( Selection selection, RealizedModel realized, String vefData, AlgebraicVector quaternion, AlgebraicNumber scale, ModelRoot root )
    {
        super( selection, realized, false );
        this .vefData = vefData;
        this .quaternion = quaternion;
        this .scale = scale;
        this .root = root;
    }

    protected String getXmlElementName()
    {
        return "LoadVEF";
    }

    protected void getXmlAttributes( Element element )
    {
        if ( quaternion != null )
        	DomUtils .addAttribute( element, "quaternion", quaternion .toString() );
        if ( scale != null )
        	DomUtils .addAttribute( element, "scale", scale .toString( AlgebraicField.ZOMIC_FORMAT ) );
        Node textNode = element .getOwnerDocument() .createTextNode( XmlSaveFormat .escapeNewlines( vefData ) );
        element .appendChild( textNode );
    }

    protected void setXmlAttributes( Element xml, XmlSaveFormat format )
            throws Failure
    {
        quaternion = format .parseRationalVector( xml, "quaternion" );
        scale = format .parseRationalNumber( xml, "scale" );
        vefData = xml .getTextContent();
    }

    public void perform() throws Failure
    {
        AlgebraicVector offset = null;
        boolean pointFound = false;
        for ( Iterator mans = mSelection .iterator(); mans .hasNext(); ) {
            Manifestation man = (Manifestation) mans .next();
            if ( man instanceof Connector )
            {
                Point nextPoint = (Point) ((Connector) man) .getConstructions() .next();
                if ( ! pointFound )
                {
                    pointFound = true;
                    offset = nextPoint .getLocation();
                }
                else
                {
                    offset = null;
                }
            }
        }

        AlgebraicField field = root .getField();
        VefToModel v2m = new VefToModel( quaternion, root, new NewConstructions(), scale, offset );
        v2m .parseVEF( vefData, field );

        redo();
    }
}
