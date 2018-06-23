package jp.gr.java_conf.stardiopside.searchfile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Spliterators;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * ファイル検索を行うアプリケーションのフレーム
 */
@SuppressWarnings("serial")
public class SearchFile extends JFrame implements MenuHintListener {
    private static final Logger logger = Logger.getLogger(SearchFile.class.getName());
    private static final ResourceBundle resource = ResourceBundle.getBundle("messages");

    private Map<String, JRadioButtonMenuItem> lookAndFeelSelectedButtons;
    private DefaultListModel<JCheckBox> listFileData = new DefaultListModel<>();

    private JCheckBoxList listFile;
    private JTextField txtDir;
    private JTextField txtFile;
    private JRadioButton radioRegular;
    private JRadioButton radioWildCard;
    private JButton btnSearch;
    private JButton btnClear;
    private JCheckBox chkDelete;

    private String strStatusBar = resource.getString("message.initStatusBar");
    private JLabel labelStatusBar = new JLabel(strStatusBar);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isSearching = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SearchFile frame = new SearchFile();

            // ウィンドウの大きさと終了動作の設定
            frame.setLocation(10, 10);
            frame.setSize(800, 550);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

            // ウィンドウを表示する
            frame.setVisible(true);
        });
    }

    public SearchFile() {
        super(resource.getString("title.searchFile"));

        Arrays.stream(UIManager.getInstalledLookAndFeels()).filter(info -> info.getName().equals("Nimbus")).findFirst()
                .ifPresent(info -> {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | UnsupportedLookAndFeelException e) {
                        throw new RuntimeException(e);
                    }
                });

        setJMenuBar(menuBar());
        initializeComponents();
        getContentPane().add(statusBar(), BorderLayout.SOUTH);

        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * コンポーネントの初期化を行う。
     */
    private void initializeComponents() {
        // listFile
        listFile = new JCheckBoxList(listFileData);

        // lblDir
        JLabel lblDir = new JLabel(resource.getString("label.lblDir"));

        // txtDir
        txtDir = new JTextField(21);
        txtDir.setText(Paths.get(".").toAbsolutePath().getParent().toString());

        // btnDir
        JButton btnDir = new JButton(resource.getString("label.btnDir"));
        btnDir.setToolTipText(resource.getString("toolTip.btnDir"));
        btnDir.addActionListener(this::onChooseDirectory);

        // lblFile
        JLabel lblFile = new JLabel(resource.getString("label.lblFile"));

        // txtFile
        txtFile = new JTextField(21);

        // radioRegular
        radioRegular = new JRadioButton(resource.getString("label.radioRegular"));

        // radioWildCard
        radioWildCard = new JRadioButton(resource.getString("label.radioWildCard"));
        radioWildCard.setSelected(true);

        // btnSearch
        btnSearch = new JButton(resource.getString("label.btnSearch.start"));
        btnSearch.setToolTipText(resource.getString("toolTip.btnSearch"));
        btnSearch.addActionListener(this::onSearch);

        // btnClear
        btnClear = new JButton(resource.getString("label.btnClear"));
        btnClear.setToolTipText(resource.getString("toolTip.btnClear"));
        btnClear.addActionListener(this::onClearResults);

        // btnCopy
        JButton btnCopy = new JButton(resource.getString("label.btnCopy"));
        btnCopy.setToolTipText(resource.getString("toolTip.btnCopy"));
        btnCopy.addActionListener(this::onCopyResults);

        // btnSelectAll
        JButton btnSelectAll = new JButton(resource.getString("label.btnSelectAll"));
        btnSelectAll.setToolTipText(resource.getString("toolTip.btnSelectAll"));
        btnSelectAll.addActionListener(this::onSelectAll);

        // btnSelectedClear
        JButton btnSelectedClear = new JButton(resource.getString("label.btnSelectedClear"));
        btnSelectedClear.setToolTipText(resource.getString("toolTip.btnSelectedClear"));
        btnSelectedClear.addActionListener(this::onClearSelection);

        // btnDeleteFile
        JButton btnDeleteFile = new JButton(resource.getString("label.btnDeleteFile"));
        btnDeleteFile.setToolTipText(resource.getString("toolTip.btnDeleteFile"));
        btnDeleteFile.addActionListener(this::onDeleteSelectedFile);

        // chkDelete
        chkDelete = new JCheckBox(resource.getString("label.chkDelete"));

        // グループ設定
        ButtonGroup groupRadio = new ButtonGroup();
        groupRadio.add(radioWildCard);
        groupRadio.add(radioRegular);

        // panelSubComponents
        JPanel panelSubComponents = new JPanel();
        GridBagLayoutUtils gridBagLayoutUtils = new GridBagLayoutUtils(panelSubComponents);
        gridBagLayoutUtils.setGridInsets(new Insets(2, 8, 2, 8));
        gridBagLayoutUtils.add(lblDir, 2, 1, GridBagConstraints.WEST, new Insets(8, 8, 2, 8));
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(txtDir, 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(btnDir, 2, 1, GridBagConstraints.EAST);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(lblFile, 2, 1, GridBagConstraints.WEST);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(txtFile, 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(radioWildCard, 1, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.add(radioRegular, 1, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(btnSearch, 1, 1, GridBagConstraints.WEST);
        gridBagLayoutUtils.add(btnClear, 1, 1, GridBagConstraints.EAST);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(btnCopy, 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(btnSelectAll, 1, 1, GridBagConstraints.WEST, new Insets(24, 8, 2, 8));
        gridBagLayoutUtils.add(btnSelectedClear, 1, 1, GridBagConstraints.EAST, new Insets(24, 8, 2, 8));
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(btnDeleteFile, 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(chkDelete, 2, 1, GridBagConstraints.WEST);

        // panelComponents
        JPanel panelComponents = new JPanel();
        panelComponents.setLayout(new BorderLayout());
        panelComponents.add(panelSubComponents, BorderLayout.NORTH);

        // scrollList
        JScrollPane scrollList = new JScrollPane(listFile);
        scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // デフォルトのボタンの設定
        getRootPane().setDefaultButton(btnSearch);

        // パネル配置
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelComponents, BorderLayout.EAST);
        getContentPane().add(scrollList, BorderLayout.CENTER);
    }

    /**
     * メニューバーのコンポーネントを生成する。
     * 
     * @return {@link JMenuBar} コンポーネント
     */
    private JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ファイルメニュー
        JLinkMenu menuFile = new JLinkMenu(resource.getString("label.menuFile"), this);
        menuFile.setMnemonic(KeyEvent.VK_F);
        JLinkMenuItem menuFileExit = new JLinkMenuItem(resource.getString("label.menuFileExit"), this);
        menuFileExit.setMnemonic(KeyEvent.VK_X);
        menuFileExit.setHint(resource.getString("hint.menuFileExit"));
        menuFile.add(menuFileExit);
        menuFileExit.addActionListener(e -> System.exit(0));
        menuBar.add(menuFile);

        // ルックアンドフィールメニュー
        JLinkMenu menuChange = new JLinkMenu(resource.getString("label.menuChange"), this);
        menuChange.setMnemonic(KeyEvent.VK_L);
        menuChange.setHint(resource.getString("hint.menuChange"));

        JLinkMenuItem menuChangeCross = new JLinkMenuItem(new ChangeLookAndFeelAction(
                UIManager.getCrossPlatformLookAndFeelClassName(), resource.getString("label.menuChangeCross")), this);
        menuChangeCross.setMnemonic(KeyEvent.VK_C);
        menuChangeCross.setHint(resource.getString("hint.menuChangeCross"));
        menuChange.add(menuChangeCross);

        JLinkMenuItem menuChangeSystem = new JLinkMenuItem(new ChangeLookAndFeelAction(
                UIManager.getSystemLookAndFeelClassName(), resource.getString("label.menuChangeSystem")), this);
        menuChangeSystem.setMnemonic(KeyEvent.VK_S);
        menuChangeSystem.setHint(resource.getString("hint.menuChangeSystem"));
        menuChange.add(menuChangeSystem);

        menuChange.addSeparator();

        ButtonGroup groupLAF = new ButtonGroup();
        Map<String, JRadioButtonMenuItem> menuItems = new HashMap<>();

        MessageFormat formatLabelLAF = new MessageFormat(resource.getString("label.menuChangeLookAndFeel"));
        MessageFormat formatHintLAF = new MessageFormat(resource.getString("hint.menuChangeLookAndFeel"));

        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            JLinkRadioButtonMenuItem item = new JLinkRadioButtonMenuItem(new ChangeLookAndFeelAction(
                    info.getClassName(), formatLabelLAF.format(new Object[] { info.getName() })), this);
            if (!info.getName().isEmpty()) {
                item.setMnemonic(info.getName().charAt(0));
            }
            item.setHint(formatHintLAF.format(new Object[] { info.getName() }));
            menuChange.add(item);
            groupLAF.add(item);
            menuItems.put(info.getClassName(), item);
        }

        lookAndFeelSelectedButtons = menuItems;

        // 現在のルックアンドフィールからメニューの選択状態を決定する
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf != null) {
            String className = laf.getClass().getName();
            menuItems.get(className).setSelected(true);
        }

        menuBar.add(menuChange);

        return menuBar;
    }

    /**
     * ステータスバーのコンポーネントを生成する。
     * 
     * @return ステータスバーの {@link JPanel} コンポーネント
     */
    private JPanel statusBar() {
        JPanel panelStatusMessage = new JPanel();
        panelStatusMessage.setLayout(new BorderLayout());
        panelStatusMessage.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(2, 6, 2, 6)));
        panelStatusMessage.add(labelStatusBar, BorderLayout.CENTER);

        JPanel panelStatusOS = new JPanel();
        panelStatusOS.setLayout(new BorderLayout());
        panelStatusOS.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(2, 6, 2, 6)));
        panelStatusOS.add(new JLabel(System.getProperty("os.name")), BorderLayout.CENTER);

        JPanel panelStatusBar = new JPanel();
        panelStatusBar.setLayout(new BorderLayout(2, 2));
        panelStatusBar.setBorder(new EmptyBorder(2, 0, 0, 0));
        panelStatusBar.add(panelStatusMessage, BorderLayout.CENTER);
        panelStatusBar.add(panelStatusOS, BorderLayout.EAST);

        return panelStatusBar;
    }

    /**
     * フラグを設定し、それに伴いコンポーネントの状態を変化させる
     */
    private void setSearchingFlag(boolean isSearching) {
        // フラグを設定する
        this.isSearching = isSearching;

        // コンポーネントの状態を変える
        if (isSearching) {
            btnSearch.setText(resource.getString("label.btnSearch.stop"));
        } else {
            btnSearch.setText(resource.getString("label.btnSearch.start"));
            btnSearch.setEnabled(true);
        }
        btnClear.setEnabled(!isSearching);
    }

    /**
     * ステータスバーのテキストを変更する<br>
     * labelStatusBarのsetTextメソッドを使用してテキストを変更した場合には
     * 一時的に変更されるが、このメソッドを使用した場合は変更内容が保存される
     */
    private void setStatusBarText(String text) {
        strStatusBar = text;
        labelStatusBar.setText(strStatusBar);
    }

    @Override
    public void changeSelectMenu(JMenuItem sender, boolean isIncluded, String hint) {
        if (isIncluded) {
            labelStatusBar.setText(hint);
        } else {
            labelStatusBar.setText(strStatusBar);
        }
    }

    private void onChooseDirectory(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(txtDir.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDir.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void onSearch(ActionEvent e) {
        if (isSearching) {
            // フラグを降ろし検索を中止する
            btnSearch.setEnabled(false);
            isSearching = false;
            return;
        }

        // 指定されたディレクトリが存在するかを調べる
        Path fileDir = Paths.get(txtDir.getText());
        if (!Files.isDirectory(fileDir)) {
            JOptionPane.showMessageDialog(this, resource.getString("message.directoryNotFound"),
                    resource.getString("title.errorDialog"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // パス照合処理を設定する
        PathMatcher pathMatcher;
        try {
            String syntax;
            if (radioRegular.isSelected()) {
                syntax = "regex:";
            } else if (radioWildCard.isSelected()) {
                syntax = "glob:";
            } else {
                throw new IllegalStateException();
            }
            pathMatcher = FileSystems.getDefault().getPathMatcher(syntax + txtFile.getText());
        } catch (PatternSyntaxException ex) {
            logger.log(Level.FINE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, resource.getString("message.searchConditionError"),
                    resource.getString("title.errorDialog"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!listFileData.isEmpty()
                && JOptionPane.showConfirmDialog(this, resource.getString("message.clearResultConfirmDialog"),
                        resource.getString("title.clearResultConfirmDialog"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        // 現在のリストをクリアする
        listFileData.clear();

        // 検索中を示すフラグを立てる
        setSearchingFlag(true);

        // 別スレッドで検索を開始する
        executorService.submit(() -> {
            try {
                Files.walkFileTree(fileDir, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        SwingUtilities.invokeLater(() -> setStatusBarText(
                                MessageFormat.format(resource.getString("message.searchingDirectory"), dir)));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (pathMatcher.matches(file.getFileName())) {
                            SwingUtilities.invokeLater(
                                    () -> listFileData.addElement(new JCheckBox(file.toAbsolutePath().toString())));
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        logger.log(Level.WARNING, exc.getMessage(), exc);
                        return FileVisitResult.CONTINUE;
                    };

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException attrs) throws IOException {
                        return isSearching ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                    }
                });

                SwingUtilities.invokeLater(() -> {
                    if (listFileData.isEmpty()) {
                        setStatusBarText(resource.getString("message.searchResult.empty"));
                    } else {
                        setStatusBarText(MessageFormat.format(resource.getString("message.searchResult.found"),
                                listFileData.size()));
                    }
                });
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
                SwingUtilities.invokeLater(() -> setStatusBarText(resource.getString("message.searchResult.error")));
            } finally {
                // 検索中を示すフラグを降ろす
                SwingUtilities.invokeLater(() -> setSearchingFlag(false));
            }
        });
    }

    private void onClearResults(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, resource.getString("message.clearResultConfirmDialog"),
                resource.getString("title.clearResultConfirmDialog"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // リストを初期化する
            listFileData.clear();
            setStatusBarText(resource.getString("message.clearResults"));
        }
    }

    private void onSelectAll(ActionEvent e) {
        stream(listFileData).forEach(c -> c.setSelected(true));
        listFile.repaint();
    }

    private void onClearSelection(ActionEvent e) {
        stream(listFileData).forEach(c -> c.setSelected(false));
        listFile.repaint();
    }

    private void onDeleteSelectedFile(ActionEvent e) {
        String messageKey = chkDelete.isSelected() ? "moveToTrash" : "delete";
        if (JOptionPane.showConfirmDialog(this, resource.getString("message.deleteFileConfirmDialog." + messageKey),
                resource.getString("title.deleteFileConfirmDialog"),
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        ArrayList<JCheckBox> deletedFiles = new ArrayList<>();
        ArrayList<JCheckBox> errorFiles = new ArrayList<>();
        Stream<JCheckBox> selectedFiles = stream(listFileData).filter(JCheckBox::isSelected);

        if (chkDelete.isSelected()) {
            Desktop desktop = Desktop.getDesktop();
            selectedFiles.forEach(c -> {
                File file = new File(c.getText());
                if (desktop.moveToTrash(file)) {
                    deletedFiles.add(c);
                } else {
                    errorFiles.add(c);
                }
            });
        } else {
            selectedFiles.forEach(c -> {
                try {
                    Path path = Paths.get(c.getText());
                    Files.delete(path);
                    deletedFiles.add(c);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                    errorFiles.add(c);
                }
            });
        }

        deletedFiles.forEach(listFileData::removeElement);

        String resultMessage = MessageFormat.format(resource.getString("message.deleteSelectedFile.success"),
                deletedFiles.size());
        if (!errorFiles.isEmpty()) {
            resultMessage += MessageFormat.format(resource.getString("message.deleteSelectedFile.error"),
                    errorFiles.size());
        }
        setStatusBarText(resultMessage);
    }

    private void onCopyResults(ActionEvent e) {
        String result = stream(listFileData).map(JCheckBox::getText)
                .collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator()));
        StringSelection selection = new StringSelection(result);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        setStatusBarText(MessageFormat.format(resource.getString("message.copyResults"), listFileData.size()));
    }

    private static <T> Stream<T> stream(DefaultListModel<T> list) {
        return StreamSupport.stream(Spliterators.spliterator(list.elements().asIterator(), list.size(), 0), false);
    }

    /**
     * ルックアンドフィール変更時のアクション
     */
    private class ChangeLookAndFeelAction extends AbstractAction {
        private String className;

        /**
         * @param className Look & Feel を実装するクラスの名前を指定する文字列
         * @param name 説明文字列
         */
        public ChangeLookAndFeelAction(String className, String name) {
            super(name);
            this.className = className;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                LookAndFeel laf = UIManager.getLookAndFeel();
                if (laf == null || !laf.getClass().getName().equals(className)) {
                    UIManager.setLookAndFeel(className);
                    SwingUtilities.updateComponentTreeUI(SearchFile.this);

                    // メニュー項目の選択状態を更新する
                    lookAndFeelSelectedButtons.get(className).setSelected(true);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException exc) {
                throw new RuntimeException(exc);
            }
        }
    }
}
