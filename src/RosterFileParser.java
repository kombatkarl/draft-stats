import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class RosterFileParser {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {

		if(args.length != 1) {
			System.out.println("Invalid # of arguments. Must supply filename ");
			System.exit(1);
		}
		BufferedReader bReader = null;
		BufferedWriter bWriter = null;
		try {
			String filename = args[0];
	
			bReader = new BufferedReader(new FileReader(new File(filename)));
			String line;
			
			System.out.println("Parsing Batters...");
			// Find batters
			while(true) {
				line = bReader.readLine();
				if(line.contains("CODES")) {
					break;
				}
			}
			// Get Batters
			bWriter = new BufferedWriter(new FileWriter(new File("DraftStats/batters.txt")));
			while(true) {
				line = bReader.readLine();
				if(line.length() > 0) {
					String[] parts = line.split(" ");
					String name = parts[0] + "," + parts[1];
					bWriter.write(name);
					bWriter.newLine();
				} else {
					break;
				}
			}
			bWriter.flush();
			
			System.out.println("Parsing Pitchers...");
			// Find pitchers
			while(true) {
				line = bReader.readLine();
				if(line.contains("CODES")) {
					break;
				}
			}
			// Get Pitchers
			bWriter = new BufferedWriter(new FileWriter(new File("DraftStats/pitchers.txt")));
			while(true) {
				line = bReader.readLine();
				if(line.length() > 0) {
					String[] parts = line.split(" ");
					String name = parts[0] + "," + parts[1];
					bWriter.write(name);
					bWriter.newLine();
				} else {
					break;
				}
			}
			bWriter.flush();
			System.out.println("Done.");
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				if(bReader != null) bReader.close();
				if(bWriter != null) bWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
