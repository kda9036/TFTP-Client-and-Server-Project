import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * TFTPServer
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-04-02
 */

public class TFTPServer extends Application implements EventHandler<ActionEvent>, TFTPConstants {
   // Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root = new VBox(8);
   
   // GUI Components
   // Labels
   private Label lblStartStop = new Label("Start the server: ");
   
   // Textfields
   private TextField tfFolder = new TextField();
   
   // Buttons
   private Button btnChooseFolder = new Button("Choose Folder");
   private Button btnStartStop = new Button("Start");
   
   // TextArea
   private TextArea taLog = new TextArea();

   // Socket
   private DatagramSocket mainSocket = null;
   private InetAddress iServer = null;
   
   // Port
   private int serverPort = TFTP_PORT;
   
   // I/O
   private File folder = null;
   
   /**
    * main program
    */
   public static void main(String[] args) {
      launch(args);
   }
   
   /**
    * Start, draw and set up GUI
    * Do server stuff
    */
   public void start(Stage _stage) {
      // Window setup
      stage = _stage;
      stage.setTitle("String teamName = null;'s TFTP Server");
      
      // listen for window close
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) { 
               System.exit(0);
            }
         } );
               
      // Row 1 (Top) - Choose Folder Button
      FlowPane fpTop = new FlowPane(8,8);
      fpTop.setAlignment(Pos.CENTER);
      fpTop.getChildren().add(btnChooseFolder);
      root.getChildren().add(fpTop);
      
      // Row 2 - Folder Text Field with Scrollbar
      // Initial Folder (P02)
      folder = new File(".");
      tfFolder.setFont(Font.font(
         "MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      tfFolder.setText(folder.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      tfFolder.setDisable(true);  // .................................................do we want this disabled?
      // add Scrollbar to text field (P02)
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolder);
      root.getChildren().add(sp);
      
      // Row 3 - Server Start (and Stop) Button
      FlowPane fp3 = new FlowPane(8,8);
      fp3.setAlignment(Pos.CENTER);
      // style button
      btnStartStop.setStyle("-fx-background-color: #30c224;");  // green
      fp3.getChildren().addAll(lblStartStop, btnStartStop);
      
      // Row 4 (Bottom) - Text Area (log)
      FlowPane fpBot = new FlowPane(8,8);
      fpBot.setAlignment(Pos.CENTER);
      taLog.setPrefWidth(360);
      taLog.setPrefHeight(320);
      taLog.setWrapText(true);
      taLog.setEditable(false);
      fpBot.getChildren().add(taLog);
   
      // add remaining FlowPanes/GUI components to root
      root.getChildren().addAll(fp3, fpBot);
      
      // Listen for buttons
      btnStartStop.setOnAction(this);
      btnChooseFolder.setOnAction(this);
      
      // Show window
      scene = new Scene(root, 400, 450);
      // connect stylesheet
      scene.getStylesheets().add("/styles.css");
      stage.setScene(scene);
      // set stage position
      stage.setX(800);
      stage.setY(100);
      stage.show();    
   }
   
   /** ActionEvent handler for button clicks*/
   public void handle(ActionEvent ae) {
      String command = ((Button) ae.getSource()).getText();
      
      switch(command) {
         case "Choose Folder":
            doChooseFolder();
            break;
         case "Start":
            doStartStop();
            break;
         case "Stop":
            doStartStop();
            break;
         default:
            log("Invalid command");
            break;
      }
   } // end handle
   
   /**
    * doStartStop
    */
   public void doStartStop() {
      if(btnStartStop.getText().equals("Start")) {  // change Start btn to Stop btn
         btnStartStop.setText("Stop");
         btnStartStop.setStyle("-fx-background-color: #ff0000;"); // change button color to red
         lblStartStop.setText("Stop the server: ");
         tfFolder.setDisable(true);  // P02
         btnChooseFolder.setDisable(true);  // P02
         // create and start a ServerThread
         ServerThread st = new ServerThread();
         st.start();
      } else  {  // change Stop btn to Start btn
         btnStartStop.setText("Start");
         btnStartStop.setStyle("-fx-background-color: #30c224;"); // change button color to green
         lblStartStop.setText("Start the server: ");
         tfFolder.setDisable(false);  // P02
         btnChooseFolder.setDisable(false);  // P02
         // close socket
         if(mainSocket != null) {
            try {
               mainSocket.close();
            }
            catch(Exception e) {
               mainSocket = null;  // reset socket
            }
         }
      }
   }
   
   /**
    * doChooseFolder
    */
   public void doChooseFolder() {
      DirectoryChooser dirChooser = new DirectoryChooser();
      dirChooser.setInitialDirectory(new File(tfFolder.getText()));
      dirChooser.setTitle("Select Folder for Uploads and Downloads");
      folder = dirChooser.showDialog(stage);
//       dirChooser.setInitialDirectory(folder);  // ............................................................FIX
      System.out.println("Folder = " + folder);
      tfFolder.setText(folder.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      
      // NEED TO ADD ERROR CATCH/CHECK IF USER CANCELS WITHOUT CHOOSING FOLDER  ...............................ADD
   }


   // Inner Class
   // P05
   class ServerThread extends Thread {
      public void run() {
         // Server stuff ... wait for a packet and process it
         try {
            iServer = InetAddress.getLocalHost();
            // mainSocket is a DatagramSocket declared in the global scope
            // and initialized to null
            mainSocket = new DatagramSocket(serverPort);   // Binds the socket to the port
         }
         catch(UnknownHostException uhe) {
            // log();
            return;
         }
         catch(IOException ioe) {
            log("Exception " + ioe);
            return;
         }
         
         // wait for a packet from a new client, then
         // start a client thread
         while(true) {
            // The socket for the client is created in the client thread
            // create DatagramPacket of max size to receive first packet from a client
            byte[] holder = new byte[MAX_PACKET];
            DatagramPacket recPacket = new DatagramPacket(holder, MAX_PACKET);
            try {
               // We get a DatagramPacket, instead of a Socket, in the UDP case
               mainSocket.receive(recPacket);  // wait for 1st packet
            }
            catch(IOException ioe) {
               // Happens when the mainSocket is closed while waiting
               // to receive - This is how we stop the server.
               return;
            }
               
            // Create a thread for the client
            // Instead of passing a Socket to the client thread, we pass the 1st packet
            ClientThread ct = new ClientThread(recPacket); // client socket will be created in client thread
            ct.start();
         } // of while loop
      } // of run
      
      public void stopServer() {
         try {
            mainSocket.close();  // This terminates any blocked accepts
         }
         catch(Exception e) {
            log("Exception " + e);
         }
      } // end stopServer
   } // end of inner class ServerThread


   // Inner Class
   class ClientThread extends Thread {
      // Since attributes are per-object items, each ClientThread has its OWN
      // socket, unique to that client
      private DatagramSocket cSocket = null;
      private DatagramPacket firstPkt = null;
   
      // Constructor for ClientThread
      public ClientThread(DatagramPacket _pkt) {
         firstPkt = _pkt;
         // So - the new DatagramSocket is on a DIFFERENT port,
         // chosen by the OS. If we use cSocket from now on, then
         // port switching has been achieved.
      }
      
      // main program for a ClientThread
      public void run() {
         log("Client packet received!");
         
         try {
            // create a DatagramSocket on an available port
            cSocket = new DatagramSocket();
            cSocket.setSoTimeout(1000);  // set a timeout on the socket (P07)
         } 
         catch (Exception e) {
            log("ClientThread Exception:" + e);
            return;
         } 
         
         try {
            // In this try-catch run the protocol, using firstPkt as
            // the first packet in the conversation
            Packet packet = new Packet();
            packet.packetChecker(firstPkt);
            int opcode = packet.getOpcode();
            
            if (opcode == RRQ) {
               log("RRQ Packet received");
               doDownload(firstPkt);
            } else if (opcode == WRQ) {
               log("WRQ Packet received");
               doUpload(firstPkt);
            }
            else {
               log("Unexpected opcode...");
               // ERROR PACKET CREATE AND SEND
               return;
            }    
         }
         catch(Exception e) {
            log("Exception " + e);
            // For TFTP, probably send an ERROR packet here
            // ERROR
            return;
         }
      
         /*
            More from P05: 
            As the conversation progresses, to receive a packet:
               byte[] holder = new holder[MAX_PACKET];
               DatagramPacket incoming = new DatagramPacket(holder, MAX_PACKET);
               cSocket.receive(incoming);
               Then - dissect the incoming packet and process it
            THE NEXT SET OF NOTES DISCUSSES HOW TO DISSECT PACKETS
      
               To send a packet:
               Compute the contents of the outgoing packet
               Build the packet ... producing a DatagramPacket, outgoing
               cSocket.send(outgoing);
            THE NEXT SET OF NOTES ALSO DISCUSSES HOW TO BUILD PACKETS
         
               log("Client disconnected!\n");
         */
      
         log("Client completed!");
      } // end run 
      
      private void doUpload(DatagramPacket dgmPkt) {
         String fileName = null;
         int blockNo = 0;
         int opcode = 0;
         int size = 512;
         DataOutputStream dos = null;
         
         log("Start doUpload");
         
         // Opcode checked as WRQ -> create and dissect
         WRQPacket wrqPkt = new WRQPacket();
         wrqPkt.dissect(dgmPkt);
         
         serverPort = wrqPkt.getPort();
         
         // file to upload
         fileName = wrqPkt.getFileName();
         
         // create ACK
         // P06
         ACKPacket ackPkt = new ACKPacket(iServer, serverPort, blockNo);
                                     //  (InetAddress _toAddress, int _port, int _blockNo)
         dgmPkt = ackPkt.build();
         
         log("Server sending ACKPacket " + blockNo);
         try {
            // send to Client
            cSocket.send(dgmPkt);
         }
         catch(Exception e) {
            // ERROR
            return;
         }
         
         try {
            while(size == 512) {  // size initially set to 512, so first pass will always go in while loop
               // prepare to receive DatagramPacket of max packet size
               dgmPkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET); 
               try {  // P07
                  cSocket.receive(dgmPkt);   
               }
               catch(SocketTimeoutException ste) {  // P07
                  log("DATA not received - upload timed out");
                  return;
               }
               
               log("Server received reply");
               
               Packet packet = new Packet();  // create generic packet - don't know opcode yet
               packet.packetChecker(dgmPkt);
               // generic packet contains opcode, InetAddress, and port                  
               serverPort = packet.getPort();            
               iServer = packet.getAddress();
               opcode = packet.getOpcode();
               // check received packet's op code... should be 3 (DATA packet)
               if(opcode != DATA) {
                  // CREATE ERROR PACKET AND SEND
                  // code...
                  return;
               }
            
               byte[] maxData = new byte[512];
               
               // increment blockNo
               blockNo++;
            
               // opcode checked as 3 (DATAPacket)
               // DATAPacket dataPkt = new DATAPacket(iServer, serverPort, blockNo, maxData, size);
                                                  // (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)            
               DATAPacket dataPkt = new DATAPacket();
               dataPkt.dissect(dgmPkt);
               
               // set size to length of data in DATAPacket
               size = dataPkt.getDataLen();
            
               // do on first pass (when dos is not instantiated)
               if(dos == null) {  //
                  log("Download: Opening file: " + fileName);
               
                  dos = new DataOutputStream(new FileOutputStream(fileName)); 
               }
               
               // write data
               dos.write(dataPkt.getData());            

               // create ACKPacket
               // P06
               ackPkt = new ACKPacket(iServer, serverPort, blockNo);
               // create DatagramPacket using ackPkt
               dgmPkt = ackPkt.build();
               // send DatagramPacket
               log("Server sending ACKPacket " + blockNo);
               cSocket.send(dgmPkt);
            } // end while
         }
         catch (Exception e) {
            log("Exception during upload " + e);
            // ERROR
            return;
         }
         finally {
            // Close socket when upload is complete or if error occurs
            try {
               cSocket.close();
            }
            catch(Exception e) {
            log("Exception during upload " + e);
            // ERROR
               return;
            }
         }
         
         // log file uploaded complete
         log("Upload " + fileName + " complete");
         
      } // end doUpload
      
      private void doDownload(DatagramPacket dgmPkt) {   
         String fileName = null;
         int blockNo = 0;
         int opcode = 0;
         int readSize = 512;
         DataInputStream dis = null;
         
         log("Starting Download");
         
         // Opcode checked as RRQ -> create and dissect
         RRQPacket rrqPkt = new RRQPacket();
         rrqPkt.dissect(dgmPkt);
         
         // file to download
         fileName = rrqPkt.getFileName();
               
         serverPort = rrqPkt.getPort();
         
         // check if file requested exists in folder
         File dir = new File(folder.getAbsolutePath());
         File[] folderContents = dir.listFiles();
         for (File f: folderContents) {
            if(fileName.equals(f.getName())) {
               break;
            } else {
               // ERROR - file doesn't exist
               log("No such file exists...");
               return;
            }
         }
      
         log("Opening file " + fileName);
         
         try { 
            while(readSize == 512) {  // readSize initially set to 512, so first pass will always go in while loop
               try {
                  // do on first pass (when dis is not instantiated)
                  if (dis == null) {                            
                     try {
                        dis = new DataInputStream(new FileInputStream(new File(fileName)));
                     }
                     catch(Exception e) {
                     // ERROR
                        return;
                     }
                  }
               
                  byte[] maxData = new byte[512];  // prepare to read max number of bytes
                  
                  try {
                     readSize = dis.read(maxData);  // set readSize to number of bytes read, allowing for up to 512
                  } 
                  // Account for errors, empty files, and reaching end of file           
                  catch(EOFException eofe) {
                     readSize = 0;
                  }
                  catch(Exception e) {
                     log("Exception " + e);
                     return;
                  }
                     
                  if (readSize == -1) {
                     readSize = 0;
                  }
               
                  // increment blockNo
                  blockNo++;
               
                  // create DATAPacket to send file contents
                  // P06
                  DATAPacket dataPkt = new DATAPacket(iServer, serverPort, blockNo, maxData, readSize);
                  //                   (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)
                  dgmPkt = dataPkt.build();
                     
                  log("Server sending DATAPacket " + blockNo);
                  // send DatagramPacket
                  cSocket.send(dgmPkt);              

                  // prepare to receive a datagram and allow to receive max packet size
                  dgmPkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
                  try {
                     cSocket.receive(dgmPkt);
                  }
                  catch(SocketTimeoutException ste) {  // P07
                     return;
                  }
               
                  log("Server received packet");
                  
                  Packet packet = new Packet();
                  packet.packetChecker(dgmPkt);
                  // generic packet contains opcode, InetAddress, and port  
                  serverPort = packet.getPort();               
                  iServer = packet.getAddress();
                  opcode = packet.getOpcode();
                  // check received packet's op code... should be 4 (ACK packet)
                  if(opcode != ACK) {
                     // CREATE ERROR PACKET AND SEND
                     String errMsg = "Unexpected opcode";
                     ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, ILLOP, errMsg);
                     //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
                     DatagramPacket dgmErr = errPkt.build();
                     cSocket.send(dgmErr);
                     return;
                  }
               }
               catch(Exception e) {
                  log("Exception during download " + e);
                  return;
               }
            } // end while
         }
         catch(Exception e) {
            log("Exception during download " + e);
            // Error
            return;
         }
         
         finally {
            // Close socket when download is complete or if error occurs
            try {
               cSocket.close();
            }
            catch(Exception e) {
               log("Error closing socket");
            }
         }
         
         // log file downloaded complete
         log("Download " + fileName + " complete");
         
      } // end doDownload
   } // End of inner class TFTPClientThread
   
   
   // utility method "log" to log a message in a thread safe manner
   private void log(String message) {
      Platform.runLater(
         new Runnable() {
            public void run() {
               taLog.appendText(message + "\n");
            }
         } );
   } // of log
} // end of TFTPServer