import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import teradata.extractor.ExtractionException;

public class Comparer {

	private BufferedImage[] images;
	private int[] img1, img2, img3;
	
	Comparer(){
		images = new BufferedImage[3];
	}
	
	void setImg(int num, BufferedImage input) {
		images[num] = input;
		switch(num) {
		case 0: img1 = imageToArray(images[num]);
			break;
		case 1: img2 = imageToArray(images[num]);
			break;
		case 2: img3 = imageToArray(images[num]);
			break;
		}
	}
	
	private int[] imageToArray(BufferedImage image) {
	      
	      int width = image.getWidth();
	      int height = image.getHeight();
	      
	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData(); 

	      int[] result = new int[height*width];
	      final int pixelLength = 1;
	         for (int pixel = 0, i = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += -16777216; // 255 alpha
	            argb += ((int) pixels[pixel] & 0xff); // blue
	            result[i] = argb;
	            i++;
	         }
	      return result;
	   }
	
	boolean compare() {
		for(int i=0;i<img1.length;i++) {
			if(img1[i]!=img2[i]) {
				return false;
			}
			if(img1[i]!=img3[i]) {
				return false;
			}
			if(img2[i]!=img3[i]) {
				return false;
			}
		}
		return true;
	}
}
