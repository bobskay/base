package wang.wangby.swing.component;

import javax.swing.*;

public class MyFrame extends JFrame {

    public void showFrame(){
        this.setSize(800,600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
