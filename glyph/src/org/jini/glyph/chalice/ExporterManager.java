// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExporterManager.java

package org.jini.glyph.chalice;

import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.Collection;

import org.jini.glyph.chalice.builder.ProxyCreator;

import net.jini.id.Uuid;
import net.jini.jeri.InvocationLayerFactory;
import net.jini.jeri.ServerEndpoint;

public interface ExporterManager
{

    public abstract Remote exportProxy(Remote remote, String s, Uuid uuid)
        throws ExportException;
    
    
    public abstract ProxyPair exportProxyAndPair(Remote remote, String s, Uuid uuid)
    throws ExportException;
    
   // public Remote exportObject(Object ob, String exporterTypeName,Uuid ID) throws ExportException;

    public abstract Object getCreatedProxy(Uuid uuid);

    public abstract Object getExportedProxy(Uuid uuid);

    public abstract void addNamedExporter(String s, ServerEndpoint serverendpoint, InvocationLayerFactory invocationlayerfactory, ProxyCreator proxycreator);

    public abstract Collection getAllExportedUnder(String s);

    public abstract void relinquish(String s, Uuid uuid);

    public abstract void relinquishAll(String s);

    public abstract void relinquishAll();

    public abstract void scheduleRelinquish(String s, Uuid uuid, long l);

    public abstract boolean contains(Uuid uuid);

    public abstract boolean containsIn(Uuid uuid, String s);

    public abstract int getCount();

    public abstract int getCount(String s);
    
    public abstract String[] getExporterNames();
}
