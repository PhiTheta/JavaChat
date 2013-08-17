import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class chat_client {
	/***************************************************************************
	 * Author: Harendra Singh
	 * Program Flow: 
	 * 		waitForServer ---> startThreads (incoming,outgoing)
	 * 			^							   ||   	||
	 * 			|							   ||   	||
	 * 			|					    check mesg   take input from console		
	 * 		when server reset			-display it  -send to server
	 *****************************************************************************/
	/* Java Chat v0.3
	 * Features Added
	 * 	>> allows user to type "ip address" and "port number" of the server
	 * BugFixed
	 * 	>> server reset does not crash the client
	 *  >> client disconnection does not throw exception in server
	 *  >> Running another instance of server does not throws exception
	 * BugsToRemove
	 * 	>> After server resets the client side input is not responding for first line
	 *  >> 
	 * FeaturesToAdd
	 *  >> check connection (remove user from list when disconnected)
	 *  >> whenever a user disconnects, notify others and server
	 *  >> when a user loggs in, show all the user online
	 *  >> Change total user online on server and user
	 */
	public static void main(String[] args)throws Exception {
		chat_client CLIENT = new chat_client();
		CLIENT.waitForServer(CLIENT);
	}
	public void waitForServer(chat_client CLIENT) throws Exception{
		String p1 = "(\\d{1,3}\\.){3}\\d{1,3}";
		String ip = null;
		String ans = null;
		int portNum = 444;
		Socket SOCK = null;
		
		Pattern check = Pattern.compile(p1);
		Matcher match;
		Scanner in = new Scanner(System.in);
		System.out.println("ip-address of server" +
		" \n Connect to 169.254.122.214 (y/n) :");
		ans=in.nextLine();
		if(ans.equalsIgnoreCase("y"))
			ip="169.254.122.214";
		else{
			while(true){
				System.out.println("Enter ip-address of server");
				ip=in.nextLine();
				match = check.matcher(ip);
				if(!match.find()){
					System.out.println("Invalid Format!");
				}
				else
					break;
			}
			ip = match.group().trim();
		}
		System.out.println("port number of server" +
		" \n Connect to 444 (y/n) :");
		ans=in.next();
		if(ans.equalsIgnoreCase("y"))
			portNum = 444;
		else{
			while(true){
				try{
					System.out.println("Enter the port numbner of server");
					portNum = in.nextInt();
					break;
				}
				catch(Exception e){
					System.out.println("Invalid Format!");
				}
			}
		}
		System.out.println("Waiting for server ("+ip+") at port "+portNum+" to start....");
		while(SOCK==null){
			try {
				SOCK = new Socket(ip,portNum);
			} catch (Exception e) {
			}
		}
		CLIENT.startThreads(SOCK);
	}
	public void startThreads(Socket SOCK)throws Exception {
		new client_background_incoming(SOCK,this).start();
		new client_background_outgoing(SOCK,this).start();
		//thread_incoming.start();
		//thread_outgoing.start();
	}
	@SuppressWarnings("deprecation")
	public void stopThreads() throws Exception{
		System.out.println("------------------------------");
		System.out.println("Trying to stop active threads");
		System.out.println("------------------------------");
		waitForServer(this);
	}
}
//-------------------------End Of Class-------------------------------


/////-----------------needs editing-----------------------------------
class client_background_incoming extends Thread {
	private Socket SOCK=null;
	private String MESG=null;
	BufferedReader BR = null;
	chat_client CLIENT = null;
	public Boolean stopFlag = false;
	public client_background_incoming(Socket X, chat_client C){
		this.SOCK = X;
		this.CLIENT = C;
	}
	public void run() {
		try {
			BR = new BufferedReader(new InputStreamReader(SOCK.getInputStream()));
			while(!stopFlag){
				MESG = BR.readLine();
				if(stopFlag) break;
				System.out.println(MESG);
			}
		} catch (IOException e) {
			String error = e.getMessage();
			if(error == "Connection reset"){
				System.out.println("-----------------------------");
				System.out.println("Opps! The Server reset itselt");
				System.out.println("-----------------------------");
				//------stopping the threads and start waiting for server-------------------
				try {
					CLIENT.stopThreads();
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else
				e.printStackTrace();
		}
	}
}

class client_background_outgoing extends Thread{
	private Socket SOCK=null;
	private String MESG=null;
	chat_client CLIENT = null;
	PrintStream PS = null;
	BufferedReader BR = null;
	public Boolean stopFlag = false;
	public client_background_outgoing(Socket X,chat_client C){
		this.SOCK = X;
		this.CLIENT = C;
	}
	public void run(){
		try {
			PS = new PrintStream(SOCK.getOutputStream());
			BR = new BufferedReader(new InputStreamReader(System.in));
			while(!stopFlag){
				MESG = BR.readLine(); // from console
				if(stopFlag) break;
				PS.println(MESG);
			}
		} catch (IOException e) {
			System.out.println("Exception Thrown");
			e.printStackTrace();
		}
	}
}