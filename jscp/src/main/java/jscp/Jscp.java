package jscp;

import java.io.InputStream;
import java.util.StringTokenizer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class Jscp {
    protected static JSch jsch = new JSch();
    protected static Session session; 
    protected static ChannelSftp sftpChannel;
	
    public static void init(String user,  String password, String host, int port) {
    	try
        {
	        session = jsch.getSession(user, host, port);
	        session.setPassword(password);
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.connect();
	        sftpChannel = (ChannelSftp) session.openChannel("sftp");
	        sftpChannel.connect();
	        sftpChannel.setPty(true);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
	
    public static  String executeCommand(String cmd) {
    	String return_val = "";
    	try {
	    	StringBuilder outputBuffer = new StringBuilder();
	        Channel channel = session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(cmd);
	        ((ChannelExec)channel).setPty(true);
	        
	        InputStream commandOutput = channel.getInputStream();
	        channel.connect();

	        int readByte = commandOutput.read();
	
	        while(readByte != 0xffffffff)
	        {
	           outputBuffer.append((char)readByte);
	           readByte = commandOutput.read();
	        }
	        return_val = outputBuffer.toString();
	        channel.disconnect();

	        
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        return return_val;
    }
    
    public static void fetch(String directory, String Destination) throws SftpException {
    	StringTokenizer st = new StringTokenizer(executeCommand("ls " + directory));
    	while(st.hasMoreTokens()) {
    		//System.out.println(st.nextToken());
    		String filename = st.nextToken();
    		String newFilename = "temp" + filename;
    		@SuppressWarnings("unused")
			SftpATTRS attrs=null;
    		try {
    		    attrs = sftpChannel.stat(directory + "/" + filename);
    		    sftpChannel.rename(directory + "/" + filename, directory + "/" + newFilename);
        		sftpChannel.get(directory + "/" + newFilename, Destination);
        		executeCommand("rm " + directory + "/"  + newFilename);
    		} catch (Exception e) {
    		}
    	}
    }
    
    public static void shutdown() {
        session.disconnect();
        sftpChannel.disconnect();
    }
	
	public static void main(String[] args) {
		if(args.length < 6) {
			System.out.println("Too few arguments.\n" +
								"Usage:\n"+
								"1. UserId\n" +
								"2. User Password\n" +
								"3. Hostname\n" +
								"4. Port\n" +
								"5. Folder Name\n" +
								"6. Destination Folder Name");
			return;
		} 
		try {
			init(args[0],args[1],args[2],Integer.parseInt(args[3]));
			fetch(args[4], args[5]);
			
			shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
