package burp.ui;

import burp.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URL;

public class CustomTab extends JPanel implements ITab {
    private JSplitPane splitPane; // 主分割面板
    private JTable table; // 显示 HTTP 记录的表格
    private CustomTableModel customTableModel; // 表格数据模型
    private JTextArea requestTextArea; // 显示请求内容的文本区域
    private JTextArea responseTextArea; // 显示响应内容的文本区域
    private IBurpExtenderCallbacks callbacks; // Burp 回调接口
    private IExtensionHelpers helpers; // Burp 辅助方法接口
    private static String[] columnNames = {"Method", "URL", "Status Code","length","messageInfo"};
    // 设置高亮
    private int highlightedRowIndex = -1;
    public CustomTab(IBurpExtenderCallbacks callbacks, IExtensionHelpers helpers) {
        this.callbacks = callbacks;
        this.helpers = helpers;
        customTableModel = new CustomTableModel();
        table = new JTable(customTableModel);

//        //设置选择模式。以下列表描述了接受的选择模式：只能选择一行！
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//        requestTextArea = new JTextArea();
//        responseTextArea = new JTextArea();
//
//        // 创建下半部分的分割面板
//        JSplitPane lowerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        lowerSplitPane.setLeftComponent(new JScrollPane(requestTextArea));
//        lowerSplitPane.setRightComponent(new JScrollPane(responseTextArea));
//        lowerSplitPane.setResizeWeight(0.5);
//
//        // 创建上半部分的表格滚动面板
//        JScrollPane topScrollPane = new JScrollPane(table);
//
//        //创建主分割面板了，左右分布HORIZONTAL_SPLIT和上下分布VERTICAL_SPLIT
//        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//
//        splitPane.setBottomComponent(lowerSplitPane);
//        splitPane.setTopComponent(topScrollPane);//给上半部分添加有table的滚动panel
//
//        // 调整下半部分占 UI 界面的 2/3
//        //splitPane.setResizeWeight(1.0/3);
//
//        // 设置文本区域的边框
//        requestTextArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//        responseTextArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

//        // 添加表格模型监听器
//        tableModel.addTableModelListener(new TableModelListener() {
//            @Override
//            public void tableChanged(TableModelEvent e) {
//                callbacks.printOutput("监听到表格变化,变化类型0 update;1 insert : "+e.getType());
//                callbacks.printOutput("e.getType() == TableModelEvent.UPDATE : "+(e.getType() == TableModelEvent.UPDATE));
//                if (e.getType() == TableModelEvent.INSERT) {
//
//                    int firstRow = 0;
//                    int columnIndexToCompare = 3; // 要比较的列索引
//
//                    // 获取新添加行的值
//                    int newRow = e.getFirstRow();
//                    callbacks.printOutput("获取新添加行的行数 : "+newRow);
//                    Object newValue = table.getModel().getValueAt(newRow, columnIndexToCompare);
//                    callbacks.printOutput("获取新添加行的值 : "+newValue);
//
//                    // 获取第一行的值进行比较
//                    Object firstValue = table.getModel().getValueAt(firstRow, columnIndexToCompare);
//                    callbacks.printOutput("获取第一行的值进行比较 : "+firstValue);
//
//                    if (newValue != null && firstValue != null && newValue.equals(firstValue)) {
//                        callbacks.printOutput("新增行的第" + (columnIndexToCompare + 1) + "列的值与第一行相同");
//                        SwingUtilities.invokeLater(() -> {
//                            callbacks.printOutput("// 执行变色逻辑");
//                            highlightRow(table, firstRow);
//                        });
//                    }
//                }
//            }
//        });

    }
    @Override
    public String getTabCaption() {
        return "JX-CSRF";
    }

    @Override
    public Component getUiComponent() {
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // 上半部分：表格
        //JPanel topPanel = new JPanel(new BorderLayout());
        JScrollPane topPanel = new JScrollPane(table);
        //topPanel.add(scrollPane, BorderLayout.CENTER);
        //mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.setTopComponent(topPanel);

        // 下半部分：左右布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // 左侧：请求预览区域
        JTextArea requestPreview = new JTextArea();
        requestPreview.setEditable(false);
        JScrollPane requestScrollPane = new JScrollPane(requestPreview);
        splitPane.setLeftComponent(requestScrollPane);

        // 右侧：响应预览区域
        JTextArea responsePreview = new JTextArea();
        responsePreview.setEditable(false);
        JScrollPane responseScrollPane = new JScrollPane(responsePreview);
        splitPane.setRightComponent(responseScrollPane);

        // 设置分割器大小
        splitPane.setDividerLocation(0.5);

        // 添加分割面板到主面板的中央区域
        //mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.setBottomComponent(splitPane);
        mainPanel.setDividerLocation(0.5);

        //设置选择模式。以下列表描述了接受的选择模式：只能选择一行！
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 添加事件监听器以更新请求/响应预览文本区域
        table.getModel().addTableModelListener(e -> {

            if (e.getType() == TableModelEvent.INSERT) {
                int row = e.getFirstRow();
                // 检查行索引是否有效
                if (row >= 0 && row < table.getModel().getRowCount()) {
                    RequestResponseData data = customTableModel.getDataAt(row);
                    System.out.println("更新请求/响应预览文本区域的行号: " + row);
                    // 更新预览区域内容
                    SwingUtilities.invokeLater(() -> {
                        try {
                            requestPreview.setText(helpers.bytesToString(data.getRequest()));
                            responsePreview.setText(helpers.bytesToString(data.getResponse()));
                        } catch (NullPointerException npe) {
                            // 如果data对象或其内部属性为null，避免空指针异常并打印错误信息
                            callbacks.printError("Failed to update preview: " + npe.getMessage());
                        }
                    });
                }

            }
        });

        //添加切换http记录监听
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            // 获取选定的 HTTP 记录,重写监听选择的逻辑
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("监听http记录切换： "+e.getValueIsAdjusting());
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        System.out.println("获取选定的 HTTP 记录,重写监听选择的行号: "+selectedRow);
                        RequestResponseData message = customTableModel.getDataAt(selectedRow);
                        SwingUtilities.invokeLater(() -> {
//                            requestPreview.setText(truncateAndPreview(data.getRequestContent()));
//                            responsePreview.setText(truncateAndPreview(data.getResponseContent()));
                            // 更新文本区域的内容
                            requestPreview.setText(helpers.bytesToString(message.getRequest()));
                            responsePreview.setText(helpers.bytesToString(message.getResponse()));
                        });
                    }
                }
            }
        });

        // 添加表格模型监听器
        customTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                callbacks.printOutput("监听到表格变化,变化类型0 update;1 insert : "+e.getType());
                callbacks.printOutput("e.getType() == TableModelEvent.UPDATE : "+(e.getType() == TableModelEvent.UPDATE));
                if (e.getType() == TableModelEvent.INSERT) {

                    int firstRow = 0;
                    int columnIndexToCompare = 3; // 要比较的列索引

                    // 获取新添加行的值
                    int newRow = e.getFirstRow();
                    callbacks.printOutput("获取新添加行的行数 : "+newRow+1);
                    Object newValue = table.getModel().getValueAt(newRow, columnIndexToCompare);
                    callbacks.printOutput("获取新添加行的值 : "+newValue);

                    // 获取第一行的值进行比较
                    Object firstValue = table.getModel().getValueAt(firstRow, columnIndexToCompare);
                    callbacks.printOutput("获取第一行的值进行比较 : "+firstValue);

                    if (newRow!=0 && newValue != null && firstValue != null && newValue.equals(firstValue)) {
                        callbacks.printOutput("新增行的第" + (columnIndexToCompare + 1) + "列的值与第一行相同");
                        SwingUtilities.invokeLater(() -> {
                            callbacks.printOutput("// 执行变色逻辑");
                            highlightRow(table, newRow);
                        });
                    }
                }
            }
        });

        //mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    public void addRecord(String method, String url, short statusCode, int length,IHttpRequestResponse messageInfo) {
        RequestResponseData requestResponseData = new RequestResponseData(method, url, statusCode, length, messageInfo.getRequest(), messageInfo.getResponse());
        SwingUtilities.invokeLater(() -> {
            callbacks.printOutput("// 更新UI的操作");
            // 插入新行到表格模型
            customTableModel.addData(requestResponseData);
        });
    }

    // 辅助方法：设置指定行的背景色
    public void highlightRow(JTable table, int rowIndex) {
        System.out.println("设置指定行的背景色 指定行： "+rowIndex);
        HighlightableRenderer renderer = new HighlightableRenderer();
        renderer.setHighlightedRowIndex(rowIndex);
        if (rowIndex > 0 && rowIndex < table.getRowCount()) {
            for (int colIndex = 0; colIndex < table.getColumnCount(); colIndex++) {
                table.getColumnModel().getColumn(colIndex).setCellRenderer(renderer);
            }
        }

        this.highlightedRowIndex = rowIndex;

        table.repaint();

    }

}
