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
import java.util.List;
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
import javax.swing.JList;
import javax.swing.JMenu;
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
    private DefaultListModel<JCheckBox> searchResult = new DefaultListModel<>();

    private JCheckBoxList listSearchResult;
    private JTextField textDirectory;
    private JTextField textFile;
    private JRadioButton radioRegular;
    private JRadioButton radioWildCard;
    private JButton buttonSearch;
    private JButton buttonClearResult;
    private JCheckBox checkMoveToTrash;

    private String statusText = resource.getString("message.initialStatusText");
    private JLabel labelStatus = new JLabel(statusText);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isSearching = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SearchFile frame = new SearchFile();
            frame.setLocationByPlatform(true);
            frame.setSize(1000, 600);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
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

        setJMenuBar(buildMenuBar());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildOperationPane(), BorderLayout.EAST);
        getContentPane().add(buildSearchResultPane(), BorderLayout.CENTER);
        getContentPane().add(buildStatusBar(), BorderLayout.SOUTH);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private JPanel buildOperationPane() {
        // panelSubComponents
        JPanel panelSubComponents = new JPanel();
        GridBagLayoutUtils gridBagLayoutUtils = new GridBagLayoutUtils(panelSubComponents);
        gridBagLayoutUtils.setGridInsets(new Insets(2, 8, 2, 8));
        gridBagLayoutUtils.add(buildLabelDirectory(), 2, 1, GridBagConstraints.WEST, new Insets(8, 8, 2, 8));
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildTextDirectory(), 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildButtonDirectory(), 2, 1, GridBagConstraints.EAST);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildLabelFile(), 2, 1, GridBagConstraints.WEST);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildTextFile(), 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildRadioWildCard(), 1, 1, GridBagConstraints.CENTER, new Insets(8, 8, 8, 8));
        gridBagLayoutUtils.add(buildRadioRegular(), 1, 1, GridBagConstraints.CENTER, new Insets(8, 8, 8, 8));
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildButtonSearch(), 1, 1, GridBagConstraints.WEST);
        gridBagLayoutUtils.add(buildButtonClearResult(), 1, 1, GridBagConstraints.EAST);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildButtonCopyResult(), 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildButtonSelectAll(), 1, 1, GridBagConstraints.WEST, new Insets(24, 8, 2, 8));
        gridBagLayoutUtils.add(buildButtonClearSelection(), 1, 1, GridBagConstraints.EAST, new Insets(24, 8, 2, 8));
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildButtonDeleteFile(), 2, 1, GridBagConstraints.CENTER);
        gridBagLayoutUtils.nextGridY();
        gridBagLayoutUtils.add(buildCheckMoveToTrash(), 2, 1, GridBagConstraints.WEST);

        // グループ設定
        ButtonGroup groupRadio = new ButtonGroup();
        groupRadio.add(radioWildCard);
        groupRadio.add(radioRegular);

        // デフォルトのボタンの設定
        getRootPane().setDefaultButton(buttonSearch);

        // panelComponents
        JPanel panelComponents = new JPanel();
        panelComponents.setLayout(new BorderLayout());
        panelComponents.add(panelSubComponents, BorderLayout.NORTH);

        return panelComponents;
    }

    private JLabel buildLabelDirectory() {
        JLabel labelDirectory = new JLabel(resource.getString("label.labelDirectory"));
        return labelDirectory;
    }

    private JTextField buildTextDirectory() {
        textDirectory = new JTextField(21);
        textDirectory.setText(Paths.get(".").toAbsolutePath().getParent().toString());
        return textDirectory;
    }

    private JButton buildButtonDirectory() {
        JButton buttonDirectory = new JButton(resource.getString("label.buttonDirectory"));
        buttonDirectory.setToolTipText(resource.getString("toolTip.buttonDirectory"));
        buttonDirectory.addActionListener(this::onChooseDirectory);
        return buttonDirectory;
    }

    private JLabel buildLabelFile() {
        JLabel labelFile = new JLabel(resource.getString("label.labelFile"));
        return labelFile;
    }

    private JTextField buildTextFile() {
        textFile = new JTextField(21);
        return textFile;
    }

    private JRadioButton buildRadioRegular() {
        radioRegular = new JRadioButton(resource.getString("label.radioRegular"));
        return radioRegular;
    }

    private JRadioButton buildRadioWildCard() {
        radioWildCard = new JRadioButton(resource.getString("label.radioWildCard"));
        radioWildCard.setSelected(true);
        return radioWildCard;
    }

    private JButton buildButtonSearch() {
        buttonSearch = new JButton(resource.getString("label.buttonSearch.start"));
        buttonSearch.setToolTipText(resource.getString("toolTip.buttonSearch"));
        buttonSearch.addActionListener(this::onSearch);
        return buttonSearch;
    }

    private JButton buildButtonClearResult() {
        buttonClearResult = new JButton(resource.getString("label.buttonClearResult"));
        buttonClearResult.setToolTipText(resource.getString("toolTip.buttonClearResult"));
        buttonClearResult.addActionListener(this::onClearResults);
        return buttonClearResult;
    }

    private JButton buildButtonCopyResult() {
        JButton buttonCopyResult = new JButton(resource.getString("label.buttonCopyResult"));
        buttonCopyResult.setToolTipText(resource.getString("toolTip.buttonCopyResult"));
        buttonCopyResult.addActionListener(this::onCopyResults);
        return buttonCopyResult;
    }

    private JButton buildButtonSelectAll() {
        JButton buttonSelectAll = new JButton(resource.getString("label.buttonSelectAll"));
        buttonSelectAll.setToolTipText(resource.getString("toolTip.buttonSelectAll"));
        buttonSelectAll.addActionListener(this::onSelectAll);
        return buttonSelectAll;
    }

    private JButton buildButtonClearSelection() {
        JButton buttonClearSelection = new JButton(resource.getString("label.buttonClearSelection"));
        buttonClearSelection.setToolTipText(resource.getString("toolTip.buttonClearSelection"));
        buttonClearSelection.addActionListener(this::onClearSelection);
        return buttonClearSelection;
    }

    private JButton buildButtonDeleteFile() {
        JButton buttonDeleteFile = new JButton(resource.getString("label.buttonDeleteFile"));
        buttonDeleteFile.setToolTipText(resource.getString("toolTip.buttonDeleteFile"));
        buttonDeleteFile.addActionListener(this::onDeleteSelectedFile);
        return buttonDeleteFile;
    }

    private JCheckBox buildCheckMoveToTrash() {
        checkMoveToTrash = new JCheckBox(resource.getString("label.checkMoveToTrash"));
        checkMoveToTrash.setSelected(true);
        if (!Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            checkMoveToTrash.setSelected(false);
            checkMoveToTrash.setVisible(false);
        }
        return checkMoveToTrash;
    }

    private JScrollPane buildSearchResultPane() {
        JScrollPane searchResultPane = new JScrollPane(buildListSearchResult());
        searchResultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return searchResultPane;
    }

    private JList<JCheckBox> buildListSearchResult() {
        listSearchResult = new JCheckBoxList(searchResult);
        return listSearchResult;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(buildMenuFile());
        menuBar.add(buildMenuChange());
        return menuBar;
    }

    private JMenu buildMenuFile() {
        JLinkMenu menuFile = new JLinkMenu(resource.getString("label.menuFile"), this);
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(buildMenuFileExit());
        return menuFile;
    }

    private JMenuItem buildMenuFileExit() {
        JLinkMenuItem menuFileExit = new JLinkMenuItem(resource.getString("label.menuFileExit"), this);
        menuFileExit.setMnemonic(KeyEvent.VK_X);
        menuFileExit.setHint(resource.getString("hint.menuFileExit"));
        menuFileExit.addActionListener(e -> System.exit(0));
        return menuFileExit;
    }

    private JMenu buildMenuChange() {
        JLinkMenu menuChange = new JLinkMenu(resource.getString("label.menuChange"), this);
        menuChange.setMnemonic(KeyEvent.VK_L);
        menuChange.setHint(resource.getString("hint.menuChange"));
        menuChange.add(buildMenuChangeCross());
        menuChange.add(buildMenuChangeSystem());
        menuChange.addSeparator();
        buildMenuChangeLookAndFeels().forEach(menuChange::add);
        return menuChange;
    }

    private JMenuItem buildMenuChangeCross() {
        JLinkMenuItem menuChangeCross = new JLinkMenuItem(new ChangeLookAndFeelAction(
                UIManager.getCrossPlatformLookAndFeelClassName(), resource.getString("label.menuChangeCross")), this);
        menuChangeCross.setMnemonic(KeyEvent.VK_C);
        menuChangeCross.setHint(resource.getString("hint.menuChangeCross"));
        return menuChangeCross;
    }

    private JMenuItem buildMenuChangeSystem() {
        JLinkMenuItem menuChangeSystem = new JLinkMenuItem(new ChangeLookAndFeelAction(
                UIManager.getSystemLookAndFeelClassName(), resource.getString("label.menuChangeSystem")), this);
        menuChangeSystem.setMnemonic(KeyEvent.VK_S);
        menuChangeSystem.setHint(resource.getString("hint.menuChangeSystem"));
        return menuChangeSystem;
    }

    private List<JMenuItem> buildMenuChangeLookAndFeels() {
        ArrayList<JMenuItem> menuItems = new ArrayList<>();

        ButtonGroup groupLookAndFeel = new ButtonGroup();
        Map<String, JRadioButtonMenuItem> menuItemMap = new HashMap<>();

        MessageFormat formatLabelLAF = new MessageFormat(resource.getString("label.menuChangeLookAndFeels"));
        MessageFormat formatHintLAF = new MessageFormat(resource.getString("hint.menuChangeLookAndFeels"));

        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            JLinkRadioButtonMenuItem item = new JLinkRadioButtonMenuItem(new ChangeLookAndFeelAction(
                    info.getClassName(), formatLabelLAF.format(new Object[] { info.getName() })), this);
            if (!info.getName().isEmpty()) {
                item.setMnemonic(info.getName().charAt(0));
            }
            item.setHint(formatHintLAF.format(new Object[] { info.getName() }));
            menuItems.add(item);
            groupLookAndFeel.add(item);
            menuItemMap.put(info.getClassName(), item);
        }

        lookAndFeelSelectedButtons = menuItemMap;

        // 現在のルックアンドフィールからメニューの選択状態を決定する
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        if (lookAndFeel != null) {
            String className = lookAndFeel.getClass().getName();
            menuItemMap.get(className).setSelected(true);
        }

        return menuItems;
    }

    private JPanel buildStatusBar() {
        JPanel panelStatusMessage = new JPanel();
        panelStatusMessage.setLayout(new BorderLayout());
        panelStatusMessage.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(2, 6, 2, 6)));
        panelStatusMessage.add(labelStatus, BorderLayout.CENTER);

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
            buttonSearch.setText(resource.getString("label.buttonSearch.stop"));
        } else {
            buttonSearch.setText(resource.getString("label.buttonSearch.start"));
            buttonSearch.setEnabled(true);
        }
        buttonClearResult.setEnabled(!isSearching);
    }

    /**
     * ステータスバーのテキストを変更する<br>
     * {@code labelStatusBar.setText(String)}メソッドを使用してテキストを変更した場合には一時的に変更されるが、
     * このメソッドを使用した場合は変更内容が保存される。
     */
    private void setStatusText(String text) {
        statusText = text;
        labelStatus.setText(statusText);
    }

    @Override
    public void changeSelectMenu(JMenuItem sender, boolean isIncluded, String hint) {
        if (isIncluded) {
            labelStatus.setText(hint);
        } else {
            labelStatus.setText(statusText);
        }
    }

    private void onChooseDirectory(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(textDirectory.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textDirectory.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void onSearch(ActionEvent e) {
        if (isSearching) {
            // フラグを降ろし検索を中止する
            buttonSearch.setEnabled(false);
            isSearching = false;
            return;
        }

        // 指定されたディレクトリが存在するかを調べる
        Path fileDir = Paths.get(textDirectory.getText());
        if (!Files.isDirectory(fileDir)) {
            JOptionPane.showMessageDialog(this, resource.getString("message.directoryNotFound"),
                    resource.getString("title.errorDialog"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // パス照合処理を設定する
        PathMatcher pathMatcher;

        if (textFile.getText().isEmpty()) {
            pathMatcher = path -> true;
        } else {
            try {
                String syntax;
                if (radioRegular.isSelected()) {
                    syntax = "regex:";
                } else if (radioWildCard.isSelected()) {
                    syntax = "glob:";
                } else {
                    throw new IllegalStateException();
                }
                pathMatcher = FileSystems.getDefault().getPathMatcher(syntax + textFile.getText());
            } catch (PatternSyntaxException ex) {
                logger.log(Level.FINE, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(this, resource.getString("message.searchConditionError"),
                        resource.getString("title.errorDialog"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!searchResult.isEmpty()
                && JOptionPane.showConfirmDialog(this, resource.getString("message.clearResultConfirmDialog"),
                        resource.getString("title.clearResultConfirmDialog"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        // 現在のリストをクリアする
        searchResult.clear();

        // 検索中を示すフラグを立てる
        setSearchingFlag(true);

        // 別スレッドで検索を開始する
        executorService.submit(() -> {
            try {
                Files.walkFileTree(fileDir, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        SwingUtilities.invokeLater(() -> setStatusText(
                                MessageFormat.format(resource.getString("message.searchingDirectory"), dir)));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (pathMatcher.matches(file.getFileName())) {
                            SwingUtilities.invokeLater(
                                    () -> searchResult.addElement(new JCheckBox(file.toAbsolutePath().toString())));
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        logger.log(Level.WARNING, exc.getMessage(), exc);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc != null) {
                            logger.log(Level.WARNING, exc.getMessage(), exc);
                        }
                        return isSearching ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                    }
                });

                SwingUtilities.invokeLater(() -> {
                    if (searchResult.isEmpty()) {
                        setStatusText(resource.getString("message.searchResult.empty"));
                    } else {
                        setStatusText(MessageFormat.format(resource.getString("message.searchResult.found"),
                                searchResult.size()));
                    }
                });
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
                SwingUtilities.invokeLater(() -> setStatusText(resource.getString("message.searchResult.error")));
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
            searchResult.clear();
            setStatusText(resource.getString("message.clearResults"));
        }
    }

    private void onSelectAll(ActionEvent e) {
        stream(searchResult).forEach(c -> c.setSelected(true));
        listSearchResult.repaint();
    }

    private void onClearSelection(ActionEvent e) {
        stream(searchResult).forEach(c -> c.setSelected(false));
        listSearchResult.repaint();
    }

    private void onDeleteSelectedFile(ActionEvent e) {
        String messageKey = checkMoveToTrash.isSelected() ? "moveToTrash" : "delete";
        if (JOptionPane.showConfirmDialog(this, resource.getString("message.deleteFileConfirmDialog." + messageKey),
                resource.getString("title.deleteFileConfirmDialog"),
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        ArrayList<JCheckBox> deletedFiles = new ArrayList<>();
        ArrayList<JCheckBox> errorFiles = new ArrayList<>();
        Stream<JCheckBox> selectedFiles = stream(searchResult).filter(JCheckBox::isSelected);

        if (checkMoveToTrash.isSelected()) {
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

        deletedFiles.forEach(searchResult::removeElement);

        String resultMessage = MessageFormat.format(resource.getString("message.deleteSelectedFile.success"),
                deletedFiles.size());
        if (!errorFiles.isEmpty()) {
            resultMessage += MessageFormat.format(resource.getString("message.deleteSelectedFile.error"),
                    errorFiles.size());
        }
        setStatusText(resultMessage);
    }

    private void onCopyResults(ActionEvent e) {
        String result = stream(searchResult).map(JCheckBox::getText)
                .collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator()));
        StringSelection selection = new StringSelection(result);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        setStatusText(MessageFormat.format(resource.getString("message.copyResults"), searchResult.size()));
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
