

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import teradata.boxing.CharScanner;
import teradata.boxing.Characters;
import teradata.imageToCsv.ImageConverter;

public class mainClass {
	
	private static final String fileName = "1501531753389";
	private static final String PATH = "/Users/mo255024/eclipse-workspace/CharacterExtraction/boxing/";

	public static void main(String[] args) {
		BufferedImage image, subimage, newimage;;
		
			try 
			{
				ImageConverter mImageConverter = new ImageConverter(PATH+"checks.csv");
			    image = ImageIO.read(new File(PATH+fileName+".png"));
			    subimage = image.getSubimage(114, 117, 516, 35); //TODO dynamic field recognition?
			    System.out.println("Img dimensions are "+image.getWidth()+"x"+image.getHeight());
			    CharScanner mCharScanner = new CharScanner(30,26);
			    Characters characters = mCharScanner.extractCharacters(subimage,28,7);
			    for (int i=0; i<characters.firstName.length;i++) {
				    newimage = characters.firstName[i];
				    File outputfile = new File("/Users/mo255024/eclipse-workspace/CharacterExtraction/src/boxing/Ch_first_output"+i+".png");
				    ImageIO.write(newimage, "png", outputfile);
				    mImageConverter.imageToSpreadsheet(newimage,fileName+"_0"+"_"+i);
			    }
			    for (int i=0; i<characters.lastName.length;i++) {
				    newimage = characters.lastName[i];
				    File outputfile = new File("/Users/mo255024/eclipse-workspace/CharacterExtraction/src/boxing/Ch_last_output"+i+".png");
				    ImageIO.write(newimage, "png", outputfile);
				    mImageConverter.imageToSpreadsheet(newimage,fileName+"_1"+"_"+i);
			    }
			    mImageConverter.flush();
			} 
			catch (IOException e) 
			{
				System.out.println("Error reading file, closing program");
			    e.printStackTrace();
			    System.exit(1);
			}
		}

}
