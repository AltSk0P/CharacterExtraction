package teradata.extractor;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

public class FieldFinder {
	
	private int width, height;
	
	private static final int pointOneMinX = 207;
	private static final int pointOneMaxX = 223;
	private static final int pointOneMaxY = 260;
	private static final int pointOneMinY = 60;
	
	private static final int pointTwoMinY = 40;
	
	private static final int[] NEIGHBOR_PRIORITY = {0,1,-1,2,-2};

	public FieldFinder() {
		
	}
	
	public BufferedImage trimField(BufferedImage input) throws IOException {
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
		
		return input.getSubimage(x, y, w, h);
	}
	
	private Point findFieldStart(int[] image) {
		Point topLeft = new Point();
		int color;
		
		for(int i=0,row=pointOneMinY,col=pointOneMaxX;col>pointOneMinX;row++)
        {
			//System.out.println("We check at "+col+"x"+row);
            color = getColorValue(image[col+row*width]);
            if (color!=255) {
            		int next = neighbors(image,col,row);
            		while (next>-3) {
            			col++;
            			row += next;
            			next = neighbors(image,col,row);
            		}
            		topLeft.x = col;
            		topLeft.y = row;
            		break;
            		}
            else if (row==pointOneMaxY) {
            		row = pointOneMinY;
            		col--;
            }
            }
		
		return topLeft;
	}
	
	private Point findLine(int[] image, Point topLeft) throws IOException {
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
            		//TODO throw an Exception
            	throw new IOException("Couldn't locate the line");
            }
            }
		return botLeft;
	}
	
	private Point findLineEnd(int[] image, Point botLeft) {
		int col = botLeft.x;
		int row = botLeft.y;
		int next = neighbors(image,col,row);
		while(next > -3 && col < width) {
			col++;
			row += next;
			next = neighbors(image,col,row);
		}
		Point botRight = new Point(col,row);
		return botRight;
	}
	
	private int neighbors(int[] image, int col, int row) {
		int color;
		//System.out.println(col+" , "+row);
		for (int i=0;i<NEIGHBOR_PRIORITY.length;i++)
		{
			int n = NEIGHBOR_PRIORITY[i];
			color = getColorValue(image[col+1+(row+n)*width]);
			if (color!=255) {
	            return n;
			}
		}
		return -3;
	}
	
	private int[] imageToArray(BufferedImage image) throws IOException {

		
		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("Input is grayscale ");
        }else {
        		System.out.println("Input isn't grayscale, converting");
            image = convertToGray(image);
        }
	      
	      width = image.getWidth();
	      height = image.getHeight();
	      
	      if(!(height==720&width==1636)) {
	    	  throw new IOException("The image isn't of the right size");
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
