
//(c) Copyright 2013, Scott Vorthmann.

package com.vzome.core.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.HeptagonField;
import com.vzome.core.algebra.PentagonField;
import com.vzome.core.algebra.RootThreeField;
import com.vzome.core.algebra.RootTwoField;
import com.vzome.core.algebra.SnubDodecField;
import com.vzome.core.commands.Command;
import com.vzome.core.commands.CommandAxialSymmetry;
import com.vzome.core.commands.CommandCentralSymmetry;
import com.vzome.core.commands.CommandCentroid;
import com.vzome.core.commands.CommandConjugate;
import com.vzome.core.commands.CommandHide;
import com.vzome.core.commands.CommandImportVEFData;
import com.vzome.core.commands.CommandMidpoint;
import com.vzome.core.commands.CommandMirrorSymmetry;
import com.vzome.core.commands.CommandPolygon;
import com.vzome.core.commands.CommandQuaternionSymmetry;
import com.vzome.core.commands.CommandSymmetry;
import com.vzome.core.commands.CommandTauDivision;
import com.vzome.core.commands.CommandTetrahedralSymmetry;
import com.vzome.core.commands.CommandTranslate;
import com.vzome.core.commands.CommandUniformH4Polytope;
import com.vzome.core.commands.CommandVanOss600Cell;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.exporters.DaeExporter;
import com.vzome.core.exporters.DxfExporter;
import com.vzome.core.exporters.Exporter3d;
import com.vzome.core.exporters.HistoryExporter;
import com.vzome.core.exporters.JsonExporter;
import com.vzome.core.exporters.LiveGraphicsExporter;
import com.vzome.core.exporters.OffExporter;
import com.vzome.core.exporters.OpenGLExporter;
import com.vzome.core.exporters.POVRayExporter;
import com.vzome.core.exporters.PartsListExporter;
import com.vzome.core.exporters.PdbExporter;
import com.vzome.core.exporters.RulerExporter;
import com.vzome.core.exporters.STEPExporter;
import com.vzome.core.exporters.SecondLifeExporter;
import com.vzome.core.exporters.SegExporter;
import com.vzome.core.exporters.StlExporter;
import com.vzome.core.exporters.VRMLExporter;
import com.vzome.core.exporters.VefExporter;
import com.vzome.core.math.RealVector;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.DodecagonalSymmetry;
import com.vzome.core.math.symmetry.IcosahedralSymmetry;
import com.vzome.core.math.symmetry.OctahedralSymmetry;
import com.vzome.core.math.symmetry.QuaternionicSymmetry;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.render.Color;
import com.vzome.core.render.Colors;
import com.vzome.core.render.Shapes;
import com.vzome.core.viewing.AbstractShapes;
import com.vzome.core.viewing.DodecagonalShapes;
import com.vzome.core.viewing.ExportedVEFShapes;
import com.vzome.core.viewing.Lights;
import com.vzome.core.viewing.OctahedralShapes;
import com.vzome.core.viewing.ScriptedShapes;

public class Application
{
    private final Map fields = new HashMap();

    private final Map<Symmetry,List<Shapes>> mStyles = new HashMap(); // key: Symmetry, value: List of
    
    private final Map symmetries = new HashMap();
    
    private final Map mCommands = new HashMap();

    private final Colors mColors;

    private final Command.FailureChannel failures;

    private Map exporters = new HashMap();

    private Lights mLights = new Lights();
    
    static Logger logger = Logger.getLogger( "com.vzome.core.editor" );

    public Application( boolean enableCommands, Command.FailureChannel failures, Properties props )
    {
        this .failures = failures;
        
        if ( props == null )
        {
        	props = loadDefaults();
        }
        mColors = new Colors( props );
        File prefsFolder = new File( System.getProperty( "user.home" ), "vZome-Preferences" );

        for ( int i = 1; i <= 3; i++ ) {
            Color color = mColors .getColorPref( "light.directional." + i );
            Vector3f dir = new Vector3f( mColors .getVectorPref( "direction.light." + i ) );
            mLights.addDirectionLight( color, dir );
        }
        mLights .setAmbientColor( mColors .getColorPref( "light.ambient" ) );
        mLights .setBackgroundColor( mColors .getColor( Colors.BACKGROUND ) );

        AbstractShapes defaultShapes = null;
        
        AlgebraicField field = new PentagonField();
        AlgebraicField pentField = field;
        fields .put( field .getName(), field );
        {
            IcosahedralSymmetry symmetry = new IcosahedralSymmetry( field, "solid connectors" );
            symmetries .put( symmetry.getName(), symmetry );
            QuaternionicSymmetry H4 = new QuaternionicSymmetry( "H_4", "com/vzome/core/math/symmetry/H4roots.vef", field );
            field .addQuaternionSymmetry( H4 );
            QuaternionicSymmetry H4_ROT = new QuaternionicSymmetry( "H4_ROT", "com/vzome/core/math/symmetry/H4roots-rotationalSubgroup.vef", field );
            field .addQuaternionSymmetry( H4_ROT );
            QuaternionicSymmetry T2 = new QuaternionicSymmetry( "2T", "com/vzome/core/math/symmetry/binaryTetrahedralGroup.vef", field );
            field .addQuaternionSymmetry( T2 );
            mStyles.put( symmetry, new ArrayList<Shapes>() );
            defaultShapes = new ScriptedShapes( prefsFolder, "default", "solid connectors", symmetry );
            addStyle( defaultShapes );
            addStyle( new ScriptedShapes( prefsFolder, "lifelike", "lifelike", symmetry, defaultShapes ) );
            defaultShapes =  new ScriptedShapes( prefsFolder, "tiny", "tiny connectors", symmetry );
            addStyle( defaultShapes );
            addStyle( new ExportedVEFShapes( prefsFolder, "dodecs", "small dodecahedra", symmetry, defaultShapes ) );
            addStyle( new ScriptedShapes( prefsFolder, "noTwist", "no-twist 121 zone", symmetry ) );
            addStyle( new ScriptedShapes( prefsFolder, "vienne", "Vienne 121 zone", symmetry ) );
            
            if ( enableCommands ) {
                mCommands .put( "icosasymm-golden", new CommandSymmetry( symmetry ) );
                mCommands .put( "tetrasymm-golden", new CommandTetrahedralSymmetry( symmetry ) );
                mCommands .put( "axialsymm-icosa", new CommandAxialSymmetry( symmetry ) );
                mCommands .put( "h4symmetry", new CommandQuaternionSymmetry( H4, H4 ) );
                mCommands .put( "h4rotations", new CommandQuaternionSymmetry( H4_ROT, H4_ROT ) );
                mCommands .put( "IxTsymmetry", new CommandQuaternionSymmetry( H4, T2 ) );
                mCommands .put( "TxTsymmetry", new CommandQuaternionSymmetry( T2, T2 ) );
                mCommands .put( "vanOss600cell", new CommandVanOss600Cell() );
                mCommands .put( "runZomicScript", new Boolean( true ) );
                mCommands .put( "runPythonScript", new Boolean( true ) );
                for ( int p = 0x1; p <= 0xF; p++ ) {
                    String dynkin = Integer.toString( p, 2 );
                    dynkin = "0000" .substring( dynkin.length() ) + dynkin;
                    mCommands .put( "h4polytope_" + dynkin, new CommandUniformH4Polytope( field, H4, p ) );
                }
            }

            Symmetry octaSymm = new OctahedralSymmetry( field, "blue", "trapezoids" );
            symmetries .put( octaSymm.getName(), octaSymm );
            mStyles.put( octaSymm, new ArrayList<Shapes>() );
            defaultShapes =  new ExportedVEFShapes( prefsFolder, "octahedral", "trapezoids", octaSymm, null );
            addStyle( defaultShapes );
            addStyle( new ExportedVEFShapes( prefsFolder, "octahedralFast", "small octahedra", octaSymm, null ) );
            addStyle( new ExportedVEFShapes( prefsFolder, "octahedralRealistic", "vZome logo", octaSymm, defaultShapes ) );

            if ( enableCommands ) {
                mCommands .put( "octasymm-golden", new CommandSymmetry( octaSymm ) );
                mCommands .put( "axialsymm-octa", new CommandAxialSymmetry( octaSymm ) );
            }
        }
        field = new RootTwoField();
        fields .put( field .getName(), field );
        {
            Symmetry symmetry = new OctahedralSymmetry( field, "blue", "small octahedra" );
            symmetries .put( symmetry.getName(), symmetry );
            mStyles.put( symmetry, new ArrayList<Shapes>() );
            defaultShapes =  new ExportedVEFShapes( prefsFolder, "rootTwoSmall", "small octahedra", "small connectors", symmetry );
            addStyle( defaultShapes );
            addStyle( new ExportedVEFShapes( prefsFolder, "rootTwo", "Tesseractix", symmetry, defaultShapes ) );
            addStyle( new ExportedVEFShapes( prefsFolder, "rootTwoBig", "ornate", symmetry, defaultShapes ) );

            if ( enableCommands ) {
                mCommands .put( "octasymm-roottwo", new CommandSymmetry( symmetry ) );
                mCommands .put( "tetrasymm-roottwo", new CommandTetrahedralSymmetry( symmetry ) );
                mCommands .put( "axialsymm-roottwo", new CommandAxialSymmetry( symmetry ) );
            }

            /*
             * This is not really a different symmetry, but it uses different colors and default shapes for the orbits.
             * 
             * Actually, it may be a different field, even, since I've learned that Synestructics does not really
             * scale by root-two, but by doubling!  That means that octagons are still irregular in this system.
             * It also raises some interesting questions about algebraic fields, and vZome's assumptions about them.
             * Unlike the golden field, one cannot scale down in this field without using fractions; basically, this
             * field is just the Integers!
             */
            final AlgebraicField fField = field;
            symmetry = new OctahedralSymmetry( field, "orange", "Synestructics" )
            {
                public String getName()
                {
                    return "synestructics";
                }

                protected void createOtherOrbits()
            	{
                    AlgebraicVector v = new AlgebraicVector( fField .one(), fField .one(), fField .one() );
                    createZoneOrbit( "yellow", 0, 4, v, true );

                    AlgebraicNumber sqrt2 = fField .createPower( 1 );
                    AlgebraicNumber half = fField .createRational( new int[]{ 1, 2 } );
                    v = new AlgebraicVector( sqrt2, sqrt2, fField .zero() ) .scale( half );
                    createZoneOrbit( "magenta", 1, 8, v, true );

                    v = new AlgebraicVector( fField .one(), fField .one(), fField .one() .plus( fField .one() ) );
                    createZoneOrbit( "brown", 0, NO_ROTATION, v, true );
            	}
            };
            symmetries .put( symmetry.getName(), symmetry );
            mStyles.put( symmetry, new ArrayList() );
            defaultShapes =  new ExportedVEFShapes( prefsFolder, "rootTwoSmall", "small octahedra", symmetry, null );
            addStyle( new ExportedVEFShapes( prefsFolder, "rootTwoSmall", "small octahedra", symmetry, defaultShapes ) );
            addStyle( new ExportedVEFShapes( prefsFolder, "rootTwo", "Synestructics", symmetry, defaultShapes ) );
            addStyle( new ExportedVEFShapes( prefsFolder, "rootTwoBig", "ornate", symmetry, defaultShapes ) );

            if ( enableCommands ) {
                mCommands .put( "octasymm-synestructics", new CommandSymmetry( symmetry ) );
                mCommands .put( "tetrasymm-synestructics", new CommandTetrahedralSymmetry( symmetry ) );
                mCommands .put( "axialsymm-synestructics", new CommandAxialSymmetry( symmetry ) );
            }
        }
        field = new RootThreeField();
        fields .put( field .getName(), field );
        fields .put( "dodecagon", field );
        {
            Symmetry symmetry = new OctahedralSymmetry( field, "blue", "small octahedra" );
            symmetries .put( symmetry.getName(), symmetry );
            mStyles.put( symmetry, new ArrayList<Shapes>() );
            addStyle( new ExportedVEFShapes( prefsFolder, "rootThreeOctaSmall", "small octahedra", "small connectors", symmetry ) );
            addStyle( new OctahedralShapes( "octahedral", "octahedra", symmetry ) );

            if ( enableCommands ) {
                mCommands .put( "octasymm-rootthree", new CommandSymmetry( symmetry ) );
                mCommands .put( "tetrasymm-rootthree", new CommandTetrahedralSymmetry( symmetry ) );
                mCommands .put( "axialsymm-rootthree", new CommandAxialSymmetry( symmetry ) );
                mCommands .put( "ghostsymm24cell", "ghostsymm24cell" );
            }
        }
        {
            Symmetry symmetry = new DodecagonalSymmetry( field, "prisms" );
            symmetries .put( symmetry.getName(), symmetry );
            mStyles.put( symmetry, new ArrayList<Shapes>() );
            addStyle( new ExportedVEFShapes( prefsFolder, "dodecagon3d", "prisms", symmetry, null ) );
            addStyle( new DodecagonalShapes( "dodecagonal", "hexagons", "flat hexagons", symmetry ) );
            if ( enableCommands ) {
                mCommands .put( "dodecagonsymm", "dodecagonsymm" );
            }
        }
        
        field = new SnubDodecField( pentField );
        fields .put( field .getName(), field );
        {
            IcosahedralSymmetry symmetry = new IcosahedralSymmetry( field, "solid connectors" );
            mStyles.put( symmetry, new ArrayList<Shapes>() );
            defaultShapes = new ScriptedShapes( prefsFolder, "default", "solid connectors", symmetry );
            addStyle( defaultShapes );
            addStyle( new ScriptedShapes( prefsFolder, "lifelike", "lifelike", symmetry, defaultShapes ) );
            addStyle( new ScriptedShapes( prefsFolder, "tiny", "tiny connectors", symmetry ) );

            mCommands .put( "icosasymm-snubDodec", new CommandSymmetry( symmetry ) );
            mCommands .put( "tetrasymm-snubDodec", new CommandTetrahedralSymmetry( symmetry ) );
            mCommands .put( "axialsymm-snubDodec", new CommandAxialSymmetry( symmetry ) );
        }

        field = new HeptagonField();
        fields .put( field .getName(), field );
        OctahedralSymmetry symmetry = new OctahedralSymmetry( field, "blue", "octahedra" );
        mStyles.put( symmetry, new ArrayList<Shapes>() );
        addStyle( new OctahedralShapes( "octahedral", "octahedra", symmetry ) );
        mCommands .put( "octasymm-heptagon", new CommandSymmetry( symmetry ) );
        mCommands .put( "tetrasymm-heptagon", new CommandTetrahedralSymmetry( symmetry ) );
        mCommands .put( "axialsymm-heptagon", new CommandAxialSymmetry( symmetry ) );
        if ( enableCommands ) {
            mCommands .put( "pointsymm", new CommandCentralSymmetry() );
            mCommands .put( "mirrorsymm", new CommandMirrorSymmetry() );
            mCommands .put( "translate", new CommandTranslate() );
            mCommands .put( "centroid", new CommandCentroid() );
            mCommands .put( "hideball", new CommandHide() );
            mCommands .put( "panel", new CommandPolygon() );
            mCommands .put( "tauDivide", new CommandTauDivision() );
            mCommands .put( "conjugate", new CommandConjugate() );
            mCommands .put( "midpoint", new CommandMidpoint() );
            mCommands .put( "import.vef", new CommandImportVEFData() );
        }

//        field = new Heptagon6Field();
//        fields .put( field .getName(), field );
//        symmetry = new OctahedralSymmetry( field, "blue", "octahedra" );
//        mStyles.put( symmetry, new ArrayList<Shapes>() );
//        addStyle( new OctahedralShapes( "octahedral", "octahedra", symmetry ) );
        
        this .exporters .put( "pov", new POVRayExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "opengl", new OpenGLExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "dae", new DaeExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "LiveGraphics", new LiveGraphicsExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "json", new JsonExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "step", new STEPExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "vrml", new VRMLExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "off", new OffExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "2life", new SecondLifeExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "vef", new VefExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "partslist", new PartsListExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "size", new RulerExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "stl", new StlExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "dxf", new DxfExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "pdb", new PdbExporter( null, this .mColors, this .mLights, null ) );
        this .exporters .put( "seg", new SegExporter( null, this .mColors, this .mLights, null ) );
        
        this .exporters .put( "history", new HistoryExporter( null, this .mColors, this .mLights, null ) );
    }

    private void addStyle( Shapes shapes )
    {
        Symmetry symm = shapes.getSymmetry();
        List<Shapes> styles = (List<Shapes>) mStyles.get( symm );
        styles .add( shapes );
    }
    
    public DocumentModel loadDocument( InputStream bytes, boolean render ) throws Exception
    {
        Document xml = null;

        // parse the bytes as XML
        try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory .newInstance();
        	factory .setNamespaceAware( true );
        	DocumentBuilder builder = factory .newDocumentBuilder();
        	xml = builder .parse( bytes );
            bytes.close();
        } catch ( SAXException e ) {
//            String errorCode = "XML is bad:  " + e.getMessage() + " at line " + e.getLineNumber() + ", column "
//                    + e.getColumnNumber();
            logger .severe( e .getMessage() );
            throw e; 
        }

        Element element = xml .getDocumentElement();
        String tns = element .getNamespaceURI();
        XmlSaveFormat format = XmlSaveFormat .getFormat( tns );

        if ( format == null )
        {
            String version = element.getAttribute( "version" );
            String edition = element.getAttribute( "edition" );
            if ( edition .isEmpty() )
                edition = "vZome";
            String error = "Unknown " + edition + " file format.";
            if ( ! version .isEmpty() )
                error += "\n " + Version.edition + " " + Version.label + " cannot open files\ncreated by "
                        + edition + " " + version;
            logger .severe( error );
            throw new IllegalStateException( error );
        }
        else
            logger .fine( "supported format: " + tns );
        
        
        String fieldName = element .getAttribute( "field" );
        if ( fieldName .isEmpty() )
            // field is qualified in the Zome interchange format
            fieldName = element .getAttributeNS( XmlSaveFormat.CURRENT_FORMAT, "field" );
        if ( fieldName .isEmpty() )
            fieldName = "golden";
        AlgebraicField field = (AlgebraicField) fields .get( fieldName );
        
        return new DocumentModel( field, failures, element, this );
    }

	public DocumentModel createDocument( String fieldName )
	{
		AlgebraicField field = (AlgebraicField) fields .get( fieldName );
		return new DocumentModel( field, failures, null, this );
	}

	public DocumentModel importDocument( String content, String extension )
	{
		String fieldName = "golden";
		// TODO: use fieldName from VEF input
		AlgebraicField field = (AlgebraicField) fields .get( fieldName );
		DocumentModel result = new DocumentModel( field, failures, null, this );
		result .doScriptAction( extension, content );
		return result;
	}

	public AlgebraicField getField( String name )
	{
		return (AlgebraicField) fields .get( name );
	}
    
    public static void main( String[] args )
    {
    	final float EPSILON = 5E-10f;

        Application app = new Application( true, null, null );
        Symmetry icosa = app .getField( "golden" ) .getSymmetry( "icosahedral" );
        Direction blue = icosa .getDirection( "blue" );
        
        Map blueAngles = new HashMap();
        RealVector baseRv = null;
        Axis baseZone = null;
        for (Iterator blues = blue .getAxes(); blues.hasNext(); )
        {
            Axis zone = (Axis) blues .next();
            RealVector rv = zone .normal() .toRealVector() .normalize();
            if ( baseRv == null )
            {
                baseRv = rv;
                baseZone = zone;
            }
            else
            {
                float cos = Math .abs( (float) rv .dot( baseRv ) );
                // don't want zero angle (zones are the same)
                if ( cos > EPSILON && cos != 1f )
                {
                    double angle = 180f *  Math .acos( cos ) / Math .PI;
                    blueAngles .put( new Float( angle ), new Axis[]{ baseZone, zone } );
                }
            }
        }
        for (Iterator angles = blueAngles .keySet() .iterator(); angles.hasNext(); ) {
            Float angle = (Float) angles.next();
            System .out. print( angle + "  " );
            Axis[] zones = (Axis[]) blueAngles .get( angle );
            System .out .print( zones[0] .getOrientation() + " " );
            System .out .println( zones[1] .getOrientation() + " " );
        }
    }

	public List<Shapes> getGeometries( Symmetry symmetry )
	{
		return mStyles .get( symmetry );
	}

	public Set getFieldNames()
	{
		return fields .keySet();
	}

	public Shapes getGeometry( Symmetry symmetry, String styleName )
	{
	    List<Shapes> geoms = mStyles .get( symmetry );
	    for ( Iterator iterator = geoms.iterator(); iterator.hasNext(); ) {
            Shapes shapes = (Shapes) iterator.next();
            if ( shapes .getName() .equals( styleName ) )
                return shapes;
        }
		return null;
	}

	public static Properties loadDefaults()
	{
        String defaultRsrc = "com/vzome/core/editor/defaultPrefs.properties";
        Properties defaults = new Properties();
        try {
            ClassLoader cl = Application.class.getClassLoader();
            InputStream in = cl.getResourceAsStream( defaultRsrc );
            if ( in != null )
            	defaults .load( in );
        } catch ( IOException ioe ) {
            System.err.println( "problem reading default preferences: " + defaultRsrc );
        }
        return defaults;
	}

	public Colors getColors()
	{
		return this .mColors;
	}

	public Map getCommands()
	{
		return this .mCommands;
	}

    public Exporter3d getExporter( String format )
    {
        return (Exporter3d) this .exporters .get( format );
    }
}
