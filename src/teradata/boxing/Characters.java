package teradata.boxing;

import java.awt.image.BufferedImage;

public class Characters{
	public BufferedImage[] firstName; // TODO change to List of words instead of two hardcoded arrays
	public BufferedImage[] lastName;
	
	public Characters(int charAmount, int firstNameChars) {
		firstName = new BufferedImage[firstNameChars];
		lastName = new BufferedImage[charAmount-firstNameChars];
		System.out.println("Allocated "+(firstName.length)+" for firstName and "+lastName.length+" for lastName");
	}
}