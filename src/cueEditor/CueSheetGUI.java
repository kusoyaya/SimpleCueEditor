package cueEditor;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class CueSheetGUI extends JFrame {

	private JPanel contentPane;
	private JButton loadButton;
	private JComboBox<String> encodeBox;
	private ReadMachine input;
	private ServiceMachine service = new ServiceMachine();
	private JTextField albumTitleField;
	private JTextField albumPerformerField;
	private JTextField albumFileField;
	private JScrollPane trackArea;
	private JTable trackTable;
	private MyTableModel trackTableModel;
	private String filePath = "";
	private int fileFormat;
	private String[] albumInfo;
	private Object[][] trackInfo;
	private String[] encode = {"UTF-8","Big5","GBK","Shift JIS"};
	private boolean isLoadFile = false;
	private JButton testButton;
	private JPanel albumGeneratePad;
	private JLabel albumGenerateLabel;
	private JTextField albumGenerateField;
	private JPanel controlPad;
	private JPanel albumDatePad;
	private JLabel albumDateLabel;
	private JTextField albumDateField;
	private JButton albumFileButton;
	private boolean hasSomethingChanged = false;
	private boolean hasCover = false;
	private JPanel albumInfoArea;
	private JLabel albumCoverLabel;
	private JPanel albumCoverArea;
	private JButton coverLastButton;
	private JButton coverNextButton;
	private JButton coverSaveButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CueSheetGUI frame = new CueSheetGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CueSheetGUI() {
		setTitle("SimpleCueEditor v0.7");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setMacThing();
		contentPane = new JPanel();
		contentPane.setBounds(new Rectangle(0, 0, 600, 800));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		JPanel albumArea = new JPanel();
		albumArea.setPreferredSize(new Dimension(600, 360));
		contentPane.add(albumArea);
		
		
		albumArea.setLayout(new BoxLayout(albumArea, BoxLayout.X_AXIS));
		ImageIcon albumCoverIcon = new ImageIcon(CueSheetGUI.class.getResource("/loading.gif"));
		albumCoverIcon.getImage().flush();
		
		albumCoverArea = new JPanel();
		albumCoverArea.setMaximumSize(new Dimension(300, 360));
		albumCoverArea.setPreferredSize(new Dimension(300, 360));
		albumArea.add(albumCoverArea);
		
		
		albumCoverLabel = new JLabel("");
		albumCoverLabel.setMaximumSize(new Dimension(300, 300));
		albumCoverArea.add(albumCoverLabel);
		albumCoverLabel.setPreferredSize(new Dimension(300, 300));
		albumCoverLabel.setIcon(albumCoverIcon);
		albumCoverIcon.setImageObserver(albumCoverLabel);
		
		coverLastButton = new JButton("last");
		coverLastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread coverChangeProcess = new Thread(new Runnable(){
					public void run() {
						if(hasCover)
							try{
								albumCoverLabel.setIcon(new ImageIcon(service.getLastAlbumCover()));
							}catch(Exception e){
								
							}
					}
				});
				coverChangeProcess.start();
			}
		});
		coverLastButton.setPreferredSize(new Dimension(75, 20));
		albumCoverArea.add(coverLastButton);
		
		coverNextButton = new JButton("next");
		coverNextButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Thread coverChangeProcess = new Thread(new Runnable(){
					public void run(){
						if(hasCover)
							try{
								albumCoverLabel.setIcon(new ImageIcon(service.getNextAlbumCover()));
							}catch(Exception e){
								
							}
					}
				});
				coverChangeProcess.start();
			}
		});
		coverNextButton.setPreferredSize(new Dimension(75, 20));
		albumCoverArea.add(coverNextButton);
		
		coverSaveButton = new JButton("save");
		coverSaveButton.setPreferredSize(new Dimension(117, 20));
		albumCoverArea.add(coverSaveButton);
		
		albumInfoArea = new JPanel();
		albumArea.add(albumInfoArea);
		albumInfoArea.setLayout(new BoxLayout(albumInfoArea, BoxLayout.Y_AXIS));
		
		
		JPanel albumTItilePad = new JPanel();
		
		JLabel albumTitleLabel = new JLabel("專輯名稱:");
		albumTItilePad.add(albumTitleLabel);
		
		albumTitleField = new JTextField();
		albumTItilePad.add(albumTitleField);
		albumTitleField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				albumInfo[ReadMachine.ALBUM_TITLE] = albumTitleField.getText();
			}
		});
		albumTitleField.setColumns(20);
		
		JPanel albumPerformerPad = new JPanel();
		
		JLabel albumPefromerLabel = new JLabel("專輯演出者:");
		albumPerformerPad.add(albumPefromerLabel);
		
		albumPerformerField = new JTextField();
		albumPerformerField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				albumInfo[ReadMachine.ALBUM_PERFORMER] = albumPerformerField.getText();
			}
		});
		albumPerformerPad.add(albumPerformerField);
		albumPerformerField.setColumns(20);
		
		JPanel albumFilePad = new JPanel();
		
		JLabel albumFileLabel = new JLabel("源檔案:");
		albumFilePad.add(albumFileLabel);
		
		albumFileField = new JTextField();
		albumFileField.setEditable(false);
		albumFilePad.add(albumFileField);
		albumFileField.setColumns(15);
		
		albumFileButton = new JButton("關聯檔案");
		albumFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File selectedFile = null;
				
				JFileChooser fileChooser = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter("Cue Sheet","cue");
				fileChooser.setFileFilter(filter);
				int result = fileChooser.showOpenDialog(loadButton);
				if (result == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
				}
				if(selectedFile != null){
					fileFormat = ReadMachine.getAudioFormat(selectedFile.getName());
					albumFileField.setText(selectedFile.getName());
				}
			}
		});
		albumFilePad.add(albumFileButton);
		
		
		albumGeneratePad = new JPanel();
		
		albumGenerateLabel = new JLabel("專輯類型:");
		albumGeneratePad.add(albumGenerateLabel);
		
		albumGenerateField = new JTextField();
		albumGenerateField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				albumInfo[ReadMachine.ALBUM_GENRE] = albumGenerateField.getText();
			}
		});
		albumGenerateField.setColumns(10);
		albumGeneratePad.add(albumGenerateField);
		
		albumDatePad = new JPanel();
		
		albumDateLabel = new JLabel("專輯年份:");
		albumDatePad.add(albumDateLabel);
		
		albumDateField = new JTextField();
		albumDateField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				albumInfo[ReadMachine.ALBUM_DATE] = albumDateField.getText();
			}
		});
		albumDateField.setColumns(5);
		albumDatePad.add(albumDateField);
		
		controlPad = new JPanel();
		controlPad.setPreferredSize(new Dimension(300, 200));
		controlPad.setLayout(new BoxLayout(controlPad, BoxLayout.X_AXIS));
		
		loadButton = new JButton("讀取檔案");
		controlPad.add(loadButton);
		
		
		
		testButton = new JButton("Just for Test");
		controlPad.add(testButton);
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new WriteMachine("/Users/nasirho/Desktop/testWrite.cue",albumInfo,trackInfo,fileFormat);
				
			}
		});
		
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File selectedFile = null;
				
				JFileChooser fileChooser = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter("Cue Sheet","cue");
				fileChooser.setFileFilter(filter);
				int result = fileChooser.showOpenDialog(loadButton);
				if (result == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
				}
				if(selectedFile != null){
					input = new ReadMachine(selectedFile.getAbsolutePath(),(String)encodeBox.getSelectedItem());
					isLoadFile = true;
					filePath = selectedFile.getAbsolutePath();
					setTitle(getTitle()+selectedFile.getName());
					albumInfo = input.getAlbumInfo();
					trackInfo = input.getTrackInfo();
					fileFormat = input.getAudioFormat();
					setTable();
				}
			}
		});
		
		encodeBox = new JComboBox<String>();
		
		for(String s : encode)
			encodeBox.addItem(s);
		encodeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isLoadFile){
					input = new ReadMachine(filePath,(String)encodeBox.getSelectedItem());
					albumInfo = input.getAlbumInfo();
					trackInfo = input.getTrackInfo();
					fileFormat = input.getAudioFormat();
					setTable();
				}
			}
		});
		controlPad.add(encodeBox);
		
		albumInfoArea.add(controlPad);
		albumInfoArea.add(albumTItilePad);
		albumInfoArea.add(albumPerformerPad);
		albumInfoArea.add(albumFilePad);
		albumInfoArea.add(albumGeneratePad);
		albumInfoArea.add(albumDatePad);
		
		trackArea = new JScrollPane();
		contentPane.add(trackArea);
		
		trackTable = new JTable();
		trackTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)){
					if(trackTable.getSelectedRows().length == 0){
						Point p = e.getPoint();
						
						int[] rowNumber = {trackTable.rowAtPoint(p)};
						
						System.out.println(rowNumber);
						
						showPopUp(e,rowNumber);
					}
					showPopUp(e,trackTable.getSelectedRows());
				}
			}
		});
		trackArea.setViewportView(trackTable);
		
		
	}

	private void setTable(){
		albumTitleField.setText(albumInfo[ReadMachine.ALBUM_TITLE]);
		albumPerformerField.setText(albumInfo[ReadMachine.ALBUM_PERFORMER]);
		albumFileField.setText(albumInfo[ReadMachine.ALBUM_FILE]);
		albumGenerateField.setText(albumInfo[ReadMachine.ALBUM_GENRE]);
		albumDateField.setText(albumInfo[ReadMachine.ALBUM_DATE]);
		
		trackTableModel = new MyTableModel(trackInfo);
		trackTable.setModel(trackTableModel);
		trackTable.setPreferredScrollableViewportSize(new Dimension(600,300));
		
		trackTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		trackTable.getColumnModel().getColumn(ReadMachine.TRACK_ORDER).setMaxWidth(50);
		trackTable.getColumnModel().getColumn(ReadMachine.TRACK_MINUTEINDEX).setMaxWidth(70);
		trackTable.getColumnModel().getColumn(ReadMachine.TRACK_SECONDINDEX).setMaxWidth(70);
		trackTable.getColumnModel().getColumn(ReadMachine.TRACK_FRAMEINDEX).setMaxWidth(70); //設定一些 column 的寬度
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		trackTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		trackTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		centerRenderer = (DefaultTableCellRenderer)trackTable.getTableHeader().getDefaultRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		trackTable.getTableHeader().setDefaultRenderer(centerRenderer); //設定column文字顯示置中
	    
		trackArea.setViewportView(trackTable);
		
		Thread getCoverProcess = new Thread(new Runnable(){
			public void run() {
				hasCover= service.hasCover(albumInfo[ReadMachine.ALBUM_PERFORMER]+albumInfo[ReadMachine.ALBUM_TITLE]);
				if(hasCover)
					try{
						albumCoverLabel.setIcon(new ImageIcon(service.getNextAlbumCover()));
					}catch(Exception e){
						
					}
			}
		});
		
		getCoverProcess.start();
		
	}
	
	private void showPopUp(MouseEvent e , int[] rowNumbers){
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuInfo = new JMenuItem("簡介");
		menuInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new TrackInfoDialog(rowNumbers,albumInfo,trackInfo);
			}
		});
		JMenuItem menuPlay = new JMenuItem("播放");
		JMenuItem menuReset = new JMenuItem("重新載入");
		menuReset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
			}
		});
		popupMenu.add(menuInfo);
		if(rowNumbers.length == 1)
			popupMenu.add(menuPlay);
		popupMenu.add(menuReset);
		if(e.isPopupTrigger()){
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	private void setMacThing(){
		Application macApplication = Application.getApplication();
		 
		// About menu handler
		macApplication.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(AboutEvent e) {
				JOptionPane.showMessageDialog(null, "Simple Cue Sheet Editor \nby NasirHo", "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		macApplication.setPreferencesHandler(new PreferencesHandler(){
			@Override
			public void handlePreferences(PreferencesEvent arg0) {
				// TODO Auto-generated method stub
		
			}
	
		});

		Image icon = null;
		try{
			icon = ImageIO.read(CueSheetGUI.class.getResourceAsStream("/icon.png"));
		}catch(Exception ex){
			ex.printStackTrace();
		}

		macApplication.setDockIconImage(icon);
	}
	
	public void someThingChanged(){
		this.hasSomethingChanged = true;
	}
	
	
}
