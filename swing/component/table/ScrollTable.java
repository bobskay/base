package wang.wangby.swing.component.table;

import wang.wangby.swing.RowInfo;
import wang.wangby.swing.component.MyScrollPane;

import java.util.List;
import java.util.Vector;

public class ScrollTable {

    public static MyScrollPane newInstance(RowInfo row, List<List> data){
        Vector vData = new Vector();
        Vector vName = new Vector();
        for(Object o :row.getNames()){
            vName.add(o);
        }
        for(List list:data){
            Vector vRow = new Vector();
            for(Object o:list){
                vRow.add(o);
            }
            vData.add(vRow);
        }
        MyTable myTable=new MyTable(vData,vName);
        MyScrollPane scroll = new MyScrollPane(myTable);

        return scroll;
    }
}
