package teradata.extractor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * This class is responsible for normalizing and centering a character image
 * during the character extraction process
 * The output image is accessible through calling .image
 */
class Character {
	
	private int[] input, boxedChar;
	private int width, height, boxWidth, boxHeight;
	private int IMG_WIDTH, IMG_HEIGHT, MARGIN;
	private int[] sidesPos; //1 - A, 2 - B, 3 - C, 4 - D
	private Rectangle charBox;
	private int backgroundColor = 250;
	private static final int MIN_SIZE = 7;
	
	public double scaleFactor=0;
	public BufferedImage image;

	/**
	 * Public constructor for Character class
	 * @param image Character input
	 * @param dimension desired output dimension, such as 28 (output image size will be 28x28)
	 * @param margins (margins to put character into)
	 */
	public Character(BufferedImage image, int dimension, int margins) {
		input = imageToArray(image);
		IMG_WIDTH = IMG_HEIGHT = dimension;
		MARGIN = margins;
		getCharacter();
	}
	
	/**
	 * Another public constructor
	 * @param image Character input
	 * @param dimension desired output dimension, such as 28 (output image size will be 28x28)
	 * @param margins margins (margins to put character into)
	 * @param scale forced scaling factor
	 */
	public Character(BufferedImage image, int dimension, int margins, double scale) {
		input = imageToArray(image);
		IMG_WIDTH = IMG_HEIGHT = dimension;
		MARGIN = margins;
		scaleFactor = scale;
		getCharacter();
	}
	
	/**
	 * Use this function to center & scale your character and return new image
	 * @return centered&scaled character image
	 */
	public void getCharacter() {
		findSides();
		boxing();
		image = arrayToImage();
	}
	
	/**
	 * Converting BufferedImage to an int[] array of colors representing grayscale intensity 
	 * If the image isn't grayscale, convert it to it prior to converting to an array
	 * @param image processed image
	 * @return the array
	 */
	private int[] imageToArray(BufferedImage image) {

	      if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
	            // 
	        }else {
	            image = convertToGray(image);
	        }
	      
	      image = inflateFrame(image);
	      
	      width = image.getWidth();
	      height = image.getHeight();
	      
	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

	      int[] result = new int[height*width];
	      //System.out.println("Created an array of "+result.length);
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
	
	/**
	 * Add a white frame to the input image. Necessary in case if the character is touching the boundaries of the image
	 * @param sourceImage
	 * @return image with a white frame
	 */
	public BufferedImage inflateFrame(BufferedImage sourceImage) {
		int sourceWidth = sourceImage.getWidth();
		int sourceHeight = sourceImage.getHeight();
		BufferedImage outputImage = new BufferedImage(sourceWidth+4,sourceHeight+4,BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = outputImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, outputImage.getWidth(), outputImage.getHeight());
        g.dispose();
        Graphics m = outputImage.createGraphics();
        m.drawImage(sourceImage, 2, 2, sourceWidth, sourceHeight, null);
        m.dispose();
        return outputImage;
	}
	
	/**
	 * Convert Color BufferedImage to Grayscale BufferedImage
	 * @param biColor input
	 * @return grayscale biColor
	 */
	public static BufferedImage convertToGray(BufferedImage biColor)
	{
	    BufferedImage biGray = new BufferedImage(biColor.getWidth(), biColor.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	    biGray.getGraphics().drawImage(biColor, 0, 0, null);
	    return biGray;
	}
	
	/**
	 * Find the topmost, leftmost, rightmost and bottommost pixels of the character to define a boundary box
	 */
	private void findSides() {
		int color;
        boolean sidesInit = false;
        
        sidesPos = new int[4];
        
        for(int i=0,row=0,col=0;i<input.length;i++)
        {
            color = getColorValue(input[i]);
            if(color < backgroundColor) {
                if (!sidesInit) {
                    sidesPos[0] = row;
                    sidesPos[1] = col;
                    sidesPos[2] = col;
                    sidesPos[3] = row;
                    sidesInit = true;
                }
                if (sidesPos[0] > row)
                {
                    sidesPos[0] = row;
                }
                if (sidesPos[1] > col)
                {
                    sidesPos[1] = col;
                }
                if (sidesPos[2] < col)
                {
                    sidesPos[2] = col;
                }
                if (sidesPos[3] < row) 
                {
                    sidesPos[3] = row;
                }
            }
            col++;
            if (col == width){
                col = 0;
                row++;
            }
        }
        
        /*System.out.println("Top sides is at y "+sidesPos[0]);
        System.out.println("Left sides is at x "+sidesPos[1]);
        System.out.println("Right sides is at x "+sidesPos[2]);
        System.out.println("Bottom sides is at y "+sidesPos[3]);*/
	}
	
	/**
	 * Define a boundary box for extracting the character
	 */
	private void boxing() {
		int rectX, rectY;
		boxHeight = sidesPos[3]-sidesPos[0];
		boxWidth = sidesPos[2] - sidesPos[1];
		
		rectX = sidesPos[1]-1;
		rectY = sidesPos[0]-1;
		
		charBox = new Rectangle(rectX,rectY,boxWidth+3,boxHeight+3);
        //System.out.println("Box created at "+rectX+"x"+rectY+" with width "+boxWidth+" and height "+boxHeight);
        verifyBoxing();
	}

	/**
	 * Used to make sure that the character was boxed correctly and no pixel was left out
	 * Was mostly used in debugging
	 * @return true if all pixels are boxed, false if not
	 */
	private boolean verifyBoxing() {
		int color;
		boolean containsAll=true;
		int[] newChar = new int[(int) (charBox.getWidth()*charBox.getHeight())];
		for(int i=0,row=0,col=0,b=0;i<input.length;i++)
        {
            color = getColorValue(input[i]);
            if(color < backgroundColor) {//if(color >= bgColor+colorDistance)
                if (!charBox.contains(col, row))
                {
                	System.out.println("Pixel at "+col+"x"+row+" isn't boxed");
                	containsAll = false;
                	break;
                }
            }
            if (charBox.contains(col,row))
            {
            	newChar[b] = input[i];
            	b++;
            }
            col++;
            if (col == width){
                col = 0;
                row++;
            }
        }
		//CharScanner scanner = new CharScanner(4);
		boxedChar = newChar;
		//boxedChar = scanner.returnSingleChar(newChar, (int)(charBox.getWidth()));
		//System.out.println("boxedChar length is "+boxedChar.length+" versus original newChar "+newChar.length+" and input "+input.length);
		return containsAll;
	}
	
	/**
	 * Converting the output array back to BufferedImage. The actual new image is built in drawNew() method
	 * charImg is supplied to it which is essentially the extracted boundary box from the original image
	 * @return output img
	 */
	private BufferedImage arrayToImage() {
		boxHeight = (int)(charBox.getHeight());
		boxWidth = boxedChar.length/boxHeight;
		BufferedImage charImg = new BufferedImage(boxWidth,boxHeight,BufferedImage.TYPE_BYTE_GRAY);
		charImg.getRaster().setPixels(0, 0, boxWidth, boxHeight, boxedChar);
		return drawNew(charImg);
	}

	/**
	 * Creates a new image for the output. Resizes and centers the character in it
	 * @param charImg character image
	 * @return
	 */
	private BufferedImage drawNew(BufferedImage charImg) {
		BufferedImage outputImage = new BufferedImage(IMG_WIDTH,IMG_HEIGHT,BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = outputImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, outputImage.getWidth(), outputImage.getHeight());
        g.dispose();
        
        final int CHAR_WIDTH = IMG_WIDTH-(MARGIN-1)*2;
        final int CHAR_HEIGHT = IMG_HEIGHT-(MARGIN-1)*2;
        
        double aspectRatio = (double) boxHeight / boxWidth;
        //System.out.println("Aspect Ratio is "+aspectRatio);
        System.out.println("Before transformation. ScaleFactor is "+scaleFactor);
        int newWidth, newHeight;
        if (scaleFactor==0) {
	        if (boxHeight>boxWidth) {
	        		System.out.println("Restricted by height, char_height="+CHAR_HEIGHT);
	        		newHeight = CHAR_HEIGHT;
	        		newWidth = (int) (newHeight / aspectRatio);
	        		scaleFactor = (double) newHeight / (double) boxHeight;
	        }
	        else {
	        		//System.out.println("Restricted by width");
	        		newWidth = CHAR_WIDTH;
	        		newHeight = (int) (newWidth * aspectRatio);
	        		scaleFactor = (double) newWidth / (double) boxWidth;
	        }
	        System.out.println("After transformation. ScaleFactor is "+scaleFactor);
        }
        else
	    {
	        	if (boxHeight>boxWidth) {
	        		int scaled = (int) (boxHeight * scaleFactor);
	        		if (scaled<=CHAR_HEIGHT & scaled>=MIN_SIZE) {
		        		newHeight = (int) (boxHeight * scaleFactor);
		        		newWidth = (int) (newHeight / aspectRatio);}
	        		else
	        		{
	        			newHeight = CHAR_HEIGHT;
		        		newWidth = (int) (newHeight / aspectRatio);
		        		scaleFactor = (double) newHeight / (double) boxHeight;
	        		}
	        }
	        else {
	        		int scaled = (int) (boxWidth * scaleFactor);
	        		if (scaled<=CHAR_WIDTH & scaled>=MIN_SIZE) {
		        		newWidth = (int) (boxWidth * scaleFactor);
		        		newHeight = (int) (newWidth * aspectRatio);}
	        		else
	        		{
	        			newWidth = CHAR_WIDTH;
		        		newHeight = (int) (newWidth * aspectRatio);
		        		scaleFactor = (double) newWidth / (double) boxWidth;
	        		}
	        }
	        	System.out.println("Scaling by "+scaleFactor+" so the boxHeight"+boxHeight+" becomes "+newHeight+
	        						"and boxWidth "+boxWidth+" becomes "+newWidth);
        }
        
        
        int xoff = (CHAR_WIDTH - newWidth) / 2;
        int yoff = (CHAR_HEIGHT - newHeight) / 2;
        
        System.out.println("newHeight=" + newHeight +
                " newWidth=" + newWidth +
                " x off=" + xoff + 
                " y off=" + yoff);
        //BufferedImage newCharImg = resizeCV(charImg,newWidth,newHeight);

        Graphics2D m = outputImage.createGraphics();
        m.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        m.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //m.drawImage(newCharImg, MARGIN-1+xoff, MARGIN-1+yoff, newCharImg.getWidth(), newCharImg.getHeight(), null);
        m.drawImage(charImg, MARGIN-1+xoff, MARGIN-1+yoff, newWidth, newHeight, null);
        m.dispose();
        
        return outputImage;
	}
	
	/**
	 * get the first 2 bytes of the color (which will stand for BLUE in RGB or grayscale in Grayscale (lel))
	 * @param input
	 * @return 0-255 color value
	 */
	private int getColorValue(int input) {
		return (input>>0)&0xFF;
	}
	
}
