import javax.swing.JMenuItem;

/**
 * ���j���[�I���ƃ����N���������N���X�ɃC���v�������g����C���^�[�t�F�[�X
 */
public interface MenuLink
{
	/**
	 * ���j���[��I�������Ƃ��ɌĂ΂��
	 * @param isIncluded JMenuItem.menuSelectionChanged�̃p�����[�^
	 * @param sender ���M���̃��j���[�A�C�e��
	 */
	public void changeSelectMenu(boolean isIncluded, JMenuItem sender);
}
