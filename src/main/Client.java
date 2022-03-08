package main;
import java.io.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.cert.*;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */

public class Client {
	public static void main(String[] args) throws Exception {
		String host = null;
		int port = -1;
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "] = " + args[i]);
		}
		if (args.length < 2) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}
		try { /* get input parameters */
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}

		String username = new String();
		String password = new String();
		BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

		try {
			SSLSocketFactory factory = null;
			try {
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLSv1.2");
				while (true) {
					try {
						System.out.print("Enter Username:");
						username = read.readLine();
						Console console = System.console();
						if (console != null) {
							StringBuilder pass = new StringBuilder();
							for (char c : console.readPassword("Enter password:")) {
								pass.append(c);
							}
							password = pass.toString();
						} else {
							System.out.print("Enter password:");
							password = read.readLine();
						}
						// keystore password (storepass)
						ks.load(new FileInputStream(getResourceAsFile("Stores/"+username+"KeyStore")), password.toCharArray());
						// truststore password (storepass);
						ts.load(new FileInputStream(getResourceAsFile("Stores/clienttruststore")), password.toCharArray());
						kmf.init(ks, password.toCharArray()); // user password (keypass)
						tmf.init(ts); // keystore can be used as truststore here
						ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
						factory = ctx.getSocketFactory();
						break;
					} catch (Exception e) {
						System.out.println("Invalid username or wrong password");
						continue;
					}
				}
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			System.out.println("");
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			/*
			 * send http request
			 *
			 * See SSLSocketClient.java for more information about why there is a forced
			 * handshake here when using PrintWriters.
			 */

			socket.startHandshake();
			SSLSession session = socket.getSession();
			Certificate[] cert = session.getPeerCertificates();
			String subject = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(username);
			while (socket.isConnected()) {
				String input;
				while(!(input = in.readLine()).equals("SERVERDONE")) {
					System.out.println(input);
				}
				System.out.print(">");
				String request = read.readLine();
				out.println(request);
				out.flush();
				if (request.equalsIgnoreCase("quit") || request.equalsIgnoreCase("exit")) {
					break;
				}
			}
			in.close();
			out.close();
			read.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
