package wang.wangby.swing.component;

import wang.wangby.swing.Css;

import javax.swing.*;
import java.awt.*;

public class MyTab extends JTabbedPane {

    public void addTab(String title, Component component) {
        super.addTab(title, component);
    }

    public MyTab(){
        this.setFont(Css.title);
    }
}
