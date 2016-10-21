package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cooksys.assessment.ClientList;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private Socket socket;
	private PrintWriter writerTemp = null;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				// Beginning of implementing the @username command. Using if
				// statement because I'm comparing characters, not strings
				if (message.getCommand().charAt(0) == ('@')) {
					System.out.println(message.getContents());
					if (!Server.users.getU().contains(message.getCommand().substring(1))) {
						message.setContents("that user is not online");
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
					}
					for (String x : Server.users.getU()) {
						if (message.getCommand().substring(1).equals(x)) {
							log.info("user <{}> direct messaged someone", message.getUsername());
							writerTemp = new PrintWriter(
									new OutputStreamWriter(Server.users.getDSocket(x).getOutputStream()));
							writerTemp.write(mapper.writeValueAsString(message));
							writerTemp.flush();
							writer.write(mapper.writeValueAsString(message));
							writer.flush();
						}
					}
				}

				switch (message.getCommand()) {
				case "connect":
					if (!Server.users.getU().contains(message.getUsername())) {
						log.info("user <{}> connected", message.getUsername());
						Server.users.add(message.getUsername(), socket);
						for (Socket x : Server.users.getS()) {
							writerTemp = new PrintWriter(new OutputStreamWriter(x.getOutputStream()));
							writerTemp.write(mapper.writeValueAsString(message));
							writerTemp.flush();
						}
						break;
					} else {
						log.info("existing username");
						message.setContents(
								"username: " + message.getUsername() + " already exists, choose a different username");
						String existUser = mapper.writeValueAsString(message);
						writer.write(existUser);
						writer.flush();
						this.socket.close();
						break;
					}

				case "disconnect":
					log.info("user <{}> disconnected", message.getUsername());
					for (Socket x : Server.users.getS()) {
						writerTemp = new PrintWriter(new OutputStreamWriter(x.getOutputStream()));
						writerTemp.write(mapper.writeValueAsString(message));
						writerTemp.flush();
					}
					Server.users.remove(message.getUsername());
					this.socket.close();
					break;

				case "echo":
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					writer.write(mapper.writeValueAsString(message));
					writer.flush();
					break;

				case "broadcast":
					log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
					if (Server.users.getS().size() > 1) {
						for (Socket x : Server.users.getS()) {
							if (!x.equals(this.socket)) {
								writerTemp = new PrintWriter(new OutputStreamWriter(x.getOutputStream()));
								writerTemp.write(mapper.writeValueAsString(message));
								writerTemp.flush();
							}
						}
					}
					writer.write(mapper.writeValueAsString(message));
					writer.flush();
					break;

				case "users":
					log.info("user <{}> requested user list", message.getUsername());
					String str = message.getContents();
					for (String x : Server.users.getU()) {
						str += "\n" + x;
					}
					message.setContents(str);
					writer.write(mapper.writeValueAsString(message));
					writer.flush();
					break;
				}
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
