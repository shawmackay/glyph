// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExporterDefinition.java

package org.jini.glyph.chalice;

import org.jini.glyph.chalice.builder.ProxyCreator;

import net.jini.export.Exporter;

interface ExporterDefinition
{

    public abstract ProxyCreator getCreator();

    public abstract Exporter getExporter();
}
