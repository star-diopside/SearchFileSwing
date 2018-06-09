package jp.gr.java_conf.stardiopside.searchfile;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 * メニュー選択とリンクさせるJMenuItem
 */
@SuppressWarnings("serial")
public class JLinkMenuItem extends JMenuItem {

    private MenuHintListener listener;
    private String hint;

    public JLinkMenuItem(MenuHintListener listener) {
        super();
        this.listener = listener;
    }

    public JLinkMenuItem(Action a, MenuHintListener listener) {
        super(a);
        this.listener = listener;
    }

    public JLinkMenuItem(Icon icon, MenuHintListener listener) {
        super(icon);
        this.listener = listener;
    }

    public JLinkMenuItem(String text, MenuHintListener listener) {
        super(text);
        this.listener = listener;
    }

    public JLinkMenuItem(String text, Icon icon, MenuHintListener listener) {
        super(text, icon);
        this.listener = listener;
    }

    public JLinkMenuItem(String text, int mnemonic, MenuHintListener listener) {
        super(text, mnemonic);
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
