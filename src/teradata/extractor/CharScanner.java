package teradata.extractor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class CharScanner {
	
	private int width, height;
	private int maxCharAmount;
	private static final int CHAR_DISTANCE = 2;
	private int backgroundColor = 250;
	private static final int MINIMAL_PIXEL = 4;
	private static final int MIN_THRES_WORD = 1;
	private static final int MINIMAL_WORD_WIDTH = 2;
	
	public CharScanner() {
		maxCharAmount = 128;
	}
	
	public CharScanner(int maxAmount) {
		maxCharAmount = maxAmount;
	}
	
	/**
	 * Returns the biggest char from the image
	 * @param input int[] representing an array of colors in the image
	 * @param width width of the image
	 * @return int[] representing the biggest character
	 */
	public int[] returnSingleChar(int[] input,int width0) {
		width = width0;
		height = input.length / width;
		int[][] charCoords = new int[maxCharAmount][2]; // TODO Change to ArrayList
		int charNum;
		CharData mCharData = recognizeCharacters(input, width);
		charCoords = mCharData.charCoords;
		charNum = mCharData.charNum;
		
		if (charNum > 0) {
		
		System.out.println("CharacterNum is "+charNum);
		
		int biggestDistance = 0;
		int biggestChar = 0;
		
		for(int i=0;i<=charNum;i++) {
			int d = charCoords[i][1]-charCoords[i][0]+1;
			if(d>biggestDistance) {
				biggestDistance = d;
				biggestChar = i;
			}
		}
		System.out.println("The biggest char is "+biggestChar+" with distance "+biggestDistance);
		return getChar(input,charCoords[biggestChar][0],charCoords[biggestChar][1]);
		}
		else {System.out.println("Only one char recognized");return input;}
	}
	
	private CharData recognizeCharacters(int[] input,int width){
		int color, threshold=MINIMAL_PIXEL;
		int height = input.length / width;
		boolean charPresent=false;
		int blackPixels = 0;
		int[][] charCoords = new int[maxCharAmount][2]; // TODO Change to ArrayList
		int charNum = -1;
		int[] whitespace = new int[maxCharAmount];
		int space = 0, wordWidth = 0;
		int wordNum = 1; // TODO Review
		int[] wordLoc = new int[16];
		
		for(int i=0,row=0,col=0;col<width&charNum<maxCharAmount;i++) //TODO should I restrict it my maxCharAmount
        {
            color = getColorValue(input[col+row*width]);
            if (color<backgroundColor) {
            	blackPixels++;
            }
            if (blackPixels == threshold) {
            	// good, column has part of a char TODO maybe set up a threshold based on the amount of pixels there?
            		if (!charPresent & wordWidth >= MINIMAL_WORD_WIDTH) { // if we haven't found a char previously, start a char and designate its starting position
            			threshold = MIN_THRES_WORD;
            			charNum++;
            			charCoords[charNum][0] = col-1-MINIMAL_WORD_WIDTH;
            			charPresent = true;
            			System.out.println("Found a beginning of a char "+charNum+" at "+(col-1)+"x"+row+" color="+color);
            			/*if(whitespace>WHITE_SPACE & charNum>0) {
            				System.out.println("A new word is char num "+(charNum));
            				wordNum++;
            				wordLoc[wordNum]=charNum;
            			}*/
            			whitespace[charNum+1]=space;
            			space = 0;
            		}
            		wordWidth++;
            		blackPixels = 0;
            		col++;
            		row=0;
            }
            else {
	            row++;
	            if (row == height){
	            	if (charNum>=0) {
	            		space++;
	            		blackPixels = 0;
	            	}
	                // Whoops, column has no black pixels, this is bad isn't it
	            		if(charPresent && space >= CHAR_DISTANCE) { // if we had a char previously, designate an end position
	            		charCoords[charNum][1] = col-(CHAR_DISTANCE);
	            		charPresent = false;
	            		blackPixels = 0;
	            		System.out.println("Found an end of a char "+charNum+" at "+(col)+"x"+row);
	            		threshold = MINIMAL_PIXEL;
	            		wordWidth = 0;
	            		}
	            		row = 0;
	            		col++;
	            }
            }
        }
		// We're assuming we only have TWO words right now, first and last name
		wordLoc[wordNum]= longestWhitespaceIndex(whitespace)-1;
		CharData mCharData = new CharData(charCoords,charNum,wordNum,wordLoc);
		return mCharData;
	}
	
	private int longestWhitespaceIndex(int[] whitespace) {
		int index=0, longest=0;
		for (int i=0;i<whitespace.length;i++) {
			if (whitespace[i]>longest) {
				longest = whitespace[i];
				index = i;
			}
		}
		return index;
	}
	
	private int[] getChar(int[] input, int x1, int x2) {
		int[] output = new int[height*(x2-x1+1)];
		
		for(int i=0,col=x1,row=0;i<output.length;i++)
		{
			output[i] = input[col+row*width];
			//System.out.println("Getting pixel from column "+col+" and row "+row);
			col++;
			if(col==x2+1) {
				row++;
				col=x1;
			}
		}
		
		return output;
	}
	
	public Characters extractCharacters(BufferedImage input,int dimension, int margins) {
		int[][] charCoords = new int[maxCharAmount][2];
		int charNum;
		int width = input.getWidth();
		double scaleFactor=0;
		
		int[] arrayImage = imageToArray(input);
		CharData mCharData = recognizeCharacters(arrayImage, width);
		
		charCoords = mCharData.charCoords;
		charNum = mCharData.charNum;
		int[] wordStart = mCharData.wordStart;
		System.out.println("CharNum "+(charNum)+", where wordStart[0]="+wordStart[0]+" and wordStart[1]="+wordStart[1]);
		Characters output = new Characters(charNum,wordStart[1]);
		
		for (int i=0,m=0; i<charNum+1; i++) { //Cycle through all the characters recognized
			int d = charCoords[i][1]-charCoords[i][0]+1;
			if(d>1) {
				if(i<wordStart[1]) { // TODO change the cycle to not be restricted to two words only
					if(i==0) {
						BufferedImage tmp = new BufferedImage(d,height,BufferedImage.TYPE_BYTE_GRAY);
						tmp.getRaster().setPixels(0, 0, d, height, getChar(arrayImage,charCoords[i][0],charCoords[i][1]));
						Character charImg = new Character(tmp,dimension,margins);
						scaleFactor = charImg.scaleFactor;
						output.firstName[i] = charImg.image;
					}
					else {
					BufferedImage tmp = new BufferedImage(d,height,BufferedImage.TYPE_BYTE_GRAY);
					tmp.getRaster().setPixels(0, 0, d, height, getChar(arrayImage,charCoords[i][0],charCoords[i][1]));
					output.firstName[i] = new Character(tmp,dimension,margins,scaleFactor).image;
					}
				}
				else if(i==wordStart[1]) {
					BufferedImage tmp = new BufferedImage(d,height,BufferedImage.TYPE_BYTE_GRAY);
					tmp.getRaster().setPixels(0, 0, d, height, getChar(arrayImage,charCoords[i][0],charCoords[i][1]));
					Character charImg = new Character(tmp,dimension,margins);
					scaleFactor = charImg.scaleFactor;
					output.lastName[m] = charImg.image;
					m++;
				}
				else
				{
					BufferedImage tmp = new BufferedImage(d,height,BufferedImage.TYPE_BYTE_GRAY);
					tmp.getRaster().setPixels(0, 0, d, height, getChar(arrayImage,charCoords[i][0],charCoords[i][1]));
					output.lastName[m] = new Character(tmp,dimension,margins,scaleFactor).image;
					m++;
				}
			}
		}
		return output;
	}
	
	private int[] imageToArray(BufferedImage image) {

		
	      if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
	            System.out.println("Input is grayscale ");
	        }else {
	        		System.out.println("Input isn't grayscale, converting");
	            image = convertToGray(image);
	        }
	      
	      width = image.getWidth();
	      height = image.getHeight();
	      
	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();  

	      int[] result = new int[height*width];
	      System.out.println("Created an array of "+result.length+" and original was "+pixels.length);
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
	
}

class CharData{
	int[][] charCoords;
	int charNum, wordNum;
	int[] wordStart;
	
	CharData(int[][] array, int amount, int words, int[] wordLoc){
		charCoords = array;
		charNum = amount+1;
		wordNum = words;
		wordStart = wordLoc;
		System.out.println("wordLoc[0]="+wordLoc[0]+" wordLoc[1]"+wordLoc[1]);
		System.out.println("wordStart[0]="+wordStart[0]+" wordStart[1]"+wordStart[1]);
	}
}
