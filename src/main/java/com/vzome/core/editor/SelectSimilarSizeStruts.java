
//(c) Copyright 2011, Scott Vorthmann.

package com.vzome.core.editor;


import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.math.DomUtils;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;
import java.util.logging.Level;
import org.w3c.dom.Element;

public class SelectSimilarSizeStruts extends ChangeSelection
{    
    public enum ComparisonModeEnum {
        SAME_LENGTH,
        PARALLEL_TO_AXIS
    }
    
    private ComparisonModeEnum comparisonMode;
    private Direction orbit;
    private Axis zone;
    private AlgebraicNumber length;
    private final RealizedModel model;
    private final SymmetrySystem symmetry;

    // All public c'tors have the three common parameters first followed by context specific qualifiers

    /**
     * called by both selectSimilarSizeStruts and selectParallelStruts from the main menu
     * @param symmetry
     * @param selection
     * @param model
     * @param mode distinguishes the desired behavior of the command
     */
    public SelectSimilarSizeStruts(SymmetrySystem symmetry, Selection selection, RealizedModel model, 
            ComparisonModeEnum mode)
    {
        super( selection, false );
        this.symmetry = symmetry;
        this.model = model;
        this.comparisonMode = mode;
        Strut lastStrut = null;
        for (Manifestation man : mSelection) {
            if (man.isRendered() && man instanceof Strut) {
                lastStrut = (Strut) man;
            }
        }
        if (lastStrut != null) {
            AlgebraicVector offset = lastStrut.getOffset();
            this.orbit = symmetry.getAxis(offset).getOrbit();
            this.zone = orbit.getAxis(offset);
            this.length = zone.getLength(offset);
        } else {
            this.orbit = null;
            this.zone = null;
            this.length = null;
        }
    }
        
    /**
     * called by selectSimilarStruts from the strut context menu
     * @param symmetry
     * @param selection
     * @param model
     * @param orbit
     * @param length
     */
    public SelectSimilarSizeStruts(SymmetrySystem symmetry, Selection selection, RealizedModel model, 
            Direction orbit, AlgebraicNumber length)
    {
        super( selection, false );
        this.symmetry = symmetry;
        this.model = model;
        this.comparisonMode = ComparisonModeEnum.SAME_LENGTH;
        this.orbit = orbit;
        this.length = length;
        this.zone = null;
    }
    
    /**
     * called by selectParallelStruts from the strut context menu
     * @param symmetry
     * @param selection
     * @param model
     * @param orbit
     * @param axis
     */
    public SelectSimilarSizeStruts(SymmetrySystem symmetry, Selection selection, RealizedModel model, 
            Direction orbit, Axis axis)
    {
        super( selection, false );
        this.symmetry = symmetry;
        this.model = model;
        this.comparisonMode = ComparisonModeEnum.PARALLEL_TO_AXIS;
        this.orbit = orbit;
        this.zone = axis;
        this.length = null;
    }
    
	// Normal usage cases include both "Select Similar Size Struts" and "Select Parallel Struts" from:
    // 1) Main menu with 1 or more struts selected (Last selected strut will be used as input)
    // 2) Strut context menu
    // 3) Undo and Redo operations
    // 4) Reopening a saved vZome file (persisted as XML)
    // 5) Reopening a previous version of vZome file with no comparisonMode specified.
    //
    // Ensure that we can safely do nothing (with no exceptions thrown) 
    //   using both "Select Similar Size Struts" and "Select Parallel Struts" from:
    // 1) Main menu with nothing selected
    // 2) Main menu with balls and/or panels selected but no strut selected
    @Override
    public void perform() throws Failure {
        deselectAllStruts();

        Axis oppositeZone = null;
        if(comparisonMode == ComparisonModeEnum.PARALLEL_TO_AXIS) {
            int opposite = ( zone .getSense() + 1 ) % 2;
            oppositeZone = orbit. getAxis( opposite, zone .getOrientation() );
        }

        for (Manifestation man : model) {
            if (man.isRendered() && man instanceof Strut ) {
                Strut strut = (Strut) man;
                AlgebraicVector offset = strut .getOffset();
                Axis axis = symmetry.getAxis(offset);
                if (axis != null && orbit == axis.getOrbit()) {
                    switch (comparisonMode) {
                        case SAME_LENGTH:
                            if( axis.getLength(offset).equals(length) ) {
                                select(strut);
                            }
                            break;
                        case PARALLEL_TO_AXIS:
                            if ( axis.equals(zone) || axis.equals(oppositeZone) ) {
                                select(strut);
                            }
                            break;
                        default:
                            throw new UnsupportedOperationException("Unexpected Comparison Mode: " + comparisonMode.toString());
                    }
                }
            }
        }
        super .perform();
    }

    private void deselectAllStruts()
    {
        boolean anySelectedStruts = false;
        for (Manifestation man : model) {
            if (man.isRendered() && man instanceof Strut) {
                anySelectedStruts = true;
                unselect(man);
            }
        }
        if (anySelectedStruts) {
            redo();
        }
    }

    @Override
    protected String getXmlElementName()
    {
        return "SelectSimilarSize";
    }

    private static final String attrComparisonMode = "comparisonMode"; // avoid typos

    @Override
    protected void getXmlAttributes( Element element )
    {
        DomUtils .addAttribute( element, attrComparisonMode, comparisonMode.toString());
        DomUtils .addAttribute( element, "symmetry", symmetry .getName() );
        DomUtils .addAttribute( element, "orbit", orbit .getName() );
        if ( zone != null )
            zone.getXML(element);
        if ( length != null )
            XmlSaveFormat .serializeNumber( element, "length", length );
    }

    @Override
    protected void setXmlAttributes( Element xml, XmlSaveFormat format )
            throws Failure
    {
        comparisonMode = ComparisonModeEnum.SAME_LENGTH;
        String strAttr = xml.getAttribute(attrComparisonMode);
        if(strAttr != null && !strAttr.isEmpty()) {
            try {
                comparisonMode = ComparisonModeEnum.valueOf(strAttr.trim());
            }
            catch(IllegalArgumentException ex) {
                String msg = "'" + strAttr +
                        "' is not a valid " + comparisonMode.getClass().getSimpleName()
                        + ". Defaulting to " + comparisonMode + ".";
                logger.log(Level.WARNING, msg, ex);
            }
        }
        orbit = symmetry .getOrbits() .getDirection( xml .getAttribute( "orbit" ) );
        switch (comparisonMode) {
            case SAME_LENGTH:
                length = format .parseNumber( xml, "length" );
                break;
            case PARALLEL_TO_AXIS:
                String strSense = xml.getAttribute("sense");
                int sense = (strSense != null && "minus".equals(strSense.toLowerCase()))
                        ? Symmetry.MINUS
                        : Symmetry.PLUS;
                int index = Integer.parseInt(xml.getAttribute("index"));
                this.zone = orbit.getAxis(sense, index);
                break;
            default:
                throw new UnsupportedOperationException("Unexpected Comparison Mode: " + comparisonMode.toString());
        }
    }
}
