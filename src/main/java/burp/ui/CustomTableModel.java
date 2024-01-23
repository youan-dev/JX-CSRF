package burp.ui;


import burp.IHttpRequestResponse;
import burp.RequestResponseData;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomTableModel extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = {"Method", "URL", "Status Code", "Length", "Request Preview", "Response Preview"};
    private List<RequestResponseData> dataList;

    // 初始化列表
    public CustomTableModel() {
        super(COLUMN_NAMES, 0); // 使用列名数组和初始行数为0来初始化DefaultTableModel
        dataList = new ArrayList<>();
    }

    // 添加新的请求/响应数据到表格模型中
    public void addData(RequestResponseData data) {
        dataList.add(data);
        Object[] rowData = {
                data.getMethod(),
                data.getUrl(),
                data.getStatusCode(),
                data.getLength(),
                "request",
                "response"
        };

        //int lastIndex = getRowCount();
        this.addRow(rowData); // 使用DefaultTableModel的addRow方法添加新行

        //fireTableRowsInserted(lastIndex, lastIndex); // 这个方法在DefaultTableModel.addRow后会自动触发，通常不需要手动调用
        //通知表格数据模型已插入新行，参数分别是：开始插入行的索引、结束插入行的索引
    }

    // 获取指定行的数据
    public RequestResponseData getDataAt(int rowIndex) {
        System.out.println("获取指定行： "+rowIndex);
        System.out.println("dataList :"+dataList);
        return dataList.get(rowIndex);
    }
}
