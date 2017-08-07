

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import teradata.extractor.CharScanner;
import teradata.extractor.Characters;
import teradata.extractor.FieldFinder;
import teradata.imageToCsv.ImageConverter;
import teradata.converter.*;

public class mainClass {
	
	//private static final String[] fileName = {};
	//private static final String[] fileName = {"1501789856631","1501790031467","1501802611669","1501802628766","1501802643005","1501802655630","1501802673240","1501802685411","1501802713592","1501802727308"};
	//private static final String[] fileName = {"1501802628766","1501804560708","1501804575458","1501804586328","1501804599548","1501804634237","1501891488249","1501891678634","1501891815938","1501891850434","1501891994031","1501892517052","1501893761293","1501893783075","1501894605220","1501894644946","1502146929737","1502146950347","1502146965318","1502146979001","1502146993624","1502147012502","1502147025584","1502147051912","1502147082838","1502147094102","1502147109980","1502147121319","1502147147535","1502147162431","1502147174397","1502147214165","1502147226738","1502147239296","1502147253807","1502147310346","1502147324639","1502147336382","1502147348893","1502147373535","1502147387623","1502147401863","1502147414862","1502147426758","1502147438242","1502147484297","1502147500219","1502147514179","1502147526762","1502147538250","1502147552821","1502147564365","1502147578813","1502147590629","1502147609458","1502147622232","1502147633282","1502147687763"};
	private static final String[] fileName = {"1501802628766"};
	private static final String PATH = "/Users/mo255024/eclipse-workspace/CharacterExtraction/boxing/checks/newhw2/";
	//private static final String PATH = "/asterfs/enghome/kb186046/tmp";

	public static void main(String[] args) {
		BufferedImage image, subimage, newimage;
		
		long startTime = System.currentTimeMillis();
		
			try 
			{
				ImageConverter mImageConverter = new ImageConverter(PATH+"checks.csv");
				for(int f=0;f<fileName.length;f++)
					{
				    image = ImageIO.read(new File(PATH+fileName[f]+".png"));
				    System.out.println("Img "+fileName[f]+" dimensions are "+image.getWidth()+"x"+image.getHeight());
				    //subimage = image.getSubimage(232, 218, 1050, 95); //TODO dynamic field recognition?
				    subimage = new FieldFinder().trimField(image);
				    File outputfile0 = new File("/Users/mo255024/eclipse-workspace/CharacterExtraction/boxing/subimage.png");
					ImageIO.write(subimage, "png", outputfile0);
				    CharScanner mCharScanner = new CharScanner(30);
				    Characters characters = mCharScanner.extractCharacters(subimage,28,7);
				    for (int i=0; i<characters.firstName.length;i++) {
					    newimage = characters.firstName[i];
					    String name = ("00"+Integer.toString(i)).substring(Integer.toString(i).length());
					    File outputfile = new File(PATH+"extracted/"+fileName[f]+"_0"+"_"+name+".png");
					    ImageIO.write(newimage, "png", outputfile);
					    mImageConverter.imageToSpreadsheet(newimage,fileName[f]+"_0"+"_"+name);
				    }
				    for (int i=0; i<characters.lastName.length;i++) {
					    newimage = characters.lastName[i];
					    String name = ("00"+Integer.toString(i)).substring(Integer.toString(i).length());
					    File outputfile = new File(PATH+"extracted/"+fileName[f]+"_1"+"_"+name+".png");
					    ImageIO.write(newimage, "png", outputfile);
					    mImageConverter.imageToSpreadsheet(newimage,fileName[f]+"_1"+"_"+name);
					    System.out.println(new ImageToByte().convert(newimage));
				    }
				    
				}
			    mImageConverter.flush();
			} 
			catch (IOException e) 
			{
				System.out.println("Error reading file, closing program");
			    e.printStackTrace();
			    System.exit(1);
			}
			System.out.println("Execution took "+(System.currentTimeMillis()-startTime)+" ms to process "+fileName.length+" files");
		}

}
