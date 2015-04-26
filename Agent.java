import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * ExecDemo shows how to execute an external program
 * read its output, and print its exit status.
 */
public class Agent {
	
	private static class StreamGobbler extends Thread {
		InputStream is;
		String type;
		boolean stopped = false;

		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null && !stopped)
					System.out.println(type + "> " + line);
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			System.out.println("****THRED STOPPED*****");
		}
		
		public void xStop() {
			stopped = true;
			interrupt();
			
		}
	}	

	public static void main(String argv[]) throws IOException {

		ProcessBuilder pb = new ProcessBuilder()
			.command("D:\\work\\vertx\\bin\\vertx.bat", "runmod", "xld~node~1.0");
			
			
		Process p = pb.start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
		outputGobbler.start();
		errorGobbler.start();
		
		String input = System.console().readLine();
		while (!"exit".equals(input)) {
			if ("r".equals(input)) {
				System.out.println("> Stopping server");
				sendExit();
				int ev = -1;
				try {
					ev = p.waitFor();
				} catch (InterruptedException ex) {
					System.err.println(ex.getMessage());
				}
				System.out.println("exit code : " + ev);
				
				System.out.println("> Restarting server");
				
				p = pb.start();
				errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
				outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
				outputGobbler.start();
				errorGobbler.start();
				
			}
			
			input = System.console().readLine();
		}
		
		System.out.println("> Stopping server");
		sendExit();
		int ev = -1;
		try {
			ev = p.waitFor();
		} catch (InterruptedException ex) {
			System.err.println(ex.getMessage());
		}
		System.out.println("exit code : " + ev);
		
		
	}
	
	public static void sendExit() {
		URL url;
 
		try {
			// get URL content
			url = new URL("http://localhost:8081/exit");
			URLConnection conn = url.openConnection();
 
			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));
 
			String inputLine;
 
			while ((inputLine = br.readLine()) != null) {
				System.out.println("EXIT> "+ inputLine);
			}
 
			br.close();
 
			System.out.println("Done");
 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 	}
}


