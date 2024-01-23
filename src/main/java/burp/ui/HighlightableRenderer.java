package burp.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 自定义颜色渲染器类
 * zzt
 * 使用方法：
 *     其他类写个函数：
 *     private int highlightedRowIndex = -1;
 *     //JTable  rowIndex要高亮的行
 *     public void highlightRow(JTable table, int rowIndex) {
 *         System.out.println("设置指定行的背景色 指定行： "+rowIndex);
 *         HighlightableRenderer renderer = new HighlightableRenderer();
 *         renderer.setHighlightedRowIndex(rowIndex);
 *         if (rowIndex > 0 && rowIndex < table.getRowCount()) {
 *             for (int colIndex = 0; colIndex < table.getColumnCount(); colIndex++) {
 *                 table.getColumnModel().getColumn(colIndex).setCellRenderer(renderer);
 *             }
 *         }
 *         this.highlightedRowIndex = rowIndex;
 *         table.repaint();
 *     }
 *
 * 实际上，在自定义渲染器类（HighlightableRenderer）中，
 * 我们已经通过在 getTableCellRendererComponent 方法中判断行索引来实现高亮效果了。
 * 当你设置 highlightedRowIndex 后，表格会在下次重绘时自动调用渲染器的 getTableCellRendererComponent 方法，从而更新指定行的颜色。
 * 所以，只需要确保正确设置了 highlightedRowIndex 并且在需要时调用 table.repaint() 以触发表格重绘即可
 */
public class HighlightableRenderer extends DefaultTableCellRenderer {
    private int highlightedRowIndex;
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        // 检查是否要高亮当前行（假设你想高亮的是参数传入的那一行）
        if (row == highlightedRowIndex) {
            c.setBackground(Color.RED);
        } else {
            c.setBackground(UIManager.getColor("Table.background")); // 设置为默认背景色
        }
        return c;
    }
    public void setHighlightedRowIndex(int rowIndex) {
        this.highlightedRowIndex = rowIndex;
    }

}
