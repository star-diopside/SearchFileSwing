import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

/**
 * �t�@�C���������s���A�v���P�[�V�����̃t���[��
 */
public class SearchFile extends JFrame
	implements ActionListener, Runnable, MenuLink
{
	JLabel lblDir = new JLabel("�f�B���N�g����");
	JTextField txtDir = new JTextField(21);
	JButton btnDir = new JButton("�f�B���N�g���̎w��");
	JLabel lblFile = new JLabel("�t�@�C����");
	JTextField txtFile = new JTextField(21);
	JRadioButton radioRegular = new JRadioButton("���K�\��");
	JRadioButton radioWildCard = new JRadioButton("���C���h�J�[�h");
	JCheckBox chkCase = new JCheckBox("�啶��/�������̋�ʂ��s��");
	JButton btnSearch = new JButton("�����J�n");
	JButton btnClear = new JButton("���X�g�̃N���A");
	JButton btnCopy = new JButton("�������ʂ��N���b�v�{�[�h�ɃR�s�[");
	JButton btnSelectAll = new JButton("���ׂđI��");
	JButton btnSelectedClear = new JButton("�I������");
	JButton btnDeleteFile = new JButton("�I���t�@�C�����폜");
	JCheckBox chkDelete = new JCheckBox("�폜���Ƀt�@�C�������ݔ��Ɉڂ�");

	JLinkMenu menuFile;
	JLinkMenuItem menuFileExit;
	JLinkMenu menuChange;
	JLinkMenuItem menuChangeCross;
	JLinkMenuItem menuChangeSystem;
	JLinkRadioButtonMenuItem menuChangeMetal;
	JLinkRadioButtonMenuItem menuChangeWin;
	JLinkRadioButtonMenuItem menuChangeMotif;
	JLinkRadioButtonMenuItem menuChangeGTK;
	JLinkRadioButtonMenuItem menuChangeMac;
	JLinkRadioButtonMenuItem menuChangeNimbus;

	DefaultListModel listFileData = new DefaultListModel();
	JList listFile = new JList(listFileData);

	private JLabel labelStatusBar = new JLabel();
	private String strStatusBar = "���f�B";

	// ���b�N�A���h�t�B�[���̃N���X��
	private static final String classMetal = "javax.swing.plaf.metal.MetalLookAndFeel";
	private static final String classWin = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
	private static final String classMotif = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
	private static final String classGTK = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
	private static final String classMac = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
	private static final String classNimbus = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	private boolean flagSearching = false;	// ���݌������ł��邩�������t���O

	public SearchFile(String title){
		super(title);

		initComponents();	// �R���|�[�l���g�̐ݒ�
		addMenuBar();		// ���j���[�o�[�̐ݒ�
		addStatusBar();		// �X�e�[�^�X�o�[�̐ݒ�
	}

	public static void main(String[] args){
		SearchFile frame = new SearchFile("�t�@�C���̌���");

		// �E�B���h�E�̑傫���ƏI������̐ݒ�
		frame.setLocation(10, 10);
		frame.setSize(800, 550);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// �E�B���h�E��\������
		frame.setVisible(true);
	}

	/**
	 * �R���|�[�l���g�̏��������s��
	 * ���̃��\�b�h�̓R���X�g���N�^����Ă΂��
	 */
	private void initComponents(){
		/*
		 * �����̃R���|�[�l���g���܂ރp�l���̃R���g���[���̐ݒ�
		 */
		JGridBagLayoutPanel panelSubComponents = new JGridBagLayoutPanel();

		// �R���|�[�l���g��ǉ�����
		panelSubComponents.setGridInsets(new Insets(2, 8, 2, 8));
		panelSubComponents.add(lblDir, 2, 1, GridBagConstraints.WEST, new Insets(8, 8, 2, 8));
		panelSubComponents.nextGridY();
		panelSubComponents.add(txtDir, 2, 1, GridBagConstraints.CENTER);
		panelSubComponents.nextGridY();
		panelSubComponents.add(btnDir, 2, 1, GridBagConstraints.EAST);
		panelSubComponents.nextGridY();
		panelSubComponents.add(lblFile, 2, 1, GridBagConstraints.WEST);
		panelSubComponents.nextGridY();
		panelSubComponents.add(txtFile, 2, 1, GridBagConstraints.CENTER);
		panelSubComponents.nextGridY();
		panelSubComponents.add(radioRegular, 1, 1, GridBagConstraints.CENTER);
		panelSubComponents.add(radioWildCard, 1, 1, GridBagConstraints.CENTER);
		panelSubComponents.nextGridY();
		panelSubComponents.add(chkCase, 2, 1, GridBagConstraints.EAST);
		panelSubComponents.nextGridY();
		panelSubComponents.add(btnSearch, 1, 1, GridBagConstraints.WEST);
		panelSubComponents.add(btnClear, 1, 1, GridBagConstraints.EAST);
		panelSubComponents.nextGridY();
		panelSubComponents.add(btnCopy, 2, 1, GridBagConstraints.CENTER);
		panelSubComponents.nextGridY();
		panelSubComponents.add(btnSelectAll, 1, 1, GridBagConstraints.WEST, new Insets(24, 8, 2, 8));
		panelSubComponents.add(btnSelectedClear, 1, 1, GridBagConstraints.EAST, new Insets(24, 8, 2, 8));
		panelSubComponents.nextGridY();
		panelSubComponents.add(btnDeleteFile, 2, 1, GridBagConstraints.CENTER);
		panelSubComponents.nextGridY();
		panelSubComponents.add(chkDelete, 2, 1, GridBagConstraints.WEST);

		// �f�[�^��ݒ肷��
		txtDir.setText(new File(".").getAbsoluteFile().getParent());

		// �O���[�v�Ə�����Ԃ�ݒ肷��
		ButtonGroup groupRadio = new ButtonGroup();
		groupRadio.add(radioRegular);
		groupRadio.add(radioWildCard);
		radioRegular.setSelected(true);

		// �A�N�V�������X�i�[��ǉ�����
		btnDir.addActionListener(this);
		btnSearch.addActionListener(this);
		btnClear.addActionListener(this);
		btnCopy.addActionListener(this);
		btnSelectAll.addActionListener(this);
		btnSelectedClear.addActionListener(this);
		btnDeleteFile.addActionListener(this);

		// �c�[���`�b�v��ݒ肷��
		btnDir.setToolTipText("��������f�B���N�g�����w�肷��");
		chkCase.setToolTipText("�t�@�C�����̑啶��/����������ʂ��Č������s��");
		btnSearch.setToolTipText("�������J�n����");
		btnClear.setToolTipText("�������ʂ��N���A����");
		btnCopy.setToolTipText("�������ʂ��N���b�v�{�[�h�ɃR�s�[����");
		btnSelectAll.setToolTipText("���X�g��S�I����Ԃɂ���");
		btnSelectedClear.setToolTipText("���X�g�̑I����Ԃ���������");
		btnDeleteFile.setToolTipText("�I�����ꂽ�t�@�C�����폜����");

		// �f�t�H���g�̃{�^���̐ݒ�
		getRootPane().setDefaultButton(btnSearch);

		JPanel panelComponents = new JPanel();
		panelComponents.setLayout(new BorderLayout());
		panelComponents.add(panelSubComponents, BorderLayout.NORTH);


		/*
		 * ���X�g�R���g���[���̐ݒ�
		 */
		listFile.setCellRenderer(new CheckListCellRenderer());
		JScrollPane scrollList = new JScrollPane(listFile);
		scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


		/*
		 * ���E�̃p�l����z�u
		 */
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panelComponents, BorderLayout.EAST);
		getContentPane().add(scrollList, BorderLayout.CENTER);
	}

	/**
	 * ���j���[�o�[�̐ݒ���s��
	 * ���̃��\�b�h�̓R���X�g���N�^����Ă΂��
	 */
	private void addMenuBar(){
		JMenuBar menuBar = new JMenuBar();

		/*
		 * �t�@�C�����j���[�̐ݒ�
		 */
		menuFile = new JLinkMenu("�t�@�C��(F)", this);
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFileExit = new JLinkMenuItem("�A�v���P�[�V�����̏I��(X)", this);
		menuFileExit.setMnemonic(KeyEvent.VK_X);
		menuFile.add(menuFileExit);
		menuFileExit.addActionListener(this);
		menuBar.add(menuFile);

		/*
		 * Look & Feel ���j���[�̐ݒ�
		 */
		menuChange = new JLinkMenu("Look & Feel", this);
		menuChange.setMnemonic(KeyEvent.VK_L);

		menuChangeCross = new JLinkMenuItem(new ChangeLookAndFeelAction(
			this, UIManager.getCrossPlatformLookAndFeelClassName(),
			"�N���X�v���b�g�t�H�[��(C)"), this);
		menuChangeCross.setMnemonic(KeyEvent.VK_C);
		menuChange.add(menuChangeCross);

		menuChangeSystem = new JLinkMenuItem(new ChangeLookAndFeelAction(
			this, UIManager.getSystemLookAndFeelClassName(),
			"�V�X�e���v���b�g�t�H�[��(S)"), this);
		menuChangeSystem.setMnemonic(KeyEvent.VK_S);
		menuChange.add(menuChangeSystem);

		menuChange.addSeparator();

		menuChangeMetal = new JLinkRadioButtonMenuItem(
			new ChangeLookAndFeelAction(this, classMetal, "Metal Look & Feel"), this);
		menuChangeMetal.setMnemonic(KeyEvent.VK_M);
		menuChangeMetal.setEnabled(isSupportedLookAndFeel(classMetal));
		menuChange.add(menuChangeMetal);

		menuChangeWin = new JLinkRadioButtonMenuItem(
			new ChangeLookAndFeelAction(this, classWin, "Windows Look & Feel"), this);
		menuChangeWin.setMnemonic(KeyEvent.VK_W);
		menuChangeWin.setEnabled(isSupportedLookAndFeel(classWin));
		menuChange.add(menuChangeWin);

		menuChangeMotif = new JLinkRadioButtonMenuItem(
			new ChangeLookAndFeelAction(this, classMotif, "CDE/Motif Look & Feel"), this);
		menuChangeMotif.setMnemonic(KeyEvent.VK_D);
		menuChangeMotif.setEnabled(isSupportedLookAndFeel(classMotif));
		menuChange.add(menuChangeMotif);

		menuChangeGTK = new JLinkRadioButtonMenuItem(
			new ChangeLookAndFeelAction(this, classGTK, "GTK+ Look & Feel"), this);
		menuChangeGTK.setMnemonic(KeyEvent.VK_G);
		menuChangeGTK.setEnabled(isSupportedLookAndFeel(classGTK));
		menuChange.add(menuChangeGTK);

		menuChangeMac = new JLinkRadioButtonMenuItem(
			new ChangeLookAndFeelAction(this, classMac, "Macintosh Look & Feel"), this);
		menuChangeMac.setMnemonic(KeyEvent.VK_A);
		menuChangeMac.setEnabled(isSupportedLookAndFeel(classMac));
		menuChange.add(menuChangeMac);

		menuChangeNimbus = new JLinkRadioButtonMenuItem(
			new ChangeLookAndFeelAction(this, classNimbus, "Nimbus Look & Feel"), this);
		menuChangeNimbus.setMnemonic(KeyEvent.VK_N);
		menuChangeNimbus.setEnabled(isSupportedLookAndFeel(classNimbus));
		menuChange.add(menuChangeNimbus);

		// �O���[�v�̐ݒ���s��
		ButtonGroup groupLAF = new ButtonGroup();
		groupLAF.add(menuChangeMetal);
		groupLAF.add(menuChangeWin);
		groupLAF.add(menuChangeMotif);
		groupLAF.add(menuChangeGTK);
		groupLAF.add(menuChangeMac);
		groupLAF.add(menuChangeNimbus);

		// ���݂̃��b�N�A���h�t�B�[�����烁�j���[�̑I����Ԃ����肷��
		LookAndFeel laf = UIManager.getLookAndFeel();
		if(laf != null){
			String className = laf.getClass().getName();

			if(className.equals(classMetal)){
				menuChangeMetal.setSelected(true);
			}else if(className.equals(classWin)){
				menuChangeWin.setSelected(true);
			}else if(className.equals(classMotif)){
				menuChangeMotif.setSelected(true);
			}else if(className.equals(classGTK)){
				menuChangeGTK.setSelected(true);
			}else if(className.equals(classMac)){
				menuChangeMac.setSelected(true);
			}else if(className.equals(classNimbus)){
				menuChangeNimbus.setSelected(true);
			}
		}

		menuBar.add(menuChange);

		setJMenuBar(menuBar);
	}

	/**
	 * �X�e�[�^�X�o�[�̐ݒ���s��
	 * ���̃��\�b�h�̓R���X�g���N�^����Ă΂��
	 */
	private void addStatusBar(){
		// �p�l���̃C���X�^���X�𐶐�����
		JPanel panelStatusBar = new JPanel();
		JPanel[] panelStatusItems = new JPanel[2];
		for(int i = 0 ; i < panelStatusItems.length ; i++){
			panelStatusItems[i] = new JPanel();
		}

		// �p�l���̔z�u���s��
		panelStatusBar.setLayout(new BorderLayout(2, 2));
		panelStatusBar.setBorder(new EmptyBorder(2, 0, 0, 0));
		for(int i = 0 ; i < panelStatusItems.length ; i++){
			panelStatusItems[i].setLayout(new BorderLayout());
			panelStatusItems[i].setBorder(new CompoundBorder(
				new LineBorder(Color.GRAY),
				new EmptyBorder(2, 6, 2, 6)));
		}
		panelStatusBar.add(panelStatusItems[0], BorderLayout.CENTER);
		panelStatusBar.add(panelStatusItems[1], BorderLayout.EAST);
		getContentPane().add(panelStatusBar, BorderLayout.SOUTH);

		// �e�p�l���A�C�e���̐ݒ���s��
		labelStatusBar.setText(strStatusBar);
		panelStatusItems[0].add(labelStatusBar, BorderLayout.CENTER);

		String nameOS;
		try{
			nameOS = System.getProperty("os.name");
		}
		catch(SecurityException e){
			nameOS = "OS�����擾�ł��܂���";
		}
		catch(NullPointerException e){
			e.printStackTrace();
			nameOS = "OS�����擾�ł��܂���";
		}
		catch(IllegalArgumentException e){
			e.printStackTrace();
			nameOS = "OS�����擾�ł��܂���";
		}
		panelStatusItems[1].add(new JLabel(nameOS), BorderLayout.CENTER);
	}

	/**
	 * ���b�N�A���h�t�B�[�����T�|�[�g����Ă��邩�𒲂ׂ�
	 */
	protected boolean isSupportedLookAndFeel(String className){
		try{
			Class laf = Class.forName(className);
			LookAndFeel newLAF = (LookAndFeel)(laf.newInstance());
			return newLAF.isSupportedLookAndFeel();
		}
		catch(Exception e){
			return false;
		}
	}

	/**
	 * �A�N�V�����C�x���g
	 */
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == btnDir){
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setSelectedFile(new File(txtDir.getText()));
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				txtDir.setText(chooser.getSelectedFile().getPath());
			}

		}else if(e.getSource() == btnSearch){
			if(flagSearching){
				// �t���O���~�낵�����𒆎~����
				btnSearch.setEnabled(false);
				flagSearching = false;
			}else if(listFileData.getSize() == 0 ||
					JOptionPane.showConfirmDialog(this,
						"�������ʂ͏�������܂��B�����𑱍s���Ă���낵���ł����H",
						"���X�g�̏���",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				// �X���b�h���쐬���A�������J�n����
				new Thread(this).start();
			}

		}else if(e.getSource() == btnClear){
			if(JOptionPane.showConfirmDialog(this,
					"�������ʂ͏�������܂��B�����𑱍s���Ă���낵���ł����H",
					"���X�g�̏���",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				// ���X�g������������
				listFileData.clear();
				setStatusBarText("���X�g�����������܂���");
			}

		}else if(e.getSource() == btnCopy){

		}else if(e.getSource() == menuFileExit){
			System.exit(0);
		}
	}

	/**
	 * �X���b�h�����s����
	 */
	public void run(){
		try{
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					// �������������t���O�𗧂Ă�
					setSearchingFlag(true);

					// ���݂̃��X�g���N���A����
					listFileData.clear();
				}
			});

			// �w�肳�ꂽ�f�B���N�g�������݂��邩�𒲂ׂ�
			File fileDir = new File(txtDir.getText());
			if(!fileDir.isDirectory()){
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						setStatusBarText("�f�B���N�g�������݂��܂���");
					}
				});
				return;
			}

			// �t�@�C������������
			final int numFiles = RecursiveSearch(fileDir, txtFile.getText(), listFileData);
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					if(numFiles == 0){
						setStatusBarText("�t�@�C����������܂���ł���");
					}else{
						setStatusBarText(numFiles + "�̃t�@�C����������܂���");
					}
				}
			});
		}
		finally{
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					// �������������t���O���~�낷
					setSearchingFlag(false);
				}
			});
		}
	}

	/**
	 * �ċA�I�Ƀt�@�C���������s���A���������t�@�C������Ԃ�
	 */
	private int RecursiveSearch(File dir, String strPattern, DefaultListModel list){
		int num = 0;	// �������ꂽ�t�@�C����

		final String dirPath = dir.getAbsolutePath();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				// �X�e�[�^�X�o�[�̃e�L�X�g��ݒ肷��
				setStatusBarText(dirPath + " ��������...");
			}
		});

		try{
			// dir���̃t�@�C������������
			int flags = 0;
			if(!chkCase.isSelected()){
				flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			}
			final Pattern pattern = Pattern.compile(strPattern, flags);
			final File[] files = dir.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					if(pathname.isDirectory()){
						return true;
					}else{
						return pattern.matcher(pathname.getName()).matches();
					}
				}
			});
			if(files == null){
				return 0;
			}

			// dir�̃T�u�f�B���N�g�����ċA�I�Ɍ�������
			for(int i = 0 ; i < files.length ; i++){
				if(files[i].isDirectory()){
					num += RecursiveSearch(files[i], strPattern, list);
				}else{
					final DefaultListModel param_list = list;
					final int param_i = i;
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							param_list.addElement(files[param_i].getAbsolutePath());
						}
					});
					num++;
				}

				// �t���O���~�낳��Ă���ꍇ�͌����𒆎~����
				if(!flagSearching)
					break;
			}
		}
		catch(PatternSyntaxException e){
			try{
				JOptionPane.showMessageDialog(this,
					"���K�\���̍\���ɃG���[������܂�", "Error",
					JOptionPane.ERROR_MESSAGE);
			}
			catch(HeadlessException exc){
				e.printStackTrace();
			}
		}
		catch(IllegalArgumentException e){
			e.printStackTrace();
		}
		catch(SecurityException e){
			e.printStackTrace();
		}

		return num;
	}

	/**
	 * �t���O��ݒ肵�A����ɔ����R���|�[�l���g�̏�Ԃ�ω�������
	 */
	protected void setSearchingFlag(boolean flag){
		// �t���O��ݒ肷��
		flagSearching = flag;

		// �R���|�[�l���g�̏�Ԃ�ς���
		if(flag){
			btnSearch.setText("�������~");
		}else{
			btnSearch.setText("�����J�n");
			btnSearch.setEnabled(true);
		}
		btnClear.setEnabled(!flag);
	}

	/**
	 * �X�e�[�^�X�o�[�̃e�L�X�g��ύX����
	 * labelStatusBar��setText���\�b�h���g�p���ăe�L�X�g��ύX�����ꍇ�ɂ�
	 * �ꎞ�I�ɕύX����邪�A���̃��\�b�h���g�p�����ꍇ�͕ύX���e���ۑ������
	 */
	protected void setStatusBarText(String text){
		strStatusBar = text;
		labelStatusBar.setText(strStatusBar);
	}

	public void changeSelectMenu(boolean isIncluded, JMenuItem sender){
		// ���j���[�I���������̓���
		if(!isIncluded){
			labelStatusBar.setText(strStatusBar);

		// ���j���[�I�����̓���
		}else{
			if(sender == menuFileExit){
				labelStatusBar.setText("�A�v���P�[�V�������I������");
			}else if(sender == menuChange){
				labelStatusBar.setText("�A�v���P�[�V�����̊O�ς̕ύX���s��");
			}else if(sender == menuChangeCross){
				labelStatusBar.setText("Swing�̃N���X�v���b�g�t�H�[���̃��b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeSystem){
				labelStatusBar.setText("�g�p���̃V�X�e���̃��b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeMetal){
				labelStatusBar.setText("Metal���b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeWin){
				labelStatusBar.setText("Windows���b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeMotif){
				labelStatusBar.setText("CDE/Motif���b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeGTK){
				labelStatusBar.setText("GTK+���b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeMac){
				labelStatusBar.setText("Macintosh���b�N�A���h�t�B�[����K�p����");
			}else if(sender == menuChangeNimbus){
				labelStatusBar.setText("Nimbus���b�N�A���h�t�B�[����K�p����");
			}else{
				labelStatusBar.setText(sender.getName());
			}
		}
	}

	/**
	 * ���b�N�A���h�t�B�[���ύX���̃A�N�V����
	 */
	class ChangeLookAndFeelAction extends AbstractAction
	{
		private Component comp;
		private String className;

		/**
		 * @param comp ���b�N�A���h�t�B�[����ύX����R���|�[�l���g
		 * @param className Look & Feel ����������N���X�̖��O���w�肷�镶����
		 */
		public ChangeLookAndFeelAction(Component comp, String className){
			super();
			this.comp = comp;
			this.className = className;
		}

		/**
		 * @param comp ���b�N�A���h�t�B�[����ύX����R���|�[�l���g
		 * @param className Look & Feel ����������N���X�̖��O���w�肷�镶����
		 * @param name ����������
		 */
		public ChangeLookAndFeelAction(Component comp, String className, String name){
			super(name);
			this.comp = comp;
			this.className = className;
		}

		/**
		 * @param comp ���b�N�A���h�t�B�[����ύX����R���|�[�l���g
		 * @param className Look & Feel ����������N���X�̖��O���w�肷�镶����
		 * @param name ����������
		 * @param icon �A�C�R��
		 */
		public ChangeLookAndFeelAction(Component comp, String className, String name, Icon icon){
			super(name, icon);
			this.comp = comp;
			this.className = className;
		}

		public void actionPerformed(ActionEvent e){
			try{
				LookAndFeel laf = UIManager.getLookAndFeel();
				if(laf == null || !laf.getClass().getName().equals(className)){
					UIManager.setLookAndFeel(className);
					SwingUtilities.updateComponentTreeUI(comp);

					// ���j���[���ڂ̑I����Ԃ��X�V����
					if(className.equals(classMetal)){
						menuChangeMetal.setSelected(true);
					}else if(className.equals(classWin)){
						menuChangeWin.setSelected(true);
					}else if(className.equals(classMotif)){
						menuChangeMotif.setSelected(true);
					}else if(className.equals(classGTK)){
						menuChangeGTK.setSelected(true);
					}else if(className.equals(classMac)){
						menuChangeMac.setSelected(true);
					}else if(className.equals(classNimbus)){
						menuChangeNimbus.setSelected(true);
					}
				}
			}
			catch(ClassNotFoundException exc){
				exc.printStackTrace();
			}
			catch(InstantiationException exc){
				exc.printStackTrace();
			}
			catch(IllegalAccessException exc){
				exc.printStackTrace();
			}
			catch (UnsupportedLookAndFeelException exc){
				exc.printStackTrace();
			}
		}
	}
}
