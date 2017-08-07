package teradata.imageToCsv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ImageConverter {
	
	private String filename;
	private PrintWriter pw;
	private StringBuilder sb;
	
	/**
	 * Public constructor.
	 * @param path of the output file
	 * @throws FileNotFoundException if the file couldn't be created/opened
	 */
	public ImageConverter(String path) throws FileNotFoundException {
		filename = path;
		pw = new PrintWriter(new File(filename));
        sb = new StringBuilder();
	}
	
	/**
	 * Append a line containing the image pixel data
	 * @param input image to decode
	 * @param meta information of the file
	 */
	public void imageToSpreadsheet(BufferedImage input, String meta) {
		final byte[] pixels = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
		
	    sb.append(meta+',');
	    
	    final int pixelLength = 1;
	       for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
	            sb.append(((int) pixels[pixel] & 0xff)); // black
	            if(pixel==pixels.length-1) {
	            		sb.append('\n');
	            		//	System.out.println("Hit the end of the file");
	            		}
	            else {
	            		sb.append(',');
	            		}
	         	}
	}
	
	/**
	 * Flush the data and close the file
	 */
	public void flush() {
		pw.write(sb.toString());
	    pw.close();
	}

}
