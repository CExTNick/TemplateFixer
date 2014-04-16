import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class ZipUtil {
	static List<String> filesListInDir = new ArrayList<String>();
	final static String documentPathInZip= "word/document.xml";
	
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
	
	
    public static ZipFile addFileToExistingZip(File zipFile, File versionFile, String relativePath) throws IOException{
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();
System.out.println("path: "+ zipFile.getAbsolutePath());
        boolean renameOk=zipFile.renameTo(tempFile);
        if (!renameOk)
        {
            throw new RuntimeException("could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[4096 * 1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean toBeDeleted = false;
                if ((relativePath+versionFile.getName()).indexOf(name) != -1) {
                    toBeDeleted = true;
                }
            if(!toBeDeleted){
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams
        zin.close();
        // Compress the files
        InputStream in = new FileInputStream(versionFile);
        String fName = versionFile.getName();
        // Add ZIP entry to output stream.
        out.putNextEntry(new ZipEntry(relativePath+fName));
        // Transfer bytes from the file to the ZIP file
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        // Complete the entry
        out.closeEntry();
        in.close();
        // Complete the ZIP file
        out.close();
        tempFile.delete();
        return new ZipFile(zipFile);
    }
}
