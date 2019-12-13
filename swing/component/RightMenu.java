package wang.wangby.swing.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightMenu extends JPopupMenu {

    public void addMenu(String text, ActionListener actionListener){
        JMenuItem  item = new JMenuItem(text);
        item.addActionListener(actionListener);
        Font font=new Font("Serief", Font.PLAIN, 22);
        item.setFont(font);
        add(item);
    }

    public void bind(Component component){
        RightMenu menu=this;
        component.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()==MouseEvent.BUTTON3) {
                    //弹出右键菜单
                    menu.show(component, e.getX(), e.getY());
                }
            }
        });
    }
}
