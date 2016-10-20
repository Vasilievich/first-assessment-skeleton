package com.cooksys.assessment;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.cooksys.assessment.model.Message;

public class ClientList {
	private static List<String> lu;
	//private static List<Message> lm;
	private static List<Socket> ls;
	
	public ClientList() {
		lu = Collections.synchronizedList(new ArrayList<String>());
		//lm = Collections.synchronizedList(new ArrayList<Message>());  //list.remove couldn't differentiate object comparison unlike string
		ls = Collections.synchronizedList(new ArrayList<Socket>());		//thus string would just be more convenient
	}

	public static void add(String m, Socket s) {
		lu.add(m);
		ls.add(s);
	}

	public static boolean remove(String m, Socket s) {
		return lu.remove(m) || ls.remove(s);
	}
	public static void remove(String m) {
		lu.remove(m);
		ls.remove(gett(m));
	}

	public static Socket getDSocket(String m) {
		return ls.get(gett(m));
	}
	public static int gett(String m) {
		int counter = 0;
		for(String x : lu) {
			if(m.equals(x))
				return counter;
			counter++;
		}
		return counter;
	}
	public static List<String> getM() {
		return lu;
	}
	public static List<Socket> getS() {
		return ls;
	}
}
