import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class TemplateEditor {
	
	public static void main(String args[])
	{
		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new EditorFrame();
	}
}
