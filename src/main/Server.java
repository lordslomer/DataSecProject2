package main;
import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;

import accounts.Person;
import util.InputHandler;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class Server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;

	public Server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			System.out.println("\nsocket before handshake:\n" + socket);
			newListener();
			SSLSession session = socket.getSession();
			Certificate[] cert = session.getPeerCertificates();
			System.out.println("socket after handshake:\n" + socket + "\n");
			String subject = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
			numConnectedClients++;
			System.out.println("client connected");
			System.out.println("client name (cert subject DN field): " + subject);
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
			
			PrintWriter out = null;
			BufferedReader in = null;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
			
			InputHandler inputHandler = new InputHandler();
			Person user = inputHandler.getPerson(cert[0]);
			
			String clientMsg;
			while ((clientMsg = in.readLine()) != null && !clientMsg.isEmpty() && !clientMsg.equals("quit") && !clientMsg.equals("exit")) {
				String response = inputHandler.hanldeInput(clientMsg,user);
				out.println(response);
				out.println("SERVERDONE".toCharArray());
				
				if (response.equalsIgnoreCase("Write in the record information:")) {
					 String entryInformation;
					 while((entryInformation = in.readLine()) == null || entryInformation.isEmpty()) {
						 out.println("Please fill in the record information:");
						 out.println("SERVERDONE".toCharArray());
					 }
					response = inputHandler.writeToRecord(clientMsg.split(" ")[1], entryInformation, user);
					out.println(response);
					out.println("SERVERDONE".toCharArray());
				}
			}
			inputHandler.save();
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			System.out.println("client disconnected");
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {
		System.out.println("\nServer Started\n");
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		String type = "TLSv1.2";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client authentication
			new Server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLSv1.2")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLSv1.2");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();
				// keystore password (storepass)
				ks.load(new FileInputStream(getResourceAsFile("Stores/serverkeystore")), password);
				// truststore password (storepass)
				ts.load(new FileInputStream(getResourceAsFile("Stores/servertruststore")), password);
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}
	public static File getResourceAsFile(String resourcePath) {
	    try {
	        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
	        if (in == null) {
	            return null;
	        }

	        File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
	        tempFile.deleteOnExit();

	        try (FileOutputStream out = new FileOutputStream(tempFile)) {
	            //copy stream
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = in.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	        }
	        return tempFile;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}
