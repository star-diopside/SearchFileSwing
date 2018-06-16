package jp.gr.java_conf.stardiopside.searchfile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

    private JLabel lblDir = new JLabel("ディレクトリ名");
    private JTextField txtDir = new JTextField(21);
    private JButton btnDir = new JButton("ディレクトリの指定");
    private JLabel lblFile = new JLabel("ファイル名");
    private JTextField txtFile = new JTextField(21);
    private JRadioButton radioRegular = new JRadioButton("正規表現");
    private JRadioButton radioWildCard = new JRadioButton("ワイルドカード");
    private JCheckBox chkCase = new JCheckBox("大文字/小文字の区別を行う");
    private JButton btnSearch = new JButton("検索開始");
    private JButton btnClear = new JButton("リストのクリア");
    private JButton btnCopy = new JButton("検索結果をクリップボードにコピー");
    private JButton btnSelectAll = new JButton("すべて選択");
    private JButton btnSelectedClear = new JButton("選択解除");
    private JButton btnDeleteFile = new JButton("選択ファイルを削除");
    private JCheckBox chkDelete = new JCheckBox("削除時にファイルをごみ箱に移す");

    private JLinkMenu menuFile;
    private JLinkMenuItem menuFileExit;
    private JLinkMenu menuChange;
    private JLinkMenuItem menuChangeCross;
    private JLinkMenuItem menuChangeSystem;

    private Map<String, JRadioButtonMenuItem> lookAndFeelSelectedButtons;

    private DefaultListModel<JCheckBox> listFileData = new DefaultListModel<>();
    private JCheckBoxList listFile = new JCheckBoxList(listFileData);

    private JLabel labelStatusBar = new JLabel();
    private String strStatusBar = "レディ";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isSearching = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SearchFile frame = new SearchFile("ファイルの検索");

            // ウィンドウの大きさと終了動作の設定
            frame.setLocation(10, 10);
            frame.setSize(800, 550);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

            // ウィンドウを表示する
            frame.setVisible(true);
        });
    }

    public SearchFile(String title) {
        super(title);

        initComponents(); // コンポーネントの設定
        addMenuBar(); // メニューバーの設定
        addStatusBar(); // ステータスバーの設定
    }

    /**
     * コンポーネントの初期化を行う<br>
     * このメソッドはコンストラクタから呼ばれる
     */
    private void initComponents() {
        /*
         * 複数のコンポーネントを含むパネルのコントロールの設定
         */
        JGridBagLayoutPanel panelSubComponents = new JGridBagLayoutPanel();

        // コンポーネントを追加する
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

        // データを設定する
        txtDir.setText(Paths.get(".").toAbsolutePath().getParent().toString());

        // グループと初期状態を設定する
        ButtonGroup groupRadio = new ButtonGroup();
        groupRadio.add(radioRegular);
        groupRadio.add(radioWildCard);
        radioRegular.setSelected(true);

        // アクションリスナーを追加する
        btnDir.addActionListener(this::onChooseDirectory);
        btnSearch.addActionListener(this::onSearch);
        btnClear.addActionListener(this::onClearResults);
        btnCopy.addActionListener(this::onCopyResults);
        btnSelectAll.addActionListener(this::onSelectAll);
        btnSelectedClear.addActionListener(this::onClearSelection);
        btnDeleteFile.addActionListener(this::onDeleteSelectionFile);

        // ツールチップを設定する
        btnDir.setToolTipText("検索するディレクトリを指定する");
        chkCase.setToolTipText("ファイル名の大文字/小文字を区別して検索を行う");
        btnSearch.setToolTipText("検索を開始する");
        btnClear.setToolTipText("検索結果をクリアする");
        btnCopy.setToolTipText("検索結果をクリップボードにコピーする");
        btnSelectAll.setToolTipText("リストを全選択状態にする");
        btnSelectedClear.setToolTipText("リストの選択状態を解除する");
        btnDeleteFile.setToolTipText("選択されたファイルを削除する");

        // デフォルトのボタンの設定
        getRootPane().setDefaultButton(btnSearch);

        JPanel panelComponents = new JPanel();
        panelComponents.setLayout(new BorderLayout());
        panelComponents.add(panelSubComponents, BorderLayout.NORTH);

        /*
         * リストコントロールの設定
         */
        JScrollPane scrollList = new JScrollPane(listFile);
        scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        /*
         * 左右のパネルを配置
         */
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelComponents, BorderLayout.EAST);
        getContentPane().add(scrollList, BorderLayout.CENTER);
    }

    /**
     * メニューバーの設定を行う このメソッドはコンストラクタから呼ばれる
     */
    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        /*
         * ファイルメニューの設定
         */
        menuFile = new JLinkMenu("ファイル(F)", this);
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFileExit = new JLinkMenuItem("アプリケーションの終了(X)", this);
        menuFileExit.setMnemonic(KeyEvent.VK_X);
        menuFileExit.setHint("アプリケーションを終了する");
        menuFile.add(menuFileExit);
        menuFileExit.addActionListener(e -> System.exit(0));
        menuBar.add(menuFile);

        /*
         * Look & Feel メニューの設定
         */
        menuChange = new JLinkMenu("Look & Feel", this);
        menuChange.setMnemonic(KeyEvent.VK_L);
        menuChange.setHint("アプリケーションの外観の変更を行う");

        menuChangeCross = new JLinkMenuItem(
                new ChangeLookAndFeelAction(UIManager.getCrossPlatformLookAndFeelClassName(), "クロスプラットフォーム(C)"), this);
        menuChangeCross.setMnemonic(KeyEvent.VK_C);
        menuChangeCross.setHint("クロスプラットフォームのルックアンドフィールを適用する");
        menuChange.add(menuChangeCross);

        menuChangeSystem = new JLinkMenuItem(
                new ChangeLookAndFeelAction(UIManager.getSystemLookAndFeelClassName(), "システムプラットフォーム(S)"), this);
        menuChangeSystem.setMnemonic(KeyEvent.VK_S);
        menuChangeSystem.setHint("OSのルックアンドフィールを適用する");
        menuChange.add(menuChangeSystem);

        menuChange.addSeparator();

        ButtonGroup groupLAF = new ButtonGroup();
        Map<String, JRadioButtonMenuItem> menuItems = new HashMap<>();

        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            JLinkRadioButtonMenuItem item = new JLinkRadioButtonMenuItem(
                    new ChangeLookAndFeelAction(info.getClassName(), info.getName()), this);
            if (!info.getName().isEmpty()) {
                item.setMnemonic(info.getName().charAt(0));
            }
            item.setHint(info.getName() + "ルックアンドフィールを適用する");
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

        setJMenuBar(menuBar);
    }

    /**
     * ステータスバーの設定を行う<br>
     * このメソッドはコンストラクタから呼ばれる
     */
    private void addStatusBar() {
        // パネルのインスタンスを生成する
        JPanel panelStatusBar = new JPanel();
        JPanel[] panelStatusItems = new JPanel[2];
        for (int i = 0; i < panelStatusItems.length; i++) {
            panelStatusItems[i] = new JPanel();
        }

        // パネルの配置を行う
        panelStatusBar.setLayout(new BorderLayout(2, 2));
        panelStatusBar.setBorder(new EmptyBorder(2, 0, 0, 0));
        for (int i = 0; i < panelStatusItems.length; i++) {
            panelStatusItems[i].setLayout(new BorderLayout());
            panelStatusItems[i].setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(2, 6, 2, 6)));
        }
        panelStatusBar.add(panelStatusItems[0], BorderLayout.CENTER);
        panelStatusBar.add(panelStatusItems[1], BorderLayout.EAST);
        getContentPane().add(panelStatusBar, BorderLayout.SOUTH);

        // 各パネルアイテムの設定を行う
        labelStatusBar.setText(strStatusBar);
        panelStatusItems[0].add(labelStatusBar, BorderLayout.CENTER);

        String nameOS = System.getProperty("os.name");
        panelStatusItems[1].add(new JLabel(nameOS), BorderLayout.CENTER);
    }

    /**
     * フラグを設定し、それに伴いコンポーネントの状態を変化させる
     */
    private void setSearchingFlag(boolean isSearching) {
        // フラグを設定する
        this.isSearching = isSearching;

        // コンポーネントの状態を変える
        if (isSearching) {
            btnSearch.setText("検索中止");
        } else {
            btnSearch.setText("検索開始");
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
            setStatusBarText("ディレクトリが存在しません");
            return;
        }

        // 検索パターンの正規表現を生成する
        Pattern searchPattern;
        try {
            int flags = 0;
            if (!chkCase.isSelected()) {
                flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            }
            searchPattern = Pattern.compile(txtFile.getText(), flags);
        } catch (PatternSyntaxException ex) {
            JOptionPane.showMessageDialog(this, "正規表現の構文にエラーがあります", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (listFileData.getSize() == 0 || JOptionPane.showConfirmDialog(this, "検索結果は消去されます。処理を続行してもよろしいですか？", "リストの消去",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // 現在のリストをクリアする
            listFileData.clear();

            // 検索中を示すフラグを立てる
            setSearchingFlag(true);

            // 別スレッドで検索を開始する
            executorService.submit(() -> {
                try {
                    Files.walkFileTree(fileDir, new FileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            SwingUtilities.invokeLater(() -> setStatusBarText(dir + " を検索中..."));
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (searchPattern.matcher(file.getFileName().toString()).matches()) {
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
                            setStatusBarText("ファイルが見つかりませんでした");
                        } else {
                            setStatusBarText(listFileData.size() + "個のファイルが見つかりました");
                        }
                    });
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                    SwingUtilities.invokeLater(() -> setStatusBarText("ファイル検索中にエラーが発生しました"));
                } finally {
                    // 検索中を示すフラグを降ろす
                    SwingUtilities.invokeLater(() -> setSearchingFlag(false));
                }
            });
        }
    }

    private void onClearResults(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, "検索結果は消去されます。処理を続行してもよろしいですか？", "リストの消去",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // リストを初期化する
            listFileData.clear();
            setStatusBarText("リストを初期化しました");
        }
    }

    private void onSelectAll(ActionEvent e) {
    }

    private void onClearSelection(ActionEvent e) {
    }

    private void onDeleteSelectionFile(ActionEvent e) {
    }

    private void onCopyResults(ActionEvent e) {
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
