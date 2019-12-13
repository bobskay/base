package wang.wangby.swing;

import wang.wangby.swing.component.MyFrame;
import wang.wangby.swing.component.MyScrollPane;
import wang.wangby.swing.component.MyTab;
import wang.wangby.swing.component.RightMenu;
import wang.wangby.swing.component.table.ScrollTable;

import java.util.*;
import java.util.function.Function;

public class MainFrame extends MyFrame {

    RightMenu rightMenu;
    List<MyScrollPane> scrollPanes;
    MyTab myTab;
    RowInfo rowInfo;

    public MainFrame(List<String> list,RowInfo row){
        rowInfo=row;
        rightMenu=new RightMenu();
        scrollPanes=new ArrayList<>();
        myTab=new MyTab();
        for(String title:list){
            MyScrollPane pane=ScrollTable.newInstance(row,new ArrayList<>());
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
            List data=new ArrayList();
            Map map=new HashMap<>();
            map.put("1","1");
            map.put("2","22");
            map.put("3","333");
            map.put("4","444");
            for(Object function:rowInfo.getFunctions()){
                Function<Map,String> fun=(Function<Map,String> )function;
                data.add(fun.apply(map));
            }

            pane.addRow(data);
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
        RowInfo row=new RowInfo();
        String[] name={"a","b","c","d"};
        row.setNames(Arrays.asList(name));

        Function<Map,String> fn=map->"11111111111111";
        List fun=new ArrayList();
        fun.add(fn);
        fun.add(fn);
        fun.add(fn);
        fun.add(fn);
        row.setFunctions(fun);


        MainFrame mainFrame=new MainFrame(list,row);
        mainFrame.showFrame();
    }
}
