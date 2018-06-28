package visualised;

import java.awt.image.BufferedImage;

public interface Visualised {
	
	void drawOnBufferedImage(BufferedImage image, int t);
	
	void drawBufferedImage(int t);
	
	void drawLatLonBufferedImage(int t);

}
