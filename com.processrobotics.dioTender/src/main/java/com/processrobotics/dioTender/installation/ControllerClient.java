package com.processrobotics.dioTender.installation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Blocking socket helper for the controller's Dashboard Server (29999) and Primary interface (30001).
 * Every call blocks: never call it on the EDT.
 */
final class ControllerClient {

	static final String HOST = "127.0.0.1";
	private static final int DASHBOARD_PORT = 29999;
	private static final int PRIMARY_PORT = 30001;
	private static final int TIMEOUT_MS = 1000;

	private ControllerClient() {
	}

	/**
	 * Asks the Dashboard Server whether a program is running. Fail safe: any unexpected response throws,
	 * and callers treat that as "running".
	 */
	static boolean isProgramRunning() throws IOException {
		Socket socket = open(DASHBOARD_PORT);
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "US-ASCII"));
			in.readLine(); // welcome line
			OutputStream out = socket.getOutputStream();
			out.write("running\n".getBytes("US-ASCII"));
			out.flush();
			String response = in.readLine();
			if (response == null) {
				throw new IOException("no response from Dashboard Server");
			}
			if (response.contains("true")) {
				return true;
			}
			if (response.contains("false")) {
				return false;
			}
			throw new IOException("unexpected Dashboard response: " + response);
		} finally {
			socket.close();
		}
	}

	/** Sends a script to the Primary interface. Nothing is read back: port 30001 streams robot state. */
	static void sendPrimary(String script) throws IOException {
		Socket socket = open(PRIMARY_PORT);
		try {
			OutputStream out = socket.getOutputStream();
			out.write(script.getBytes("US-ASCII"));
			out.flush();
			socket.shutdownOutput();
		} finally {
			socket.close();
		}
	}

	private static Socket open(int port) throws IOException {
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(HOST, port), TIMEOUT_MS);
			socket.setSoTimeout(TIMEOUT_MS);
		} catch (IOException e) {
			socket.close();
			throw e;
		}
		return socket;
	}
}
