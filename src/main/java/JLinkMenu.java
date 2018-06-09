import javax.swing.*;

/**
 * メニュー選択とリンクさせるJMenu
 */
public class JLinkMenu extends JMenu
{
	protected MenuLink link;

	public JLinkMenu(MenuLink link){
		super();
		this.link = link;
	}

	public JLinkMenu(Action a, MenuLink link){
		super(a);
		this.link = link;
	}

	public JLinkMenu(String s, MenuLink link){
		super(s);
		this.link = link;
	}

	public JLinkMenu(String s, boolean b, MenuLink link){
		super(s, b);
		this.link = link;
	}

	public void menuSelectionChanged(boolean isIncluded){
		super.menuSelectionChanged(isIncluded);
		link.changeSelectMenu(isIncluded, this);
	}
}
