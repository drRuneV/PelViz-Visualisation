package basicGUI;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ProgressFrame extends JFrame{
	
	private JProgressBar progressBar;
	private JTextArea taskOutput;
	private int count=0;
//	private Task task;

	public ProgressFrame() {

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		taskOutput = new JTextArea(5, 10);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);

		JPanel panel = new JPanel();
		panel.add(progressBar);

		add(panel, BorderLayout.PAGE_START);
		add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		
		this.setAlwaysOnTop(true);
		this.setMinimumSize(new java.awt.Dimension(280, 350));
		this.pack();
		this.setLocation(300,200);
	}

	/**
	 * 
	 * Updates the progress frame by sending a message to the text  output area
	 * @param message
	 */
	public void update(String message){
		count+=10;
		progressBar.setValue(count);
//		taskOutput.append(String.format("Completed %d%% of task.\n", progressBar.getValue()));
		taskOutput.append(message+"\n");
		
	}


}
