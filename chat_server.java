import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
//------check connecction is still to be implemented
public class chat_server {
	ArrayList<Socket> SOCKET_LIST = new ArrayList<Socket>();
	ArrayList<String> USER_CONNECTED = new ArrayList<String>();
	
	//------------------------- Main ---------------------------------
	
	public static void main(String[] args)throws Exception {
		chat_server SERVER = new chat_server();
		try{
		SERVER.startThreads(SERVER);
		}
		catch (Exception e) {
			if(e.getMessage().equals("Address already in use: JVM_Bind")){
				System.out.println("Another instance of server is runnning" +
						"\nPlease shut it down first.");
			}
		}
	}
	
	//----------------------Start Threads------------------------------
	
	public void startThreads(chat_server SERVER)throws Exception {
		Socket SOCK = null;
		ServerSocket SERVER_SOCK = new ServerSocket(444);
		System.out.println("Waiting for clients to connect....");
		while(true){
			SOCK = SERVER_SOCK.accept();
			System.out.println("connection accepted "+SOCK.getLocalAddress());
			new chat_server_background(SERVER,SOCK).start();
		}	
	}
	
}
//------------------- END OF CLASS -------------------------------------------

class chat_server_background extends Thread{
	private Socket SOCK=null; 
	private chat_server SERVER = null;
	// incoming SOCK is fixed now for each instance of this thread
	private String MESG=null;
	private String NAME=null;
	BufferedReader BR = null;
	PrintStream PS = null;
	public chat_server_background(chat_server server, Socket X){
		this.SOCK = X;
		this.SERVER=server;
	}
	public void run(){
		try {
			BR = new BufferedReader(new InputStreamReader(SOCK.getInputStream()));
			PS = new PrintStream(SOCK.getOutputStream());
			NAME = tellMeYourName(SOCK);
			System.out.println("name given"+NAME);
			notifyServer(NAME,SOCK);
			notifyUsers(NAME,true);
			addNameToList(NAME,SOCK);
			System.out.println("Now total users: "+SERVER.USER_CONNECTED.size());
			while(true){
				MESG = BR.readLine();
				if(MESG!=null){
					// show mesg on server
					System.out.println(NAME+" >> "+MESG);
					sendMesgToAllUser(NAME,MESG);
				}
			}
		} catch (IOException e) {
			if(e.getMessage().equals("Connection reset")){
				try {
					notifyUsers(NAME,false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else{
				System.out.println("Exception Thrown");
				e.printStackTrace();
			}
		}
	}
	public String tellMeYourName(Socket X) throws IOException{
		while(true){
			PS.println("SERVER: Tell me your username");
			NAME = BR.readLine();
			if(NAME!=""){
				if(SERVER.USER_CONNECTED.contains(NAME)){
					PS.println("Sorry that username is already taken :(");
				}
				else{
					break;
				}
			}
			else{
				System.out.println("Nothing returned from client");
			}
		}
		return NAME;
	}
	public void addNameToList(String name, Socket X){
		SERVER.USER_CONNECTED.add(NAME);
		SERVER.SOCKET_LIST.add(X);
	}
	public void notifyUsers(String name,Boolean connectFlag) throws IOException{
		String mesg = null;
		if(connectFlag)
			mesg=" just connected to chat :)";
		else
			mesg=" disconnected :(";
		for (Socket X : SERVER.SOCKET_LIST) {
			Socket TEMP_SOCK = X;
			String host = X.getLocalAddress().getHostName();
			PrintStream PS = new PrintStream(TEMP_SOCK.getOutputStream());
			PS.println("SERVER:"+name+mesg);
		}
	}
	public void notifyServer(String name, Socket X){
		String host = X.getLocalAddress().getHostName();
		System.out.println("#"+name+" Connected  |  host: "+host);
	}
	public void sendMesgToAllUser(String name, String mesg) throws IOException {
		for (Socket X : SERVER.SOCKET_LIST) {
			Socket TEMP_SOCK = X;
			PrintStream PS = new PrintStream(TEMP_SOCK.getOutputStream());
			PS.println(">>"+name+": "+mesg);
		}
		
	}
}