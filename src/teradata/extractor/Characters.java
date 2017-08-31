package teradata.extractor;

import java.awt.image.BufferedImage;

/**
 * This public class contains an array of buffered images of the output characters
 */
public class Characters{
	public BufferedImage[] firstName; // TODO change to List of words instead of two hardcoded arrays
	public BufferedImage[] lastName;
	
	public Characters(int charAmount, int firstNameChars) {
		firstName = new BufferedImage[firstNameChars];
		lastName = new BufferedImage[charAmount-firstNameChars];
		System.out.println("Allocated "+(firstName.length)+" for firstName and "+lastName.length+" for lastName");
	}
}