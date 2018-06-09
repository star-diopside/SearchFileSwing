import javax.swing.*;
import javax.swing.border.*;

import java.awt.Component;
import java.awt.Rectangle;

import java.io.Serializable;

/**
 * コンポーネントにJCheckBoxを使用したListCellRenderer
 */
public class CheckListCellRenderer extends JCheckBox
	implements ListCellRenderer, Serializable
{
	protected static Border noFocusBorder;

	public CheckListCellRenderer(){
		super();
		if(noFocusBorder == null){
			noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		}
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
		SwingUtilities.updateComponentTreeUI(this);

		setComponentOrientation(list.getComponentOrientation());
		if(cellHasFocus){
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}else{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if(value instanceof Icon){
			setIcon((Icon)value);
			setText("");
		}else{
			setIcon(null);
			setText(value == null ? "" : value.toString());
		}

		setSelected(isSelected);
		setEnabled(list.isEnabled());
		setBorder(cellHasFocus ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

		return this;
	}

	public void validate(){
	}

	public void revalidate(){
	}

	public void repaint(long tm, int x, int y, int width, int height){
	}

	public void repaint(Rectangle r){
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue){
		if(propertyName=="text")
			super.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, byte oldValue, byte newValue){
	}

	public void firePropertyChange(String propertyName, char oldValue, char newValue){
	}

	public void firePropertyChange(String propertyName, short oldValue, short newValue){
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue){
	}

	public void firePropertyChange(String propertyName, long oldValue, long newValue){
	}

	public void firePropertyChange(String propertyName, float oldValue, float newValue){
	}

	public void firePropertyChange(String propertyName, double oldValue, double newValue){
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue){
	}

	public static class UIResource extends DefaultListCellRenderer
		implements javax.swing.plaf.UIResource
	{
	}
}
