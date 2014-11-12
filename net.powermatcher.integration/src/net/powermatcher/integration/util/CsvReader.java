package net.powermatcher.integration.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CsvReader {
	
	private final static String DEFAULT_DELIMITER = ",";
	
	private File file;
	private String filename;
	
	private BufferedReader bufRdr;
	private int row = 0;
	private int col = 0;
	
	private String delimiters = DEFAULT_DELIMITER;
	
	

	public CsvReader(String filename) throws FileNotFoundException {
		super();
		this.filename = filename;
		init();
	}
	
	private void init() throws FileNotFoundException {
		file = new File(filename);
		bufRdr  = new BufferedReader(new FileReader(file));
	}

	
	public List<String >nextLine() throws IOException {
		String line = null;
		List<String> items = null;
		
		if ((line = bufRdr.readLine()) != null)
		{	
			items = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(line, this.delimiters);
			String token = null;
			while (st.hasMoreTokens())
			{
				token = st.nextToken();
				items.add(token);
				col++;
			}
			col = 0;
			row++;
			
			// Print the array
			printList(items);
		}
		return items;
	}


	private void printList(List<String> list) {
		String line = null;
		for (String item : list) {
			if (line == null) {
				line = item;
			}
			else {
				line =+ ',' + item;
			}
		}
		//System.out.println(line);
	}
	
	public void closeReader() throws IOException {
		if (bufRdr != null) {
			bufRdr.close();
		}
		
	}
	
	
	public void setDelimiters(String delimiters) {
		this.delimiters = delimiters;
	}

	public static void main(String[] args) {
		
		//String [][] numbers = new String [24][24];
		 
		File file = new File("resources/Bids.csv");		
	 
		//read each line of text file
		try {			
			BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
			String line = null;
			int row = 0;
			int col = 0;
					
			while((line = bufRdr.readLine()) != null && row < 24)
			{	
				List<String> demandArray = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(line,",");
				while (st.hasMoreTokens())
				{
					demandArray.add(st.nextToken() + ',');
					//get next token and store it in the array
					//System.out.println("token=" + st.nextToken());
					//numbers[row][col] = st.nextToken();
					col++;
				}
				col = 0;
				row++;
				
				// Print the array
				for (String string : demandArray) {
					System.out.print(string);
				}
				System.out.println();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
