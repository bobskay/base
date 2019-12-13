package wang.wangby.swing.component;

import wang.wangby.swing.component.table.MyTable;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

public class MyScrollPane extends JScrollPane {
    MyTable myTable;
    boolean scroll = false;

    public MyTable getTable(){
        return myTable;
    }

    public MyScrollPane(MyTable myTable) {
        super(myTable);
        this.myTable = myTable;
        new Thread(() -> {
            while (true) {
                try {
                    if (scroll) {
                        for (int i = 0; i < 5; i++) {
                            Thread.sleep(100);
                            toEnd();
                        }
                        scroll=false;
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void addRow(List row) {
        myTable.addRow(new Vector(row));
        scroll = true;
    }

    public void toEnd() {
        JScrollBar sBar = getVerticalScrollBar();
        sBar.setValue(sBar.getMaximum());
    }

    public void clear() {
        myTable.clear();
    }
}
