package glyph.test.article;

import java.rmi.RemoteException;

import net.jini.core.event.EventRegistration;

public interface ChatBoard extends java.rmi.Remote{
    public void sendMessage(String userID, String message) throws RemoteException;
    public EventRegistration registerListener(String userID, ChatListener listener) throws RemoteException;
}
