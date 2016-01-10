package com.vzome.core.editor;

import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import static com.vzome.core.editor.ChangeSelection.logger;
import com.vzome.core.math.DomUtils;
import com.vzome.core.model.Connector;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.w3c.dom.Element;

/**
 * @author David Hall
 */
public class SelectCollinear extends SelectFromRealizedModel {

    private AlgebraicVector vector1;
    private AlgebraicVector vector2;
    private boolean vectorsFromSelection = false;

	/* 
	* Called by the context menu. Inputs are the two end points of the strut associated with the context menu.
	*/
    public SelectCollinear(Selection selection, RealizedModel model, boolean groupInSelection, AlgebraicVector vector1, AlgebraicVector vector2) {
        super(selection, model, groupInSelection);
        this.vector1 = vector1;
        this.vector2 = vector2;
        vectorsFromSelection = false;
    }

	/* 
	* Called by the main menu. Input is either the last selected strut or the last two selected balls.
	*/
    public SelectCollinear(Selection selection, RealizedModel model, boolean groupInSelection) {
        super(selection, model, groupInSelection);
        AlgebraicVector v1 = null;
        AlgebraicVector v2 = null;
        Strut lastStrut = null;
        for (Strut strut : SelectedStruts()) {
            lastStrut = strut;
        }
        if (lastStrut != null) {
            v1 = lastStrut.getLocation();
            v2 = lastStrut.getEnd();
        } else {
            for (Connector ball : SelectedConnectors()) {
                // use the last two balls selected if a strut was not selected
                v1 = v2;
                v2 = ball.getLocation();
            }
        }
        vector1 = v1;
        vector2 = v2;
        vectorsFromSelection = (vector1 != null && vector2 != null);
    }

    @Override
    public void perform() throws Command.Failure {
        if (vector1 != null && vector2 != null) {
            unselectConnectors();
            unselectStruts();

            Set<Connector> balls = new TreeSet<>(); // auto sorted
            // Example of using FilteredIterator with Predicate parameter.
            for (Connector ball : VisibleConnectors(this::isCollinearWith)) { 
                balls.add(ball);
            }
            
            Set<Strut> struts = new TreeSet<>(); // auto sorted
            // Example of conventional "if" syntax within the loop.
            // Compare to the equivalent FilteredIterator predicate syntax above.
            for (Strut strut : VisibleStruts()) {
                if (isCollinearWith(strut)) {
                    struts.add(strut);
                }
            }

			// balls and struts to be selected are now in two sorted sets
            // so we can select them in a consistent order, suitable
            // for subsequent use by other tools such as JoinPoints
            for (Connector ball : balls) {
                select(ball);
            }
            for (Strut strut : struts) {
                select(strut);
            }

            Level level = Level.FINER;
            if (logger.isLoggable(level)) {
                StringBuilder sb = new StringBuilder("Selected:\n");
                final String indent = "  ";
                for (Connector ball : balls) {
                    sb.append(indent).append(ball.toString()).append("\n");
                }
                for (Strut strut : struts) {
                    sb.append(indent).append(strut.toString()).append("\n");
                }
                logger.log(level, sb.toString());
            }

            super.perform();
        }
    }

    private boolean isCollinearWith(Connector ball) {
        return isCollinear(ball.getLocation());
    }

    private boolean isCollinearWith(Strut strut) {
        return isCollinear(strut.getLocation())
                && isCollinear(strut.getEnd());
    }

    private boolean isCollinear(AlgebraicVector vec) {
        return AlgebraicVector.areCollinear(vec, vector1, vector2);
    }

    @Override
    protected String getXmlElementName() {
        return "SelectCollinear";
    }

    private static final String strVectorsFromSelectionATTR = "vectorsFromSelection";

    @Override
    protected void getXmlAttributes(Element element) {
        DomUtils.addAttribute(element, strVectorsFromSelectionATTR, Boolean.toString(vectorsFromSelection));
        if( !vectorsFromSelection ) {
            // don't persist the vectors if they are derived from the selection
            DomUtils.addAttribute(element, "vector1", vector1.toParsableString());
            DomUtils.addAttribute(element, "vector2", vector2.toParsableString());
        }
    }

    @Override
    protected void setXmlAttributes(Element xml, XmlSaveFormat format) throws Failure {
        vectorsFromSelection = Boolean.parseBoolean(xml.getAttribute(strVectorsFromSelectionATTR));
        if(!vectorsFromSelection) {
            this.vector1 = format.parseRationalVector(xml, "vector1");
            this.vector2 = format.parseRationalVector(xml, "vector2");
        }
    }
}
