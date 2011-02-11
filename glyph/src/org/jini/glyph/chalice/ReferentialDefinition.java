// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReferentialDefinition.java

package org.jini.glyph.chalice;

import org.jini.glyph.chalice.builder.ProxyCreator;

import net.jini.config.*;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.http.HttpServerEndpoint;

// Referenced classes of package utilities20.export:
//            ExporterDefinition

public class ReferentialDefinition
    implements ExporterDefinition
{

    public ReferentialDefinition(String reference, ProxyCreator creator)
    {
        this.reference = reference;
        this.creator = creator;
    }

    public ProxyCreator getCreator()
    {
        return creator;
    }

    public Exporter getExporter()
    {
        String parts[] = reference.split("/");
        String configfilename = parts[0];
        try
        {
            Configuration config = ConfigurationProvider.getInstance(new String[] {
                parts[0]
            });
            Exporter exp = (Exporter)config.getEntry(parts[1], parts[2], net.jini.export.Exporter.class, new BasicJeriExporter(HttpServerEndpoint.getInstance(0), new BasicILFactory()));
            return exp;
        }
        catch(ConfigurationException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String reference;
    private ProxyCreator creator;
}
