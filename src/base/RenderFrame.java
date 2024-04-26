package base;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;    

public class RenderFrame extends JFrame {
    public RenderFrame() {
        setTitle("Render Frame");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        this.setUndecorated(false);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        add(new RenderPanel());
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                /* try {
                    if(Server.server != null)
                    Server.server.close();
                    if(Client.client != null)
                    Client.client.close();
                    if(VoiceClient.client != null)
                    VoiceClient.client.close();
                    if(VoiceServer.server != null)
                    VoiceServer.server.close();
                } catch (IOException e1) {
                }    
                System.out.println("Fenster wird geschlossen");
                System.exit(0); */
            }
        });
    }

}