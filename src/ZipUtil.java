import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ZipUtil {
	List<String> fileList;
	private String sourceFolder;
	final static String documentPathInZip= "word/document.xml";
	
	private ZipUtil(String sourceFolder) {
		this.sourceFolder = sourceFolder;
		fileList = new ArrayList<String>();
		this.sourceFolder = sourceFolder;
	}

	public static void zipDirectory(String sourceFolder) {
		ZipUtil appZip = new ZipUtil(sourceFolder);
		appZip.generateFileList(new File(sourceFolder));
		appZip.zipIt(sourceFolder + ".docx");
	}

	/**
	 * Zip it
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */
	public void zipIt(String zipFile) {

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);

			for (String file : this.fileList) {

				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(sourceFolder
						+ File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			// remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	public void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}

	}

	/**
	 * Format the file path for zip
	 * 
	 * @param file
	 *            file path
	 * @return Formatted file path
	 */
	private String generateZipEntry(String file) {
		return file.substring(sourceFolder.length() + 1, file.length());
	}
	
	
	protected  File file(final File root, final ZipEntry entry)
		    throws IOException {

		    final File file = new File(root, entry.getName());

		    File parent = file;
		    if (!entry.isDirectory()) {
		        final String name = entry.getName();
		        final int index = name.lastIndexOf('/');
		        if (index != -1) {
		            parent = new File(root, name.substring(0, index));
		        }
		    }
		    if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
		        throw new IOException(
		            "failed to create a directory: " + parent.getPath());
		    }

		    return file;
		}
	
	public static void saveZip()
	{

		Path myFilePath = Paths.get("C:\\Users\\Nick\\Desktop\\document.xml");

	    Path zipFilePath = Paths.get("C:\\Users\\Nick\\Desktop\\resume ex - Copy.docx");
	    FileSystem fs;
	    try {
	        fs = FileSystems.newFileSystem(zipFilePath, null);

	        Path fileInsideZipPath = fs.getPath("\\word\\document.xml");
	        Files.copy(myFilePath, fileInsideZipPath , new CopyOption[]{
	        	      StandardCopyOption.REPLACE_EXISTING
	        	    });
	       // fs.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	public static InputStream unzip(final ZipFile zipfile)
		    throws IOException, SAXException, ParserConfigurationException {
			InputStream is = null;
		    final Enumeration<? extends ZipEntry> entries = zipfile.entries();
		    while (entries.hasMoreElements()) {
		        final ZipEntry entry = entries.nextElement();
		        if (!entry.isDirectory() && entry.getName().equals(documentPathInZip)) {
		        	
		        	 is  = zipfile.getInputStream(entry);
		        }
		    }
		    return is;
		}
}
