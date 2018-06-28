package basicGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;




/**
 * Displays a rich text document as a keyboard help
 * @author Admin
 *
 */
public class KeyboardHelp {
  
	StyledDocument document = null;
	static String defaultPath=   "res/Keyboard S.rtf";
	String path="";
	Point location= new Point(); 
	
	
	
	/**
	 * Constructor
	 * @param path
	 */
	public KeyboardHelp(String path, Point location) {
		this.path=path;
		this.location  = location;

		if (path==null) {
			this.path= new String(defaultPath);
//			System.out.println("null: "+this.path);
		}

		// 
		try {
			show();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}
	
	
public static void main(String args[]) throws IOException, BadLocationException {
		KeyboardHelp k=new KeyboardHelp(null,new Point(300, 50));//defaultPath);
		
	}

/**
 * Shows the keyboard help document in a separate swing JFrame
 * @throws BadLocationException 
 * @throws IOException 
 */
	public void show() throws IOException, BadLocationException {
	// JFrame
	JFrame frame = new JFrame("Keyboard shortcuts");
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);// EXIT_ON_CLOSE);
	// JScrollPane  with the rich text
	JScrollPane scrollPane = showRTF();// new JScrollPane(textPane);
	frame.add(scrollPane, BorderLayout.CENTER);
	// 
	frame.setSize(500, 700);
	frame.setLocation(location);
	frame.setVisible(true);	
}


/**
 * Shows the rich text in a scroll pane
 * @return a scroll pane containing the Rich text
 * @throws IOException
 * @throws BadLocationException
 */
	private JScrollPane showRTF() throws IOException, BadLocationException {
	
	// Create an RTF editor window
	RTFEditorKit rtf = new RTFEditorKit();
	JEditorPane editor = new JEditorPane();
	editor.setEditorKit( rtf );
	editor.setEditable(false);
	editor.setBackground( Color.white );
	String m = "cannot find the file!" ;

	
	// Load an RTF file into the editor
	FileInputStream inputStream;
	try {
		File file = new File(path);
		if (file.exists()) {
			inputStream = new FileInputStream(path);
			
			// Read the content of the file into the editor
			rtf.read( inputStream, editor.getDocument(), 0 );
		}
		else System.out.println(m);
		
	} catch (FileNotFoundException e) {
		e.printStackTrace();

		System.out.println(m);
//		editor.getText().
	}
	
	// This text could be big so add a scroll pane
	JScrollPane scroller = new JScrollPane(editor);

	return scroller;
}



}


