package jp.gr.java_conf.stardiopside.searchfile;

import javax.swing.*;

/**
 * メニュー選択とリンクさせるJRadioButtonMenuItem
 */
public class JLinkRadioButtonMenuItem extends JRadioButtonMenuItem
{
	protected MenuLink link;

	public JLinkRadioButtonMenuItem(MenuLink link){
		super();
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(String text, MenuLink link){
		super(text);
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(String text, boolean selected, MenuLink link){
		super(text, selected);
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(Action a, MenuLink link){
		super(a);
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(Icon icon, MenuLink link){
		super(icon);
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(Icon icon, boolean selected, MenuLink link){
		super(icon, selected);
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(String text, Icon icon, MenuLink link){
		super(text, icon);
		this.link = link;
	}

	public JLinkRadioButtonMenuItem(String text, Icon icon, boolean selected, MenuLink link){
		super(text, icon, selected);
		this.link = link;
	}

	public void menuSelectionChanged(boolean isIncluded){
		super.menuSelectionChanged(isIncluded);
		link.changeSelectMenu(isIncluded, this);
	}
}
