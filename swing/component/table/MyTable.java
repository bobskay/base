package wang.wangby.swing.component.table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

public class MyTable extends JTable {

    DefaultTableModel tableModel;
    Vector vData;
    Vector vName;

    public MyTable(Vector vData, Vector vName) {
        this.vData = vData;
        this.vName = vName;
        tableModel = new DefaultTableModel(vData, vName);
        setModel(tableModel);
        setDefaultRenderer(Object.class, new TableCellTextAreaRenderer());
        setStyle();
    }

    private void setStyle() {
        JTableHeader head = getTableHeader(); // 创建表格标题对象
        head.setPreferredSize(new Dimension(head.getWidth(), 35));// 设置表头大小
        head.setFont(new Font("楷体", Font.PLAIN, 18));// 设置表格字体
        this.setBorder(new EmptyBorder(-5, 0, -5, 0));

        // 设置字体颜色
        setForeground(Color.red);
        // 设置被选中的行前景（被选中时字体的颜色）
        setSelectionForeground(Color.green);
        // 设置被选中的行背景
        setSelectionBackground(Color.BLACK);
        // 设置网格颜色
        setGridColor(Color.yellow);
        // 设置是否显示网格
        setShowGrid(false);
        // 水平方向网格线是否显示
        setShowHorizontalLines(true);
        // 竖直方向网格线是否显示
        setShowVerticalLines(false);


        this.getColumnModel().getColumn(0).setWidth(80);
    }

    public void addRow(Vector row) {
        vData.add(row);
        tableModel = new DefaultTableModel(vData, vName);

        this.setModel(tableModel);
        this.getWidth();
        JTableHeader header = getTableHeader();
        TableColumn column =  this.getColumnModel().getColumn(0);
        column.setWidth(100);
        header.setResizingColumn(column);

        column =  this.getColumnModel().getColumn(1);
        column.setWidth(200);
        header.setResizingColumn(column);

        column =  this.getColumnModel().getColumn(2);
        column.setWidth(100);
        header.setResizingColumn(column);

        column =  this.getColumnModel().getColumn(3);
        column.setWidth(50);
        header.setResizingColumn(column);

    }

    public void clear() {
        tableModel = new DefaultTableModel(new Vector(), vName);

    }

    public void fix() {
        JTable myTable=this;
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable, column.getIdentifier()
                            , false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
                        myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column); // 此行很重要
            column.setWidth(width + myTable.getIntercellSpacing().width);
        }
    }
}
