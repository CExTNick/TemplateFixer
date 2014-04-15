import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class EditorFrame extends JFrame implements MouseListener, DocumentListener, ActionListener{

	final String[] columnNames = {"New", "Old"};
	JPanel panel;
	JTextArea filePreviewArea;
	JTable mappingTable;
	String fileText;
	DefaultTableModel tableModel;
	JTextField templateFilePath;
	JButton saveButton;
	JTextField selectionField;
	
	public EditorFrame()
	{
		super("Template Editor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
				
		//file path label
		JLabel filePathLabel = new JLabel("Template file: ");
		
		//file path field
		templateFilePath = new JTextField();
		templateFilePath.addMouseListener(this);
		templateFilePath.getDocument().addDocumentListener(this);
	
		//save button
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		
		
		tableModel = new DefaultTableModel(columnNames, 0);

		
		//mappings table
		JTable mappingTable = new JTable( tableModel);
		((DefaultCellEditor) mappingTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
		mappingTable.setRowHeight(25);
		JScrollPane scrollPane = new JScrollPane(mappingTable);
		mappingTable.setFillsViewportHeight(true);
		
		//preview text area
		filePreviewArea = new JTextArea();
		filePreviewArea.setLineWrap(true);
		filePreviewArea.setWrapStyleWord(true);
		JScrollPane filePreviewScrollPane = new JScrollPane(filePreviewArea);
		
		JTextField selectionField = new JTextField();
		layout.setHorizontalGroup(
				   layout.createSequentialGroup()
				   .addGroup(layout.createSequentialGroup()
				   	  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				   		   .addGroup(layout.createSequentialGroup()
					           .addComponent(filePathLabel)
					           .addComponent(templateFilePath))
				           .addComponent(scrollPane)
				           .addComponent(selectionField))
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				           .addComponent(saveButton)
				           .addComponent(filePreviewScrollPane)))
				);
				layout.setVerticalGroup(
				   layout.createSequentialGroup()
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(filePathLabel)
				           .addComponent(templateFilePath)
				           .addComponent(saveButton))
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(scrollPane)
				           .addComponent(filePreviewScrollPane))
				      .addComponent(selectionField)
				);
		add(panel);
		this.pack();
		this.setSize(1100, this.getHeight());
		this.setVisible(true);
		
	}
	public void importTemplateFile(String filePath) throws IOException, SAXException, ParserConfigurationException, TransformerException
	{
		InputStream is = null;
		if(filePath.endsWith(".xml"))
		{
			System.out.println("parsing xml");
			is = new FileInputStream(filePath);
		}
		else if(filePath.endsWith(".docx") || filePath.endsWith(".zip"))
		{
			System.out.println("parsing docx or zip");
			ZipFile zipFile = new ZipFile(filePath);
			is = ZipUtil.unzip(zipFile);
		}
		if(is != null)
		{
			System.out.println("reading file");
			importTemplateFile(is);
		}
	}

	public void importTemplateFile(InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException,
			TransformerException {

		InputStreamReader in = new InputStreamReader(inputStream, "utf-8");

		BufferedReader reader = new BufferedReader(in);

		InputSource input = new InputSource(reader);

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Element records = builder.parse(input).getDocumentElement();
		TemplateFixer templateFixer = new TemplateFixer();
		templateFixer.fix(records);
		
		reader.close();
		in.close();
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(records);
		
		StreamResult result = new StreamResult(new File(
				"C:\\Users\\Nick\\Desktop\\document.xml"));
		transformer.transform(source, result);
		
		NodeList nList = records.getElementsByTagName("w:t");

		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			tableModel.addRow(new String[] { "" + i, node.getTextContent() });
		}

	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		if(evt.getSource() ==templateFilePath )
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(
			        "Word Doc" , "docx"));
		    int returnVal = chooser.showOpenDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	templateFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
		    }
		}
		
	}

	public void insertUpdate(DocumentEvent evt) {
		try {
			importTemplateFile(templateFilePath.getText());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent evt) {
		ZipUtil.saveZip();
		
	}
	
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	
	public void removeUpdate(DocumentEvent e) {}
	public void changedUpdate(DocumentEvent evt) {}
	
}
