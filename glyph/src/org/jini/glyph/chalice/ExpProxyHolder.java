// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExpProxyHolder.java

package org.jini.glyph.chalice;

import java.rmi.Remote;
import java.util.logging.Logger;
import net.jini.export.Exporter;
import net.jini.id.Uuid;

class ExpProxyHolder
{
    private class DoUnexport
        implements Runnable
    {

        public void run()
        {
            try
            {
                Thread.sleep(1500L);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            l.finest("Unexporting object " + ID);
            exporterUsed.unexport(true);
            exported = null;
            exporterUsed = null;
            l.finest("Finished clear-up");
        }

        DoUnexport()
        {
        }
    }


    public boolean equals(Object obj)
    {
        if(obj instanceof ExpProxyHolder)
        {
            ExpProxyHolder cmp = (ExpProxyHolder)obj;
            return ID.equals(cmp.getID());
        } else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return ID.hashCode();
    }

    public String toString()
    {
        return ref;
    }

    public ExpProxyHolder(Remote instance, Remote wrappered, Remote exported, Uuid ID, Exporter exporterUsed)
    {
        l = Logger.getLogger("exportermanager");
        this.instance = null;
        this.exported = null;
        this.wrappered = null;
        this.ID = null;
        ref = null;
        this.exporterUsed = null;
        this.instance = instance;
        this.exported = exported;
        this.wrappered = wrappered;
        this.ID = ID;
        ref = "ExpProxyHolder: " + ID.toString();
        this.exporterUsed = exporterUsed;
    }

    public Remote getInstance()
    {
        return instance;
    }

    public Remote getWrapperedProxy()
    {
        return wrappered;
    }

    public Remote getExportedProxy()
    {
        return exported;
    }

    public Uuid getID()
    {
        return ID;
    }

    public void clear()
    {
        instance = null;
        ID = null;
        Thread t = new Thread(new DoUnexport());
        t.start();
    }

    Logger l;
    private Remote instance;
    private Remote exported;
    private Remote wrappered;
    private Uuid ID;
    private String ref;
    private Exporter exporterUsed;




}
