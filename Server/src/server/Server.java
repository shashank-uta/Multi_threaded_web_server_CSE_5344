/**
 Name - Shashank Shekhar
 UTA ID - 1001767592
 CSE-5344-001-COMPUTER NETWORKS
 **/
package server;

import java.io.*;
import java.net.*;

public class Server implements Runnable 
{
    private ServerSocket serverSocket;
    private String serverHost;
    private int serverPort;
    private final String DefHost = "localhost";					
    private final int DefPort = 8080;
	
    //Constructor - no port passed
    public Server ()													
    {
        //will take the default host 
        this.serverHost = DefHost;
        //will take the default port
        this.serverPort = DefPort;
    }

    //Constructor - both host and port passed
    public Server (String sHost, int port)							
    {
        this.serverHost = sHost;
        this.serverPort = port;
    }

    //Constructor - only port passed
    public Server (int port)											
    {
        // will take the default host
        this.serverHost = DefHost;
        this.serverPort = port;
    }

    @Override
    public void run() 
    {
        try 
        {
            //the inet address of the host
            InetAddress serverInet = InetAddress.getByName(serverHost);
            serverSocket = new ServerSocket(serverPort, 0, serverInet);
            System.out.println("Server started at hostname: " + serverSocket.getInetAddress() + " port number: " + serverSocket.getLocalPort() + "\n");
            int clientId=0;
            //here client gets connected and multithreaded concept starts.
            while(true)
            {
                //Accepts the client socket here
                Socket clientSocket = serverSocket.accept();
                System.out.println("C:"+clientId+"Connection established with the client at hostname : " + clientSocket.getInetAddress() + "and port number:" + clientSocket.getPort());
                HttpRequest request = new HttpRequest(clientSocket, clientId);
                //new thread created for the client and it gets transfered to HttpRequest.
                new Thread((Runnable) request).start();
                //next client gets in.
                clientId++;
            }

        }
        catch (UnknownHostException ex)
        {
            System.err.println("Unknown Host: " + serverHost + ex);
        }
        catch (IllegalArgumentException | IOException ex1) 
        {
            System.err.println("Exception in starting the SERVER: " + ex1.getMessage());
        }
        finally 
        {
            try 
            {
                if(serverSocket != null)
                {
                    serverSocket.close();
                }
            }
            catch (IOException ex2)
            {
                System.err.println("Exception in closing the server socket." + ex2);
            }
        }
    }
    public static void main(String[] args) 
    {
        int port = 8080;											//initialize port to default 8080
        if(args.length == 1)										
        {	
            try 
            {													
                port = Integer.parseInt(args[0]); 					
            }
            catch (NumberFormatException ex)
            {
                System.err.println("No Port number provided. Server will start at default port." + ex);
            }
        }

        System.out.println("Using Server Port : " + port);
        //Server object
        Server s = new Server(port); 					
        //starting Server in a new thread
        new Thread(s).start();										
    }
}


final class HttpRequest implements Runnable
{
    private Socket clientSocket;
    private int cId; 									
    //carriage return line feed
    private final String CRLF = "\r\n"; 							
    //status line parts separator
    private final String SP = " "; 									
	
        
        //Constructor sets client id and socket
    public HttpRequest(Socket cliSoc, int CID) 
    {						
        this.clientSocket = cliSoc;
        this.cId = CID;
    }
    public void run() 
    {
        //defining IOstreams
        BufferedReader br = null; 						
        DataOutputStream os = null; 					
        FileInputStream fis = null;

        try 
        {
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            os = new DataOutputStream(clientSocket.getOutputStream());
            String packet = br.readLine();							
            if(packet != null)													
            {
                System.out.println("C:"+cId+" Received a request: " + packet);
                //split request line based on whitespace in 3 parts
                String[] messageParts = packet.split(SP);
                //checks the request type and length
                if (messageParts[0].equals("GET") && messageParts.length == 3)
                {
                    String path = messageParts[1];
                    
                    //adding a forward slash to make it relative to the current file path
                    if(path.indexOf("/") != 0)
                    {	
                        path = "/" + path;
                    }
                    System.out.println("C:"+cId+" Requested filePath: " + path);

                    if(path.equals("/"))									
                    {
                        System.out.println("C:"+cId+" Respond with default /index.html file");
                            
                        //setting filePath to the default file
                        path = path + "index.html";						
                    }
                    
                    //making the file path relative
                    path = "." + path;
                    File file = new File(path);								
                    try 
                    {
                        if (file.isFile() && file.exists()) 
                        {
                            String responseLine = "HTTP/1.0" + SP + "200" + SP + "OK" + CRLF;
                            os.writeBytes(responseLine);
                            os.writeBytes("Content-type: " + getContentType(path) + CRLF);
                            os.writeBytes(CRLF);
                            fis = new FileInputStream(file);
                            byte[] buffer = new byte[1024];						
                            int bytes = 0;
                            while((bytes = fis.read(buffer)) != -1 )
                            {
                                os.write(buffer, 0, bytes);
                            }
                            System.out.println("C:"+cId+" Sending Response with status line: " + responseLine);
                            os.flush();							
                            System.out.println("C:"+cId+" HTTP Response sent");
                        } 
                        else 
                        {
                            System.out.println("C:"+cId+" Error: Requested filePath " + path + " does not exist"); 		
                            String responseLine = "HTTP/1.0" + SP + "404" + SP + "Not Found" + CRLF;				
                            os.writeBytes(responseLine);
                            os.writeBytes("Content-type: text/html" + CRLF);							
                            os.writeBytes(CRLF);					
                            os.writeBytes(getErrorFile());	
                            System.out.println("C:"+cId+" Sending Response with status line: " + responseLine);
                            os.flush();
                            System.out.println("C:"+cId+" HTTP Response sent");
                        }
                    }
                    catch (FileNotFoundException ex) 
                    {
                        System.err.println("C:"+cId+" Exception: Requested filePath " + path + " does not exist");
                    }
                    catch (IOException ex1)
                    {
                        System.err.println("C:"+cId+" Exception in processing request." + ex1.getMessage());
                    }
                }
                else
                {
                    System.err.println("C:"+cId+" Invalid HTTP GET Request. " + messageParts[0]);
                }
            }
            else
            {	
                System.err.println("C:"+cId+" Discarding a NULL/unknown HTTP request."); 
            }
        } 
        catch (IOException ex2) 
        {
            System.err.println("C:"+cId+" Exception in processing request." + ex2.getMessage());
        }
        finally 
        {
            try 
            {															//close the resources
                if (fis != null)
                {
                    fis.close();
                }
                if (br != null) 
                {
                    br.close();
                }
                if (os != null) 
                {
                    os.close();
                }
                if (clientSocket != null)
                {
                    clientSocket.close();
                    System.out.println("C:"+cId+" Closing the connection.\n");
                }
            } 
            catch (IOException e) 
            {
                System.err.println("C:"+cId+" EXCEPTION in closing resource." + e);
            }
        }
    }
	//method helps get uss the contect type of the file
    private String getContentType(String path)								
    {
        
        if(path.endsWith(".html") || path.endsWith(".html"))			
        {
            return "text/html";
        }
        return "application/octet-stream";										//otherwise, a binary file
    }
    private String getErrorFile ()
    {
        String errorContent = "<!doctype html>" + "\n" + "<html lang=\"en\">" + "\n" + "<head>" + "\n" + "    <meta charset=\"UTF-8\">" + "\n" +
                                "    <title>Error 404</title>" + "\n" + "</head>" + "\n" + "<body>" + "\n" + "    <b>ErrorCode:</b> 404" + "\n" +
                                "    <br>" + "\n" + "    <b>Error Message:</b> The requested file does not exist." + "\n" + "</body>" + "\n" +"</html>";
        return errorContent;
    }
    
}
