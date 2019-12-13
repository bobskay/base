package wang.wangby.swing;

import wang.wangby.swing.component.MyFrame;
import wang.wangby.swing.component.MyScrollPane;
import wang.wangby.swing.component.MyTab;
import wang.wangby.swing.component.RightMenu;
import wang.wangby.swing.component.table.ScrollTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFrame extends MyFrame {

    RightMenu rightMenu;
    List<MyScrollPane> scrollPanes;
    MyTab myTab;

    public MainFrame(List<String> list){
        rightMenu=new RightMenu();
        scrollPanes=new ArrayList<>();
        myTab=new MyTab();
        for(String title:list){
            MyScrollPane pane=ScrollTable.newInstance(list,new ArrayList<>());
            myTab.addTab(title,pane);
            scrollPanes.add(pane);
        }

        this.add(myTab);
        createRightMenu();
    }



    private void createRightMenu() {
        rightMenu.addMenu("清空",e->{
            MyScrollPane pane=(MyScrollPane) myTab.getSelectedComponent();
            pane.clear();
        });
        rightMenu.addMenu("bbbb",e->{
            MyScrollPane pane=(MyScrollPane) myTab.getSelectedComponent();
            String [] s={"1","2","3333333333333333"};
            pane.addRow(Arrays.asList(s));
        });
        for(MyScrollPane pane:scrollPanes){
            rightMenu.bind(pane.getTable());
            rightMenu.bind(pane);
        }

    }

    public static void main(String[] args) {
        List<String> list=new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        MainFrame mainFrame=new MainFrame(list);
        mainFrame.showFrame();
    }
}
