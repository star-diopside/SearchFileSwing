import javax.swing.*;
import java.awt.*;

/**
 * ���C�A�E�g�}�l�[�W����GridBagLayout�Œ��JPanel
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
	 * �����������̂��߂ɃR���X�g���N�^����Ă΂��
	 */
	private void init(){
		super.setLayout(layout);
		gridConstraints.gridx = 0;
		gridConstraints.gridy = 0;
		gridConstraints.gridwidth = 1;
		gridConstraints.gridheight = 1;
	}

	/**
	 * ���̃��\�b�h�͉����s��Ȃ� (���C�A�E�g�}�l�[�W����ύX�ł��Ȃ�)
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
	 * �����̃p�����[�^���w�肵�ăR���|�[�l���g��ǉ�����
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
	 * �����̃p�����[�^���w�肵�ăR���|�[�l���g��ǉ�����
	 * �C���Z�b�g�̓p�����[�^�ɓn���ꂽ�l�Ɉꎞ�I�ɕύX����邪�A���̃��\�b�h�I�����Ɍ��ɖ߂����
	 */
	public Component add(Component comp, int gridwidth, int gridheight, int anchor, Insets insets){
		// ���݂̃C���Z�b�g���ꎞ�̈�ɕۑ����A�V���ȃC���Z�b�g��K�p����
		Insets tmp = getGridInsets();
		setGridInsets(insets);

		// �R���|�[�l���g��ǉ�����
		Component retComp = add(comp, gridwidth, gridheight, anchor);

		// �C���Z�b�g�����ɖ߂�
		setGridInsets(tmp);

		return retComp;
	}

	/**
	 * ���݂̃Z���̈ʒu��ݒ肷��
	 */
	public void setGridCell(int gridx, int gridy){
		gridConstraints.gridx = gridx;
		gridConstraints.gridy = gridy;
	}

	/**
	 * �R���|�[�l���g��z�u����Z����������1�i�߂�
	 */
	public void nextGridX(){
		gridConstraints.gridx++;
	}

	/**
	 * �R���|�[�l���g��z�u����Z�����s������1�i�߁A����������Z�b�g����
	 */
	public void nextGridY(){
		gridConstraints.gridx = 0;
		gridConstraints.gridy++;
	}

	/**
	 * �R���|�[�l���g�̕\���̈��1�s�̃Z�������擾����
	 */
	public int getGridWidth(){
		return gridConstraints.gridwidth;
	}

	/**
	 * �R���|�[�l���g�̕\���̈��1�s�̃Z������ݒ肷��
	 */
	public void setGridWidth(int gridwidth){
		gridConstraints.gridwidth = gridwidth;
	}

	/**
	 * �R���|�[�l���g�̕\���̈��1��̃Z�������擾����
	 */
	public int getGridHeight(){
		return gridConstraints.gridheight;
	}

	/**
	 * �R���|�[�l���g�̕\���̈��1��̃Z������ݒ肷��
	 */
	public void setGridHeight(int gridheight){
		gridConstraints.gridheight = gridheight;
	}

	/**
	 * �R���|�[�l���g�̔z�u�ꏊ���擾����
	 */
	public int getGridAnchor(){
		return gridConstraints.anchor;
	}

	/**
	 * �R���|�[�l���g�̔z�u�ꏊ��ݒ肷��
	 */
	public void setGridAnchor(int anchor){
		gridConstraints.anchor = anchor;
	}

	/**
	 * �R���|�[�l���g�̃C���Z�b�g���擾����
	 */
	public Insets getGridInsets(){
		return gridConstraints.insets;
	}

	/**
	 * �R���|�[�l���g�̃C���Z�b�g��ݒ肷��
	 */
	public void setGridInsets(Insets insets){
		gridConstraints.insets = insets;
	}
}
