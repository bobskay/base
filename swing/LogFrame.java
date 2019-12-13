package wang.wangby.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;

public class LogFrame extends JFrame {
    private JTabbedPane jTabbedpane = new JTabbedPane();// 存放选项卡的组件
    private String[] tabNames = { "选项1", "选项2" };
    ImageIcon icon = createImageIcon("images/middle.gif");
    public LogFrame(){
       // addDir();
        addTable();
    }

    private void addTable() {
        JTabbedPane jTabbedpane=new JTabbedPane();
        int i = 0;
        // 第一个标签下的JPanel
        JPanel jpanelFirst = new JPanel();
        // jTabbedpane.addTab(tabNames[i++],icon,creatComponent(),"first");//加入第一个页面
       // jTabbedpane.addTab(tabNames[i++], icon, jpanelFirst, "first");// 加入第一个页面

        jTabbedpane.addTab("title1",icon,createTable(1));
        jTabbedpane.setMnemonicAt(0, KeyEvent.VK_0);// 设置第一个位置的快捷键为0

        // 第二个标签下的JPanel
        JPanel jpanelSecond = new JPanel();
        //jTabbedpane.addTab(tabNames[i++], icon, jpanelSecond, "second");// 加入第一个页面

        jTabbedpane.addTab("title1",icon,createTable(2));
        jTabbedpane.setMnemonicAt(1, KeyEvent.VK_1);// 设置快捷键为1
        setLayout(new GridLayout(1, 1));
        add(jTabbedpane,BorderLayout.CENTER);


    }

    private Component createTable(int index) {
        String[] n1 = { "111111111111111\n\n11111111111111111111111111111111111111111111111111111111", "", "", "", "", "", "", "", "", "", "", "", "" };
        Object[][] p = { n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1,
                n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1, n1,
                n1, };
        String[] n = { "序号", "姓名", "学号", "语文", "数学", "英语", "政治", "历史", "地理", "物理", "化学", "生物", "总分" };

        DefaultTableModel  defaultTableModel = new DefaultTableModel(p, n); // 用双数组创建DefaultTableModel对象
      JTable  table = new JTable(defaultTableModel);// 创建表格组件
        JTableHeader head = table.getTableHeader(); // 创建表格标题对象
        head.setPreferredSize(new Dimension(head.getWidth(), 35));// 设置表头大小
        head.setFont(new Font("楷体", Font.PLAIN, 18));// 设置表格字体
        //table.setRowHeight(18);// 设置表格行宽

        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);// 以下设置表格列宽
        TableColumn column=null;
        for (int i = 0; i < 13; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(50);
            }
        }

        DefaultTableCellRenderer ter = new DefaultTableCellRenderer()// 设置表格间隔色
        {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                // table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                if (row % 2 == 0)
                    setBackground(Color.pink);
                else if (row % 2 == 1)
                    setBackground(Color.white);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        for (int i = 0; i < 13; i++) {
            table.getColumn(n[i]).setCellRenderer(ter);
        }

        JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);// 创建滚动条组件，默认滚动条始终出现，初始化列表组件


        setTitle("育才中学初一.4班期末考试成绩表");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return scrollPane;

    }

    private void addDir() {
        DefaultListModel mode=new DefaultListModel();
        mode.addElement("aaa");
        mode.addElement("bbb");
        mode.addElement("cccccc");
        mode.addElement("ccc");
        JList jList=new JList(mode);

        add(jList, BorderLayout.WEST);
    }

    public static void main(String[] args) {
        LogFrame frame=new LogFrame();
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private ImageIcon createImageIcon(String path) {

        URL url = LogFrame.class.getResource(path);
        if (url == null) {
            System.out.println("the image " + path + " is not exist!");
            return null;
        }
        return new ImageIcon(url);
    }

}
