package glyph.test.simple;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jini.glyph.ServiceUI;

import net.jini.core.lookup.ServiceItem;
@ServiceUI
public class HelloPanel extends JPanel {

    Hello service = null;
    private JTextField nameEntry;
    private JLabel actualResponse;
    
    public HelloPanel(ServiceItem svItem){
        super();
        service = (Hello) svItem.service;
        init(svItem);
    }
    
    private void init(ServiceItem svItem){
        this.service = (Hello) svItem.service;
        setSize(500,500);
        setLayout(new BorderLayout());
      
        this.setVisible(true);
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JLabel nameLabel = new JLabel("Name:");
        nameEntry = new JTextField();
        JLabel responseLabel = new JLabel("Response");
        actualResponse = new JLabel("<Not yet called>");
        GridBagConstraints gc;
         gc = new GridBagConstraints();
         gc.gridx = 0;
         gc.gridy=0;
         gc.insets = new Insets(12,12,0,0);
         p.add(nameLabel, gc);
         gc = new GridBagConstraints();
         gc.gridx = 1;
         gc.gridy=0;
         gc.fill = GridBagConstraints.BOTH;
         gc.insets = new Insets(6,12,0,12);
         p.add(nameEntry, gc);
         gc = new GridBagConstraints();
         gc.gridx = 0;
         gc.gridy=1;
         gc.insets = new Insets(12,6,12,0);
         p.add(responseLabel, gc);
         
         gc = new GridBagConstraints();
         gc.gridx = 1;
         gc.gridy=1;
         gc.insets = new Insets(6,6,12,12);
         p.add(actualResponse, gc);
         JButton goButton = new JButton("Go");
         gc = new GridBagConstraints();
         gc.gridx = 1;
         gc.gridy=2;
         gc.anchor = GridBagConstraints.EAST;
         gc.insets = new Insets(6,6,12,12);
         p.add(goButton, gc);
         goButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 try {
                    actualResponse.setText(service.sayHello(nameEntry.getText()));
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
         });
         add(p, BorderLayout.CENTER);
    }
}
