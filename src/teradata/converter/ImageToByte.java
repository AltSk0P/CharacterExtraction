package teradata.converter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageToByte {
	
	public ImageToByte() {
		
	}

	public byte[] convert(BufferedImage input) {
		final byte[] pixels = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
		byte[] result = new byte[pixels.length];
	    
	    final int pixelLength = 1;
	       for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
	            result[pixel] = (byte) ((int) pixels[pixel] & 0xff); // black
	       }
	    return result;
	}
}