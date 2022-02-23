package util;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Logger {
	private static PrintStream output;
	public Logger() {
		try {
			FileOutputStream fileOutput = new FileOutputStream("Resources/Db/logs.txt",true);
			output = new PrintStream(fileOutput);
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static String getTimeDate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
	public void log(String user, String patient, String action) {
		output.println(getTimeDate() + ": " +user + " " + action + " " + patient);
		output.flush();
	}
	

}
