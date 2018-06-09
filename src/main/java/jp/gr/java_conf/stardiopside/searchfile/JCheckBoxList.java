package jp.gr.java_conf.stardiopside.searchfile;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

/**
 * リスト要素に{@link JCheckBox}を使用した{@link JList}
 */
@SuppressWarnings("serial")
public class JCheckBoxList extends JList<JCheckBox> {

    public JCheckBoxList() {
        super();
        initialize();
    }

    public JCheckBoxList(JCheckBox[] listData) {
        super(listData);
        initialize();
    }

    public JCheckBoxList(ListModel<JCheckBox> dataModel) {
        super(dataModel);
        initialize();
    }

    public JCheckBoxList(Vector<? extends JCheckBox> listData) {
        super(listData);
        initialize();
    }

    private void initialize() {
        setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            SwingUtilities.updateComponentTreeUI(value);
            return value;
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    JCheckBox checkbox = getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    repaint();
                }
            }
        });
    }
}
