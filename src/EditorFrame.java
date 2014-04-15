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
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class EditorFrame extends JFrame implements MouseListener, ActionListener{

	final String[] columnNames = {"Node Number", "Text"};
	JPanel panel;
	JTable mappingTable;
	String fileText;
	DefaultTableModel tableModel;
	JTextField templateFilePath;
	JButton saveButton;
	JComboBox fileSelector;
	HashMap<String, Element> documents;
	
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
		JLabel filePathLabel = new JLabel("Template files: ");
		
		//file path field
		templateFilePath = new JTextField();
		templateFilePath.addMouseListener(this);
	
		//save button
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		
		fileSelector = new JComboBox();
		fileSelector.addActionListener(this);
		
		tableModel = new DefaultTableModel(columnNames, 0);
		
		
		//mappings table
		JTable mappingTable = new JTable( tableModel);
		((DefaultCellEditor) mappingTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
		mappingTable.setRowHeight(25);
		JScrollPane scrollPane = new JScrollPane(mappingTable);
		mappingTable.setFillsViewportHeight(true);
				
		layout.setHorizontalGroup(
				   layout.createSequentialGroup()
				   .addGroup(layout.createSequentialGroup()
				   	  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				   		   .addGroup(layout.createSequentialGroup()
					           .addComponent(filePathLabel)
					           .addComponent(templateFilePath)
					           .addComponent(saveButton))
				           .addComponent(fileSelector)
				           .addComponent(scrollPane))
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				           ))
				);
				layout.setVerticalGroup(
				   layout.createSequentialGroup()
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(filePathLabel)
				           .addComponent(templateFilePath)
				           .addComponent(saveButton))
				      .addComponent(fileSelector)
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(scrollPane))
				);
		add(panel);
		this.pack();
		this.setVisible(true);
		
	}
	public Element importTemplateFile(String filePath) throws IOException, SAXException, ParserConfigurationException, TransformerException
	{
		Element document = null;
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
			document = importTemplateFile(is);
		}
		return document;
	}

	public Element importTemplateFile(InputStream inputStream)
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
		
		return records;
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		if(evt.getSource() ==templateFilePath )
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileFilter(new FileNameExtensionFilter(
			        "Word Doc" , "docx"));
		    int returnVal = chooser.showOpenDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	
		    	String files = "";
		    	File selectedFiles[] = chooser.getSelectedFiles();
		    	
		    	for(int i=0;i<selectedFiles.length;i++)
		    	{
		    		files += selectedFiles[i].getAbsolutePath();
		    		if(i != selectedFiles.length -1)
		    			files+=',';
		    	}
		    	templateFilePath.setText(files);
		    }
		}
		
	}

	public void insertUpdate(DocumentEvent evt) {
	
	}

	public void actionPerformed(ActionEvent evt) {
		
		if(evt.getSource() == saveButton)
		{
			fixDocuments();
			try {
				exportDocuments();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(evt.getSource() == fileSelector)
		{
			setSelectedDocument((String) fileSelector.getSelectedItem());
		}
	}
	public void fixDocuments()
	{
		clearFileSelector();
		clearTable();
		
		documents = new HashMap<String, Element>();
		
		String fileNames[] = templateFilePath.getText().split(",");
		for(int i=0;i<fileNames.length;i++)
		{
			String fileName = fileNames[i].trim();
			if(fileName.length()>0)
			{
				Element document = null;
				try {
					document = importTemplateFile(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				if(document!= null)
				{
					documents.put(fileName, document);
					fileSelector.addItem(fileName);
				}
			}
		}
	}
	/**
	 * Exports the document
	 * @throws Exception
	 */
	public void exportDocuments() throws Exception
	{
		Set<String> keySet=  documents.keySet();
		String[] keys = keySet.toArray(new String[keySet.size()]);
		
		for(int i=0;i<keys.length;i++)
		{
			String newPath = ZipUtil.extractFolder(keys[i]);
			
			// write the content into xml file
			DOMSource source = new DOMSource(documents.get(keys[i]));
			
			
			File documentXMLFile = new File(newPath + "\\word\\document.xml");
			
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			StreamResult result = new StreamResult(documentXMLFile);
			transformer.transform(source, result);

			File newFile = new File(newPath);
			System.out.println("PAth" + newPath);
			ZipUtil.zipDirectory(newFile, newPath+ " Fixed.docx");
			//delete the file we just created
			removeDir(newFile);
		}
		
	}
	public void clearFileSelector()
	{
		fileSelector.removeAllItems();
	}
	/**
	 * Clears the main table of entries so we can display a new table
	 */
	public void clearTable()
	{
		for(int i=tableModel.getRowCount()-1;i>=0;i--)
		{
			tableModel.removeRow(i);
		}
	}
	/**
	 * Sets the document you want to preview
	 * @param selectedDocument
	 */
	public void setSelectedDocument(String selectedDocument)
	{
		clearTable();
		if(documents.containsKey(selectedDocument))
		{
			Element document = documents.get(selectedDocument);
			
			NodeList nList = document.getElementsByTagName("w:t");

			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				tableModel.addRow(new String[] { "" + i, node.getTextContent() });
			}
		}
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	
	/**
	 * Removes a directory. We create an extra directory when making new zip, so this cleans it up
	 * @param fIn
	 */
	public void removeDir(File fIn) {
		int i;
		File f;
		String[] as;

		if (fIn.isDirectory()) 
		{
			as = fIn.list(); 

			for (i = 0; i < as.length; i++) {
				f = new File(fIn, as[i]); 
				removeDir(f); 
			}

			System.out.println("Remove " + fIn + " directory");
			fIn.delete();
			return;
		}

		System.out.println("Remove " + fIn + " file");
		fIn.delete();
		return;
	}


	
}
