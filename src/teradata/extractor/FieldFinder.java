package teradata.extractor;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

/**
 * This class is used to recognize signature points on a check and extract a field which should contain the characters to recognize
 */
public class FieldFinder {
	
	private int width, height;
	
	private int pointOneMinX = 203;
	private int pointOneMaxX = 228;
	private int pointOneMaxY = 294;
	private int pointOneMinY = 128;
	
	private int pointTwoMinY = 40;
	
	private static final int minWidth = 700;
	private static final int minHeight = 50;
	
	private static final int[] NEIGHBOR_PRIORITY = {0,1,-1,2,-2};

	public FieldFinder() {
		
	}
	
	/**
	 * A public constructor which also allows overriding of the origin points
	 * @param minx1
	 * @param maxx1
	 * @param miny1
	 * @param maxy1
	 * @param miny2
	 */
	public FieldFinder(int minx1, int maxx1, int miny1, int maxy1, int miny2) {
		pointOneMinX = minx1;
		pointOneMaxX = maxx1;
		pointOneMinY = miny1;
		pointOneMaxY = maxy1;
		pointTwoMinY = miny2;
	}
	
	public BufferedImage trimField(BufferedImage input) throws ExtractionException {
		int[] image = turnBackground(imageToArray(input));
		Point topLeft = findFieldStart(image);
		Point botLeft = findLine(image,topLeft);
		Point botRight = findLineEnd(image,botLeft);
		
		System.out.println("topLeft = ["+topLeft.x+","+topLeft.y+"]");
		System.out.println("botLeft = ["+botLeft.x+","+botLeft.y+"]");
		System.out.println("botRight = ["+botRight.x+","+botRight.y+"]");
		
		int x,y,w,h,bigY;
		x = topLeft.x+4;
		y = topLeft.y-30;
		w = (botRight.x-4)-botLeft.x;
		bigY = getMin(botLeft.y, botRight.y)-2;
		h = bigY-y;
		
		System.out.println("x= "+x+" y="+y+" w="+w+" h="+h);
		
		if (w<minWidth || h<minHeight || Math.abs(botLeft.y-botRight.y)>14) {
			throw new ExtractionException("Trimming error occured");
		}
		
		return input.getSubimage(x, y, w, h);
	}
	
	/**
	 * Find the rightmost-topmost pixel of the letter E on a check image
	 * @param image
	 * @return found Point
	 */
	private Point findFieldStart(int[] image) throws ExtractionException{
		Point topLeft = new Point();
		int color;
		
		for(int i=0,row=pointOneMinY,col=pointOneMaxX;col>pointOneMinX;row++)
        {
			//System.out.println("We check at "+col+"x"+row);
            color = getColorValue(image[col+row*width]);
            //System.out.println("Lookin at "+col+"x"+row);
            if (color!=255) {
            		//System.out.println("2Lookin at "+col+"x"+row);
            		int next = neighbors(image,col,row,1);
            		while (next>-3 && col < width/2 && col>pointOneMinX) {
            			col++;
            			row += next;
            			next = neighbors(image,col,row,1);
            		}
            		//System.out.println("3Lookin at "+col+"x"+row);
            		topLeft.x = col;
            		topLeft.y = row;
            		break;
            		}
            else if (row==pointOneMaxY && col>pointOneMinX) {
            		row = pointOneMinY;
            		col--;
            }
            }
		System.out.println("topleft is "+topLeft.x+"x"+topLeft.y);
		return topLeft;
	}
	
	private Point findLine(int[] image, Point topLeft) throws ExtractionException {
		Point botLeft = new Point();
		int color;
		int startPos = pointTwoMinY+topLeft.y;
		int endPos = startPos+60;
		
		for(int i=0,row=startPos,col=topLeft.x;row<endPos;row++)
        {
            color = getColorValue(image[col+row*width]);
            if (color!=255) {
            		botLeft.y = row;
            		botLeft.x = col;
            		break;
            		}
            else if (row==endPos) {
            	throw new ExtractionException("Couldn't locate the line");
            }
            }
		return botLeft;
	}
	
	private Point findLineEnd(int[] image, Point botLeft) throws ExtractionException {
		int col = botLeft.x;
		int row = botLeft.y;
		System.out.println(col+"x"+row);
		int next = neighbors(image,col,row,6);
		while(next > -3 && row > botLeft.y-30) {
			col++;
			row += next;
			if (col < 1150) {
				next = neighbors(image,col,row,6);
			}
			else {
				next = neighbors(image,col,row,2);
			}
		}
		Point botRight = new Point(col,row);
		return botRight;
	}
	
	private int neighbors(int[] image, int col, int row, int jump) throws ExtractionException{
		int color;
		try {
		for (int i=0;i<NEIGHBOR_PRIORITY.length;i++)
		{
			for(int m=1;m<=jump;m++) {
				int n = NEIGHBOR_PRIORITY[i];
				color = getColorValue(image[col+m+(row+n)*width]);
				if (color!=255) {
		            return n;
				}
			}
		}
		return -3;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new ExtractionException("Trimming error");
		}
	}
	
	private int[] imageToArray(BufferedImage image) throws ExtractionException {

		
		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("Input is grayscale ");
        }else {
        		System.out.println("Input isn't grayscale, converting");
            image = convertToGray(image);
        }
	      
	      width = image.getWidth();
	      height = image.getHeight();
	      
	      if(!(height==720&width==1636)) {
	    	  throw new ExtractionException("The image isn't of the right size");
	      }
	      
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
	
	public static BufferedImage convertToGray(BufferedImage biColor)
	{
	    BufferedImage biGray = new BufferedImage(biColor.getWidth(), biColor.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	    biGray.getGraphics().drawImage(biColor, 0, 0, null);
	    return biGray;
	}
	
	private int getColorValue(int input) {
		return (input>>0)&0xFF;
	}

	private int getMin(int num1, int num2) {
		if (num1<num2) {
			return num1;
		}
		else return num2;
	}
	
	private int[] turnBackground(int[] input) {
		int[] output = new int[input.length];
		int color;
		for(int i=0;i<output.length;i++) {
			color = getColorValue(input[i]);
			if (color>240) {
	            output[i]=-16776961;
			}
			else {
				output[i] = input[i];
			}
		}
		return output;
	}
}
