// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExporterHolder.java

package org.jini.glyph.chalice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jini.glyph.chalice.builder.ProxyCreator;

import net.jini.export.Exporter;
import net.jini.jeri.InvocationLayerFactory;
import net.jini.jeri.ServerEndpoint;

// Referenced classes of package utilities20.export:
//            ExporterDefinition

class ExporterHolder
    implements ExporterDefinition
{

    public ExporterHolder(ServerEndpoint se, InvocationLayerFactory ilf, Class exporterClass, ProxyCreator creator)
    {
        this.se = null;
        this.ilf = null;
        this.creator = null;
        this.exporterClass = null;
        this.creator = creator;
        this.se = se;
        this.ilf = ilf;
        this.exporterClass = exporterClass;
    }

    public ProxyCreator getCreator()
    {
        return creator;
    }

    public Exporter getExporter()
    {
        try
        {
            Constructor c = exporterClass.getConstructor(new Class[] {
                net.jini.jeri.ServerEndpoint.class, net.jini.jeri.InvocationLayerFactory.class
            });
            Exporter exp = (Exporter)c.newInstance(new Object[] {
                se, ilf
            });
            return exp;
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch(NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch(InstantiationException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch(InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private ServerEndpoint se;
    private InvocationLayerFactory ilf;
    private ProxyCreator creator;
    private Class exporterClass;
}
