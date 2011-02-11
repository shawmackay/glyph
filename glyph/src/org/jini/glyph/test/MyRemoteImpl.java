// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MyRemoteImpl.java

package org.jini.glyph.test;

import java.io.PrintStream;
import java.rmi.RemoteException;

// Referenced classes of package utilities20.export.test:
//            MyRemoteIntf

public class MyRemoteImpl
    implements MyRemoteIntf
{

    public MyRemoteImpl()
    {
    }

    public void sayHello(String name)
        throws RemoteException
    {
        System.out.println("Hello " + name);
    }

    protected void finalize()
        throws Throwable
    {
        super.finalize();
        System.out.println("Finalized!");
    }
}
