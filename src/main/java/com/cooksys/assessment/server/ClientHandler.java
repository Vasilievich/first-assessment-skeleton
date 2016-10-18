package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.ClientList;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	//private Map<Message, Socket> users = Collections.synchronizedMap(new HashMap<Message, Socket>());
	//private ConcurrentLinkedQueue users;

	private int counter = 0;
	private Socket socket;

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
				
				for(String x : Server.users.getM()) {
					if(message.equals(x)) {
						log.info("user <{}> direct messaged someone");
						String temp = message.getUsername() + "(direct): ";
						message.setContents(temp + message.getContents());
						String dmr = mapper.writeValueAsString(message);
						PrintWriter writerTemp = new PrintWriter(new OutputStreamWriter(Server.users.getDSocket(x).getOutputStream()));	
						writerTemp.write(dmr);
						writerTemp.flush();
					}
				}
				
				switch (message.getCommand()) {
					case "connect":
						Server.users.add(message.getUsername(), socket);
						log.info("user <{}> connected", message.getUsername());
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						Server.users.remove(message.getUsername(), socket);
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);   //returns a string version of a Message format "user:___ , command: ___ etc"
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcast message <{}>", message.getUsername(), message.getContents());
						String temp = message.getUsername() + "(all):";
						message.setContents(temp + message.getContents());
						String br = mapper.writeValueAsString(message);

						for(Socket x : Server.users.getS()) {
							if(!x.equals(this.socket)) {
								PrintWriter writerTemp = new PrintWriter(new OutputStreamWriter(x.getOutputStream()));	
								writerTemp.write(br);
								writerTemp.flush();
							}
						}
						break;
					case "users":
						log.info("user <{}> requested user list", message.getUsername());
						String str = "";
						for(String x : Server.users.getM()) {
							str += x + '\n';
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
