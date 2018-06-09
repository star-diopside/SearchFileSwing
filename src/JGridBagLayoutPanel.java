import javax.swing.*;
import java.awt.*;

/**
 * レイアウトマネージャがGridBagLayout固定のJPanel
 */
public class JGridBagLayoutPanel extends JPanel
{
	GridBagLayout layout = new GridBagLayout();
	GridBagConstraints gridConstraints = new GridBagConstraints();

	public JGridBagLayoutPanel(){
		super();
		init();
	}

	public JGridBagLayoutPanel(boolean isDoubleBuffered){
		super(isDoubleBuffered);
		init();
	}

	/**
	 * 初期化処理のためにコンストラクタから呼ばれる
	 */
	private void init(){
		super.setLayout(layout);
		gridConstraints.gridx = 0;
		gridConstraints.gridy = 0;
		gridConstraints.gridwidth = 1;
		gridConstraints.gridheight = 1;
	}

	/**
	 * このメソッドは何も行わない (レイアウトマネージャを変更できない)
	 */
	public void setLayout(LayoutManager mgr){
	}

	public Component add(Component comp){
		layout.setConstraints(comp, gridConstraints);
		nextGridX();
		return super.add(comp);
	}

	public Component add(String name, Component comp){
		layout.setConstraints(comp, gridConstraints);
		nextGridX();
		return super.add(name, comp);
	}

	public Component add(Component comp, int index){
		layout.setConstraints(comp, gridConstraints);
		nextGridX();
		return super.add(comp, index);
	}

	public void add(Component comp, Object constraints){
		layout.setConstraints(comp, gridConstraints);
		nextGridX();
		super.add(comp, constraints);
	}

	public void add(Component comp, Object constraints, int index){
		layout.setConstraints(comp, gridConstraints);
		nextGridX();
		super.add(comp, constraints, index);
	}

	/**
	 * 複数のパラメータを指定してコンポーネントを追加する
	 */
	public Component add(Component comp, int gridwidth, int gridheight, int anchor){
		setGridWidth(gridwidth);
		setGridHeight(gridheight);
		setGridAnchor(anchor);
		layout.setConstraints(comp, gridConstraints);
		nextGridX();
		return super.add(comp);
	}

	/**
	 * 複数のパラメータを指定してコンポーネントを追加する
	 * インセットはパラメータに渡された値に一時的に変更されるが、このメソッド終了時に元に戻される
	 */
	public Component add(Component comp, int gridwidth, int gridheight, int anchor, Insets insets){
		// 現在のインセットを一時領域に保存し、新たなインセットを適用する
		Insets tmp = getGridInsets();
		setGridInsets(insets);

		// コンポーネントを追加する
		Component retComp = add(comp, gridwidth, gridheight, anchor);

		// インセットを元に戻す
		setGridInsets(tmp);

		return retComp;
	}

	/**
	 * 現在のセルの位置を設定する
	 */
	public void setGridCell(int gridx, int gridy){
		gridConstraints.gridx = gridx;
		gridConstraints.gridy = gridy;
	}

	/**
	 * コンポーネントを配置するセルを列方向に1つ進める
	 */
	public void nextGridX(){
		gridConstraints.gridx++;
	}

	/**
	 * コンポーネントを配置するセルを行方向に1つ進め、列方向をリセットする
	 */
	public void nextGridY(){
		gridConstraints.gridx = 0;
		gridConstraints.gridy++;
	}

	/**
	 * コンポーネントの表示領域の1行のセル数を取得する
	 */
	public int getGridWidth(){
		return gridConstraints.gridwidth;
	}

	/**
	 * コンポーネントの表示領域の1行のセル数を設定する
	 */
	public void setGridWidth(int gridwidth){
		gridConstraints.gridwidth = gridwidth;
	}

	/**
	 * コンポーネントの表示領域の1列のセル数を取得する
	 */
	public int getGridHeight(){
		return gridConstraints.gridheight;
	}

	/**
	 * コンポーネントの表示領域の1列のセル数を設定する
	 */
	public void setGridHeight(int gridheight){
		gridConstraints.gridheight = gridheight;
	}

	/**
	 * コンポーネントの配置場所を取得する
	 */
	public int getGridAnchor(){
		return gridConstraints.anchor;
	}

	/**
	 * コンポーネントの配置場所を設定する
	 */
	public void setGridAnchor(int anchor){
		gridConstraints.anchor = anchor;
	}

	/**
	 * コンポーネントのインセットを取得する
	 */
	public Insets getGridInsets(){
		return gridConstraints.insets;
	}

	/**
	 * コンポーネントのインセットを設定する
	 */
	public void setGridInsets(Insets insets){
		gridConstraints.insets = insets;
	}
}
