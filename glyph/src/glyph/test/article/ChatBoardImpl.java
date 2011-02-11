package glyph.test.article;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jini.glyph.Exportable;
import org.jini.glyph.Service;

import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;

@Service
@Exportable
public class ChatBoardImpl implements ChatBoard {
    
    private Map<String,ChatListener> clients = new TreeMap<String,ChatListener>();
    
    private long conversationSeq = 0L;
    
    public ChatBoardImpl(String[] args){
        System.out.println("Created Chat Board");
    }
    
    public EventRegistration registerListener(String userID,ChatListener listener) throws RemoteException {
        // TODO Auto-generated method stub
        clients.put(userID,listener);
        return new EventRegistration(1,  null,null,conversationSeq);
    }

    public void sendMessage(String userID, String message) throws RemoteException{
        // TODO Auto-generated method stub
        conversationSeq++;
        for(Map.Entry entr : clients.entrySet()){
            Thread t = new Thread(new MessageDispatch((ChatListener)entr.getValue(), userID + ": " + message));
            t.start();
        }
    }
    
    public class MessageDispatch implements Runnable{
        
        private ChatListener listen;
        private String message;
        
        public MessageDispatch(ChatListener listener, String message){
            this.listen = listener;
            this.message = message;
        }
        public void run() {
            // TODO Auto-generated method stub
            try {
                listen.notify(new RemoteEvent(message,1L,conversationSeq,null));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnknownEventException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
