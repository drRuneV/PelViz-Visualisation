package interaction;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import graphics.GraphicsUtil;

public class ImageSaver {

	BufferedImage bi;
	File file ;//=  GraphicsUtil.saveImagePath(this);
	String path;
	String prefix="";
	boolean active=false;
	int count=0;
	int maxImages= 365;
	// Whether we are going to use just a region of a component
	boolean useRegion=true;
	private Rectangle region= null;
	
	
	
	
	/**
	 * Constructor
	 * @param file the file we want to save to
	 * @param region the pixel region we are saving
	 */
	public ImageSaver(File file, Rectangle region){
		this.file= file;
		this.region= region;
		//We are using a specific region of the component we are saving
		useRegion=(region!=null);
		path= file.getAbsolutePath();
		count=0;
		active=true;
//	System.out.println("File location:"+path);
	}

	
	/**
	 * Saving one image at a time
	 * @param n
	 * @param component
	 */
	public void save(int n, Component component){

		try {
			count++;
			//Save only the specified region , Typical one distribution
			if (useRegion) {
				Point p = new Point(0, 0);
				SwingUtilities.convertPointToScreen(p, component);
				Rectangle r= new Rectangle(p.x+region.x, p.y+region.y, region.width, region.height) ;
				bi=GraphicsUtil.createRobotImage(r);						
			}
			// Save the image the whole component
			else{
				bi= GraphicsUtil.createImageOfComponent(component);
			}
			String prezero=  (n<10) ? "_00" : (n<100) ? "_0" : "_" ;
			prefix =  prezero + String.format("%d", n);
			File file1=  new File( path+prefix +".png");
			ImageIO.write(bi, "PNG", file1);
//			System.out.println("Saving image: "+path+prefix+" "+n);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (AWTException e1) {
			e1.printStackTrace();
		}

		if (count> maxImages) {
			active=false;
			count=0;
		}

	}


	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}


	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}


	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	/**
	 * @return the useRegion
	 */
	public boolean isUseRegion() {
		return useRegion;
	}


	/**
	 * @param useRegion the useRegion to set
	 */
	public void setUseRegion(boolean useRegion) {
		this.useRegion = useRegion;
	}


	/**
	 * @return the region
	 */
	public Rectangle getRegion() {
		return region;
	}


	/**
	 * @param region the region to set
	 */
	public void setRegion(Rectangle region) {
		this.region = region;
	}



	
}
