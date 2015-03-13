
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package com.vzome.core.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.construction.Construction;
import com.vzome.core.construction.ModelRoot;
import com.vzome.core.construction.Point;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;

public class ModuleTool extends ChangeConstructions implements Tool
{
    private String name;
    
    private final List bookmarkedSelection = new ArrayList();
        
    private Tool.Registry tools;

    private final ModelRoot root;

    public ModuleTool( String name, Selection selection, RealizedModel realized, ModelRoot root, Tool.Registry tools )
    {
        super( selection, realized, false );
        this.name = name;
        this.root = root;
        this.tools = tools;
        mSelection .copy( bookmarkedSelection );
    }

	public boolean isSticky()
    {
        return true;
    }

    public void perform() throws Failure
    {
        defineTool();
    }

    protected void defineTool()
    {
        tools .addTool( this );
    }

    public void prepare( ChangeConstructions applyTool ) {}

	public void complete( ChangeConstructions applyTool ) {}

    public boolean needsInput()
    {
    	return true;
    }

    public void performEdit( Construction c, ChangeConstructions applyTool )
    {
        if ( ! ( c instanceof Point ) )
            return;
        Point p = (Point) c;
        AlgebraicVector loc = p .getLocation();
        Duplicator duper = new Duplicator( applyTool, root, loc );
        for ( Iterator mans = bookmarkedSelection .iterator(); mans .hasNext(); ) {
            Manifestation man = (Manifestation) mans .next();
            duper .duplicateManifestation( man );
        }
        applyTool .redo();
    }

	public void performSelect( Manifestation man, ChangeConstructions applyTool ) {};

    public void redo()
    {
        // TODO manifest a symmetry construction... that is why this class extends ChangeConstructions
        // this edit is now sticky (not really undoable)
//        tools .addTool( this );
    }

    public void undo()
    {
        // this edit is now sticky (not really undoable)
//        tools .removeTool( this );
    }

    public String getName()
    {
        return name;
    }

    protected String getXmlElementName()
    {
        return "ModuleTool";
    }
    
    protected void getXmlAttributes( Element element )
    {
        element .setAttribute( "name", this.name );
    }

    protected void setXmlAttributes( Element element, XmlSaveFormat format ) throws Failure
    {
        this.name = element .getAttribute( "name" );
    }

    public String getCategory()
    {
        return "module";
    }

    public String getDefaultName()
    {
        return "module";
    }
}
