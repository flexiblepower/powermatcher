package net.powermatcher.simulation.buildutil;

import java.io.File;

public class GenerateTargetPlatformXml {

	public static void main(String[] args) {
		File workspace = new File("").getAbsoluteFile().getParentFile();

		System.out.println("Listing files in " + workspace);
		for (File dir : workspace.listFiles()) {
			File generated = new File(dir.getAbsolutePath() + File.separator + "generated");
			if (dir.isDirectory() && generated.exists()) {
				System.out.println(generated);
			}
		}
	}
}
