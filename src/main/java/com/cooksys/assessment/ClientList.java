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
//	public void add(Message m) {
//		lm.add(m);
//	}
//	public void add(Socket s) {
//		sm.add(s);
//	}
	public static void add(String m, Socket s) {
		lu.add(m);
		ls.add(s);
	}
//	public boolean remove(Message m) {
//		if(!lm.contains(m))
//			return false;
//		lm.remove(m);
//		return true;
//	}
//	public boolean remove(Socket s) {
//		if(!sm.contains(s))
//			return false;
//		sm.remove(s);
//		return true;
//	}
	public static boolean remove(String m, Socket s) {
		return lu.remove(m) || ls.remove(s);
	}
	public static List<String> getM() {
		return lu;
	}
	public static List<Socket> getS() {
		return ls;
	}
	public static Socket getDSocket(String m) {
		int counter = 0;
		for(String x : lu) {
			if(m.equals(x))
				return ls.get(counter);
			counter++;
		}
		return null;
	}
}
