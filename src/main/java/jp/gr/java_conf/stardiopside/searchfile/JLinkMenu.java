package jp.gr.java_conf.stardiopside.searchfile;

import javax.swing.Action;
import javax.swing.JMenu;

/**
 * メニュー選択とリンクさせるJMenu
 */
@SuppressWarnings("serial")
public class JLinkMenu extends JMenu {

    private MenuHintListener listener;
    private String hint;

    public JLinkMenu(MenuHintListener listener) {
        super();
        this.listener = listener;
    }

    public JLinkMenu(Action a, MenuHintListener listener) {
        super(a);
        this.listener = listener;
    }

    public JLinkMenu(String s, MenuHintListener listener) {
        super(s);
        this.listener = listener;
    }

    public JLinkMenu(String s, boolean b, MenuHintListener listener) {
        super(s, b);
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
