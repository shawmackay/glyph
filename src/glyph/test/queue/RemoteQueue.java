/*
 * RemoteQueue.java
 *
 * Created on 20 September 2006, 12:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package glyph.test.queue;

import java.rmi.Remote;

import net.jini.admin.Administrable;

/**
 *
 * @author calum
 */
public interface RemoteQueue extends Queue, Remote{
    
}
