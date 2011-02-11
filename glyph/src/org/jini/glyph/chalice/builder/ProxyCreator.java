// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProxyCreator.java

package org.jini.glyph.chalice.builder;

import java.rmi.Remote;
import net.jini.id.Uuid;

public interface ProxyCreator
{

    public abstract Remote create(Remote remote, Uuid uuid);
}
