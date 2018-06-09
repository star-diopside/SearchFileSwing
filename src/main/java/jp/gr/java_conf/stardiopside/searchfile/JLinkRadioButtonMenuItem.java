package jp.gr.java_conf.stardiopside.searchfile;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;

/**
 * メニュー選択とリンクさせるJRadioButtonMenuItem
 */
@SuppressWarnings("serial")
public class JLinkRadioButtonMenuItem extends JRadioButtonMenuItem {

    private MenuHintListener listener;
    private String hint;

    public JLinkRadioButtonMenuItem(MenuHintListener listener) {
        super();
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(String text, MenuHintListener listener) {
        super(text);
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(String text, boolean selected, MenuHintListener listener) {
        super(text, selected);
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(Action a, MenuHintListener listener) {
        super(a);
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(Icon icon, MenuHintListener listener) {
        super(icon);
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(Icon icon, boolean selected, MenuHintListener listener) {
        super(icon, selected);
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(String text, Icon icon, MenuHintListener listener) {
        super(text, icon);
        this.listener = listener;
    }

    public JLinkRadioButtonMenuItem(String text, Icon icon, boolean selected, MenuHintListener listener) {
        super(text, icon, selected);
        this.listener = listener;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void menuSelectionChanged(boolean isIncluded) {
        super.menuSelectionChanged(isIncluded);
        listener.changeSelectMenu(this, isIncluded, hint);
    }
}
