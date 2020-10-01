/**
 Name - Shashank Shekhar
 UTA ID - 1001767592
 CSE-5344-001-COMPUTER NETWORKS
 **/
package Client;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.time.ZonedDateTime;

public class Client 
{
    public static void main(String[] args) 
    {
        //carriage return line feed
        final String CRLF = "\r\n";
        //status line parts separator        
        final String SP = " "; 												
        String serHost = null;
        int serPort = 8080;												
        String path = "/";												
            if(args.length == 1)										
            {
                serHost = args[0];											
            }
            else if (args.length == 2)
            {
                serHost = args[0];											
                try {															
                        serPort = Integer.parseInt(args[1]); 					
                }
                catch (NumberFormatException ex)
                {
                    System.out.println(ex);
                    path = args[1];											
                }
            }
            else if (args.length == 3)
            {
                serHost = args[0];											
                try {															
                    serPort = Integer.parseInt(args[1]); 					
                }
                catch (NumberFormatException ex)
                {
                    System.err.println("Client Integer Port is not provided. Default Server port will be used.");
                }

                path = args[2];												
            }
            else
            {
                System.err.println("Client Not enough parameters provided. At least Server Host is required.");
                System.exit(-1);
            }
            System.out.println("Client Using Server Port: " + serPort);
            System.out.println("Client Using FilePath: " + path);
            
            //define input and output streams
            //socket is initialized
            Socket socket = null;												
            //reads data received over the socket's inputStream                                                                                                                                        
            BufferedReader br = null; 
            //writes data over the socket's outputStream
            DataOutputStream DataOS = null; 							

            //writes content of the responded file in a file
            FileOutputStream FileOS = null; 										

            try 
            {
                InetAddress serverInet = InetAddress.getByName(serHost);
                socket = new Socket(serverInet, serPort);
                    System.out.println("Client Connected to the server at " + serHost + ":" + serPort);
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    DataOS = new DataOutputStream(socket.getOutputStream());

                                                                                                                                                    
                    String requestLine = "GET" + SP + path + SP +"HTTP/1.0" + CRLF;
                    System.out.println("Client Sending HTTP GET request: " + requestLine);
                    
                    //TIMER******************************
                    Calendar calendar = Calendar.getInstance();
                    long start = calendar.getTimeInMillis();

                                                                                                                                                    
                    DataOS.writeBytes(requestLine);

                                                                                                                                                    
                    DataOS.writeBytes(CRLF);

                                                                                                                                                    
                    DataOS.flush();

                    System.out.println("[CLIENT]> Waiting for a response from the server");
                                                                                                                                                    
                    String responseLine = br.readLine();
                    System.out.println("[CLIENT]> Received HTTP Response with status line: " + responseLine);

                                                                                                                                                    
                    String contentType = br.readLine();
                    System.out.println("[CLIENT]> Received " + contentType);

                                                                                                                                                    
                    br.readLine();

                    System.out.println("[CLIENT]> Received Response Body:");
                                                                                                                                                    
                    StringBuilder content = new StringBuilder();
                    String res;
                    while((res = br.readLine()) != null)
                    {
                                                                                                                                                    
                            content.append(res + "\n");

                                                                                                                                                    
                            System.out.println(res);
                    }

                    String fileName = getFileName(content.toString());				
                    long finish = calendar.getTimeInMillis();
                    System.out.println("RTT: " + (finish-start+" ms"));
                                                                                                                                                    
                                                                                                                                                    
                    FileOS = new FileOutputStream(fileName);
                    FileOS.write(content.toString().getBytes());
                    FileOS.flush();
                    System.out.println("[CLIENT]> HTTP Response received. File Created: " + fileName);
            } catch (IllegalArgumentException iae) {
                    System.err.println("[CLIENT]> EXCEPTION in connecting to the SERVER: " + iae.getMessage());
            } catch (IOException e) {
                    System.err.println("[CLIENT]> ERROR " + e);
            }
            finally {
                    try {
                                                                                                                                                    
                            if (br != null) {
                                    br.close();
                            }
                            if (DataOS != null) {
                                    DataOS.close();
                            }
                            if (FileOS != null) {
                                    FileOS.close();
                            }
                            if (socket != null) {
                                    socket.close();
                                    System.out.println("[CLIENT]> Closing the Connection.");
                            }
                    } catch (IOException e) {
                            System.err.println("[CLIENT]> EXCEPTION in closing resource." + e);
                    }
            }
    }
    //Method return index and name of the file with the title tag
    private static String getFileName(String con)						
    {
        String filename = "";												
        filename = con.substring(con.indexOf("<title>")+("<title>").length(), con.indexOf("</title>"));
        if(filename.equals(""))
        {
                filename = "index";
        }	
        filename = filename+".html";
        return filename;
    }
}
    
