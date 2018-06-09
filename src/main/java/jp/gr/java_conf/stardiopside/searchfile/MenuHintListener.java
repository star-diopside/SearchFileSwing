package jp.gr.java_conf.stardiopside.searchfile;

import javax.swing.JMenuItem;

/**
 * メニュー選択時にヒント情報を通知するリスナー
 */
@FunctionalInterface
public interface MenuHintListener {

    /**
     * メニューが選択された際にヒント情報を通知する。
     * 
     * @param sender 送信元のメニューアイテム
     * @param isIncluded
     *            {@link JMenuItem#menuSelectionChanged(boolean)}に引き渡された{@code isIncluded}パラメータ情報
     * @param hint ヒント情報
     */
    public void changeSelectMenu(JMenuItem sender, boolean isIncluded, String hint);

}
