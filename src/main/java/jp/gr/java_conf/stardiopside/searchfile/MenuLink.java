package jp.gr.java_conf.stardiopside.searchfile;

import javax.swing.JMenuItem;

/**
 * メニュー選択とリンクさせたいクラスにインプリメントするインターフェース
 */
public interface MenuLink
{
	/**
	 * メニューを選択したときに呼ばれる
	 * @param isIncluded JMenuItem.menuSelectionChangedのパラメータ
	 * @param sender 送信元のメニューアイテム
	 */
	public void changeSelectMenu(boolean isIncluded, JMenuItem sender);
}
