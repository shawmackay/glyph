// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MyRemoteIntf.java

package org.jini.glyph.test;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MyRemoteIntf
    extends Remote
{

    public abstract void sayHello(String s)
        throws RemoteException;
}
