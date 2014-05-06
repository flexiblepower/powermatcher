package net.powermatcher.simulation.buildutil;

import java.io.File;


public class GenerateTargetPlatformXml {

	public static void main(String[] args) {
		
		File workspace = new File(System.getProperty("user.dir")).getParentFile();
		
		for(File dir : workspace.listFiles()) {
			File generated = new File(dir.getAbsolutePath() + File.separator + "generated");
			if(dir.isDirectory() && generated.exists()) {
				System.out.println(generated);
			}
		}
		
	}

}
