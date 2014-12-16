
//(c) Copyright 2011, Scott Vorthmann.

package com.vzome.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vzome.api.Application;
import com.vzome.api.Document;
import com.vzome.api.Exporter;

public class ExportTool extends HttpServlet
{
	Properties props;
	Application app;
	
    public void init( ServletConfig config )
	throws ServletException
    {
    	super.init( config );

        app = new Application();
    }

    public void doGet( HttpServletRequest req, HttpServletResponse res )
        throws ServletException, IOException
    {
        InputStream bytes = null;
        PrintWriter out = null;
        try {
            URL vZomeFile = new URL( req .getQueryString() );
            String format = req .getServletPath() .substring( 1 );
            
            Exporter exporter = this .app .getExporter( format );

            // set header field first
            res .setContentType( exporter .getContentType() );

            out = res .getWriter();
            
            bytes = vZomeFile .openStream();
            
            props .setProperty( "export.format", format );

            Document model = app .loadDocument( bytes );
	        exporter .doExport( model, out, 1080, 1920 );
	        
		} catch ( Exception e ) {
			e .printStackTrace();
			throw new ServletException( e );
		} finally {
		    if ( bytes != null )
		        bytes .close();
		    if ( out != null )
		        out .close();
		}
    }

    public String getServletInfo()
    {
        return "Exports a JSON file from a vZome model, for use in vZome webview";
    }
}