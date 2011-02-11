// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SimpleCreator.java

package org.jini.glyph.test;

import java.io.PrintStream;
import java.rmi.Remote;

import org.jini.glyph.chalice.builder.ProxyCreator;

import net.jini.id.Uuid;

public class SimpleCreator
    implements ProxyCreator
{

    public SimpleCreator()
    {
    }

    public Remote create(Remote in, Uuid ID)
    {
        System.out.println("Creating a simple proxy");
        return in;
    }
}
