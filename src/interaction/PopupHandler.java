package interaction;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import graphics.ColorGradient;
import graphics.DistributionsPanel;
import graphics.GradientPanel;


/**
 * 
 * @author a1500
 *
 */
public class PopupHandler {

	  final JPopupMenu popup = new JPopupMenu();
	  public DistributionsPanel panel= null;

	  /**
	   * Constructor
	   * @param panel
	   */
	  public PopupHandler(DistributionsPanel panel) {
		  this.panel= panel;
		  createMenu();
	  }
	  


	  /**
	   * Triggers showing the pop-up menu
	   * @param e
	   */
      public void showPopup(MouseEvent e) {
    	  
    	  
          if (e.isPopupTrigger()) {
              popup.show(e.getComponent(),
                      e.getX(), e.getY());
          }
      }
	  
	  /**
	   * Creates the pop-up menu
	   */
	  public void createMenu(){
		  
		  Font font= new Font("Times", Font.PLAIN, 14); 

		  popup.add(menuGradient(font));
		  // New File menu item
		  popup.add(menuTopography(font));
		  //
		  popup.add(menuMask(font));
		  //
		  popup.add(menuMode(font));
	  }
	  
	  private JMenuItem menuMode(Font font) {
		  JMenuItem menuItem = new JMenuItem("Mode ...");
			return menuItem;
		
	  }



	private JMenuItem menuMask(Font font) {
		  JMenuItem menuItem = new JMenuItem("Mask ...");
			return menuItem;
		
	}



	private JMenuItem menuTopography(Font font) {
		JMenuItem menuItem = new JMenuItem("Topography ...");
		return menuItem;
	}



	private JMenuItem menuGradient(Font font){
		  
		  JMenuItem menuItem = new JMenuItem("Define gradient...");
		  //                new ImageIcon("images/newproject.png"));
		  menuItem.setMnemonic(KeyEvent.VK_H);
//		  menuItem.getAccessibleContext().setAccessibleDescription("Define gradient");
		  menuItem.setFont(font);
		  menuItem.addActionListener(new ActionListener() {
			  
			  /**
			   * Action for pop-up Gradient
			   */
			  public void actionPerformed(ActionEvent e) {
				  ColorGradient g= panel.getDistribution().getGradient();
				  GradientPanel p= new GradientPanel(g);
				  p.show();
			  }
		  });

		  return menuItem;
	  }

}
