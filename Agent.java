import java.io.*;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;

/**
 * ExecDemo shows how to execute an external program
 * read its output, and print its exit status.
 */
public class Agent {
	static volatile long lastFileModify = 0;
	
	/* ==================================== StreamGobbler ================================================ */
	private static class StreamGobbler extends Thread {
		InputStream is;
		String type;
		boolean stopped = false;
		String printStartsWith = null;

		private StreamGobbler(InputStream is, String type) {
			this(is, type, null);
		}

		private StreamGobbler(InputStream is, String type, String printStartsWith) {
			this.is = is;
			this.type = type;
			this.printStartsWith = printStartsWith;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null && !stopped) {
					if (line == null)	
						continue;
					if (printStartsWith == null || line.startsWith(printStartsWith))
						System.out.println(type + "> " + line);
					
				}
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			System.out.println("**** THRED STOPPED *****");
		}
		
		public void xStop() {
			stopped = true;
			interrupt();
			
		}
	}	
	/* ==================================== WatchDir ================================================ */
	private static class WatchDir extends Thread {

		private final WatchService watcher;
		private final Map<WatchKey,Path> keys;
		private final boolean recursive;
		private boolean trace = false;
		public volatile boolean stopped = false;

		@SuppressWarnings("unchecked")
		static <T> WatchEvent<T> cast(WatchEvent<?> event) {
			return (WatchEvent<T>)event;
		}

		/**
		 * Register the given directory with the WatchService
		 */
		private void register(Path dir) throws IOException {
			WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			if (trace) {
				Path prev = keys.get(key);
				if (prev == null) {
					System.out.format("register: %s\n", dir);
				} else {
					if (!dir.equals(prev)) {
						System.out.format("update: %s -> %s\n", prev, dir);
					}
				}
			}
			keys.put(key, dir);
		}

		/**
		 * Register the given directory, and all its sub-directories, with the
		 * WatchService.
		 */
		private void registerAll(final Path start) throws IOException {
			// register directory and sub-directories
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
				{
					if (dir.toString().indexOf("target") < 0) {
						register(dir);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		/**
		 * Creates a WatchService and registers the given directory
		 */
		WatchDir() throws IOException {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keys = new HashMap<WatchKey,Path>();
			this.recursive = true;

			/*if (recursive) {
				System.out.format("Scanning %s ...\n", dir);
				registerAll(dir);
				System.out.println("Done.");
			} else {
				register(dir);
			}*/

		}

		/**
		 * Process all events for keys queued to the watcher
		 */
		 @Override
		public void run() {
			// enable trace after initial registration
			this.trace = true;
			
			while (!stopped) {

				// wait for key to be signalled
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					return;
				}

				Path dir = keys.get(key);
				if (dir == null) {
					System.err.println("WatchKey not recognized!!");
					continue;
				}

				for (WatchEvent<?> event: key.pollEvents()) {
					WatchEvent.Kind kind = event.kind();

					// TBD - provide example of how OVERFLOW event is handled
					if (kind == OVERFLOW) {
						continue;
					}

					// Context for directory entry event is the file name of entry
					WatchEvent<Path> ev = cast(event);
					Path name = ev.context();
					Path child = dir.resolve(name);

					// print out event
					if (child.toString().indexOf("target") < 0) {
						System.out.format("%s: %s\n", event.kind().name(), child);
						lastFileModify = System.currentTimeMillis();
					}

					// if directory is created, and watching recursively, then
					// register it and its sub-directories
					if (recursive && (kind == ENTRY_CREATE)) {
						try {
							if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
								registerAll(child);
							}
						} catch (IOException x) {
							// ignore to keep sample readbale
						}
					}
				}

				// reset key and remove from set if directory no longer accessible
				boolean valid = key.reset();
				if (!valid) {
					keys.remove(key);

					// all directories are inaccessible
					if (keys.isEmpty()) {
						break;
					}
				}
			}
		}
	}
	

	/* ==================================== Read console input ================================================ */
	
	static class ReadRunnable implements Runnable {

		@Override
		public void run() {
			final Scanner in = new Scanner(System.in);
			while(in.hasNext()) {
				final String line = in.nextLine();
				lineRead(line);
				if ("exit".equalsIgnoreCase(line)) {
					break;
				}
			}
		}

	}	
	
	/* ==================================== MAIN ================================================ */
	public static WatchDir watchDir;
	public static String inputLine;
	public static Object inputLineSynch = new Object();
	public static ProcessBuilder pb;
	public static ProcessBuilder pbc;
	public static StreamGobbler errorGobbler;
	public static StreamGobbler outputGobbler;	
	public static Process p;
	
	public static void main(String argv[]) throws IOException {

	
		watchDir = new WatchDir();
		watchDir.registerAll(Paths.get("./site"));
		watchDir.registerAll(Paths.get("./node"));
		watchDir.registerAll(Paths.get("./api"));
		watchDir.registerAll(Paths.get("./api-actor"));
		watchDir.registerAll(Paths.get("./api-article"));
		watchDir.registerAll(Paths.get("./api-homebudget"));
		watchDir.registerAll(Paths.get("./api-order"));
		watchDir.registerAll(Paths.get("./api-partner"));
		
		watchDir.start();	
		
		pb = new ProcessBuilder()
			.command("D:\\work\\vertx\\bin\\vertx.bat", "runmod", "xld~node~1.0");
			
		pbc = new ProcessBuilder()
			.command("D:\\work\\apache-maven-3.2.3\\bin\\mvn.bat", "clean", "package");
	
		p = pb.start();
		errorGobbler = new StreamGobbler(p.getErrorStream(), "E");
		outputGobbler = new StreamGobbler(p.getInputStream(), "O");
		outputGobbler.start();
		errorGobbler.start();
	
		
		new Thread(new ReadRunnable()).start();
		long lfm = lastFileModify;
		
		for (;;) {
			synchronized(inputLineSynch) {
				if (inputLine != null) {
					System.out.println(Thread.currentThread().getName());
					System.out.println("LINE READ IN MAIN : " + inputLine);
				
				
					if ("exit".equals(inputLine)) {
						System.out.println("> Stopping everything...");
						System.out.println(Thread.currentThread().getName());
						watchDir.stopped = true;
						watchDir.interrupt();
						
						sendExit();
						int ev = -1;
						try {
							ev = p.waitFor();
						} catch (InterruptedException ex) {
							System.err.println(ex.getMessage());
						}
						System.out.println("exit code : " + ev);
						
						
						break;
					}
					
					inputLine = null;
				}
			}
			
			if (lastFileModify != lfm && System.currentTimeMillis() - lastFileModify > 2000) {
					lfm = lastFileModify;
					System.out.println("Should recompile");
					if (recompile()) {
						restart();
					}
			}
			
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
				break;
			}
		}
	
	
		
			
		/*	
		Process p = pb.start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "E");
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "O");
		outputGobbler.start();
		errorGobbler.start();
	
		String input = System.console().readLine();
		while (!"exit".equals(input)) {
			/*
			if ("rr".equals(input)) {
			}
			
			
			if ("r".equals(input)) {
				
				
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
		*/
		
	}
	
	public static void lineRead(String line) {
		synchronized (inputLineSynch) {
			inputLine = line;
		}
	}
	
	public static boolean recompile() throws IOException {
		System.out.println("Recompiling....");
		Process pc = pbc.start();
		StreamGobbler cErrorGobbler = new StreamGobbler(pc.getErrorStream(), "C.E");
		StreamGobbler cOutputGobbler = new StreamGobbler(pc.getInputStream(), "C.O", "[ERROR]");
		cOutputGobbler.start();
		cErrorGobbler.start();
		
		int evc = -1;
		try {
			evc = pc.waitFor();
		} catch (InterruptedException ex) {
			System.err.println(ex.getMessage());
		}
		System.out.println("exit code : " + evc);
		return evc == 0;
	}
	
	public static void restart() throws IOException {
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
		errorGobbler = new StreamGobbler(p.getErrorStream(), "E");
		outputGobbler = new StreamGobbler(p.getInputStream(), "O");
		outputGobbler.start();
		errorGobbler.start();	
	}
	
	
	/* ==================================== SendExit ================================================ */
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


