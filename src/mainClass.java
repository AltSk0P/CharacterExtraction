

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import teradata.extractor.*;
import teradata.imageToCsv.ImageConverter;
import teradata.converter.*;

public class mainClass {
	
	//private static final String[] fileName = {};
	//private static final String[] fileName = {"1502920758836","1502920767519","1502920775013","1502920783773","1502920790689","1502920798526","1502920806216","1502920817875","1502920827466","1502920838838","1502920847018","1502920859868","1502920868282","1502920878313","1502920887028","1502920896620","1502920906888","1502920915785","1502920925002","1502920938584","1502920946648","1502920954997","1502920961030","1502920969812","1502920977625","1502920987590","1502920994492","1502921004769","1502921013526","1502921021908","1502921029395","1502921038963","1502921047984","1502921062763","1502921072768","1502921080097","1502921092691","1502921102631","1502921112457"};
	private static final String PATH = "/Users/mo255024/eclipse-workspace/CharacterExtraction/boxing/checks/maxhwang/";
	private static final String[] fileName = {"1503021499512"};
	//private static final String[] fileName = {"1501802628766","1501804560708","1501804575458","1501804586328","1501804599548","1501804634237","1501891488249","1501891678634","1501891815938","1501891850434","1501891994031","1501892517052","1501893761293","1501893783075","1501894605220","1501894644946","1502146929737","1502146950347","1502146965318","1502146979001","1502146993624","1502147012502","1502147025584","1502147051912","1502147082838","1502147094102","1502147109980","1502147121319","1502147147535","1502147162431","1502147174397","1502147214165","1502147226738","1502147239296","1502147253807","1502147310346","1502147324639","1502147336382","1502147348893","1502147373535","1502147387623","1502147401863","1502147414862","1502147426758","1502147438242","1502147484297","1502147500219","1502147514179","1502147526762","1502147538250","1502147552821","1502147564365","1502147578813","1502147590629","1502147609458","1502147622232","1502147633282","1502147687763"};
	
	public static void main(String[] args) {
		BufferedImage image, subimage, newimage;
		
		long startTime = System.currentTimeMillis();
		List<String> list = new ArrayList<>();
			try 
			{
			Comparer mComparer = new Comparer();
			for(int tries=0;tries<3;tries++) {
				ImageConverter mImageConverter = new ImageConverter(PATH+"checks.csv");
				for(int f=0;f<fileName.length;f++)
					{
					try {
					    image = ImageIO.read(new File(PATH+fileName[f]+".png"));
					    System.out.println("Img "+fileName[f]+" dimensions are "+image.getWidth()+"x"+image.getHeight());
					    subimage = new FieldFinder().trimField(image);
					    File outputfile0 = new File("/Users/mo255024/eclipse-workspace/CharacterExtraction/boxing/subimage.png");
						ImageIO.write(subimage, "png", outputfile0);
					    CharScanner mCharScanner = new CharScanner(30);
					    Characters characters = mCharScanner.extractCharacters(subimage,28,7);
					    for (int i=0; i<characters.firstName.length;i++) {
					    		
						    newimage = characters.firstName[i];
						    String name = ("00"+Integer.toString(i)).substring(Integer.toString(i).length());
						    File outputfile = new File(PATH+"extracted/"+fileName[f]+"_0_"+name+".png");
						    ImageIO.write(newimage, "png", outputfile);
						    mImageConverter.imageToSpreadsheet(newimage,fileName[f]+"_0_"+name);
					    }
					    for (int i=0; i<characters.lastName.length;i++) {
						    newimage = characters.lastName[i];
						    String name = ("00"+Integer.toString(i)).substring(Integer.toString(i).length());
						    File outputfile = new File(PATH+"extracted/"+fileName[f]+"_1_"+name+".png");
						    ImageIO.write(newimage, "png", outputfile);
						    mImageConverter.imageToSpreadsheet(newimage,fileName[f]+"_1_"+name);
						    System.out.println(new ImageToByte().convert(newimage));
						    if(i==2) {
				    			mComparer.setImg(tries,newimage);
				    			}
					    }
					}
					catch (ExtractionException e) {
						 e.printStackTrace();
						 list.add(fileName[f]);
					}
				}
			    mImageConverter.flush();
				}
			System.out.println(mComparer.compare());
			}
			catch (IOException e) 
			{
			    e.printStackTrace();
			}
			System.out.println("Execution took "+(System.currentTimeMillis()-startTime)+" ms to process "+fileName.length+" files");
			System.out.println("Failed "+list.size()+" files: "+Arrays.toString(list.toArray()));
			System.exit(0);
		}

}
