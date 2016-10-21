package com.cooksys.assessment;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cooksys.assessment.model.Message;

public class ClientList {
	private static List<String> lu; //list user
	private static List<Socket> ls; //list socket
	
	public ClientList() {
		lu = Collections.synchronizedList(new ArrayList<String>());
		ls = Collections.synchronizedList(new ArrayList<Socket>());
	}

	public synchronized static void add(String m, Socket s) {
		lu.add(m);
		ls.add(s);
	}
	
	public synchronized static void remove(String m) {
		ls.remove(lu.indexOf(m));
		lu.remove(m);
	}	
	
	//Used for direct messaging
	public synchronized static Socket getDSocket(String m) {
		return ls.get(lu.indexOf(m));
	}
	
	public synchronized static List<String> getU() {
		return lu;
	}
	
	public synchronized static List<Socket> getS() {
		return ls;
	}
}
