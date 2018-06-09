import javax.swing.*;

/**
 * メニュー選択とリンクさせるJMenuItem
 */
public class JLinkMenuItem extends JMenuItem
{
	protected MenuLink link;

	public JLinkMenuItem(MenuLink link){
		super();
		this.link = link;
	}

	public JLinkMenuItem(Action a, MenuLink link){
		super(a);
		this.link = link;
	}

	public JLinkMenuItem(Icon icon, MenuLink link){
		super(icon);
		this.link = link;
	}

	public JLinkMenuItem(String text, MenuLink link){
		super(text);
		this.link = link;
	}

	public JLinkMenuItem(String text, Icon icon, MenuLink link){
		super(text, icon);
		this.link = link;
	}

	public JLinkMenuItem(String text, int mnemonic, MenuLink link){
		super(text, mnemonic);
		this.link = link;
	}

	public void menuSelectionChanged(boolean isIncluded){
		super.menuSelectionChanged(isIncluded);
		link.changeSelectMenu(isIncluded, this);
	}
}
