package glyph.test.article;

import java.rmi.RemoteException;

import javax.swing.JEditorPane;

import org.jini.glyph.Exportable;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;

@Exportable
public class NormalChatListener implements ChatListener {
    
    private JEditorPane editor;
    
    public NormalChatListener(JEditorPane editor){
        this.editor = editor;
    }
    
    public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
        // TODO Auto-generated method stub
        editor.setText(editor.getText() + "\n" + theEvent.getSource().toString());
    }
}
