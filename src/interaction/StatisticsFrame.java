package interaction;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;

public class StatisticsFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StatisticsFrame frame = new StatisticsFrame();
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
	public StatisticsFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		JPanel panelWest = new JPanel();
		contentPane.add(panelWest);
		panelWest.setLayout(new BoxLayout(panelWest, BoxLayout.X_AXIS));
		
		JButton btnNewButton = new JButton("New button");
		contentPane.add(btnNewButton);
		
		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar);
		
		JTextArea textArea = new JTextArea();
		contentPane.add(textArea);
	}

}
