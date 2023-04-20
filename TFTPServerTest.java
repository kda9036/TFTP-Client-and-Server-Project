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

public class TFTPServerTest extends Application implements TFTPConstants {
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
      fpTop.setAlignment(Pos.BASELINE_LEFT);
      fpTop.getChildren().add(btnChooseFolder);
      root.getChildren().add(fpTop);
      
      // Row 2 - Folder Text Field with Scrollbar
      // Initial Folder
      folder = new File(".");
      //System.out.println("Folder Name: " + folder.getName());
      tfFolder.setFont(Font.font(
         "MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      tfFolder.setText(folder.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      // add Scrollbar to text field
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolder);
      root.getChildren().add(sp);
      
      // Row 3 - Server Start (and Stop) Button
      FlowPane fp3 = new FlowPane(8,8);
      lblStartStop.setAlignment(Pos.BASELINE_LEFT);
      // style button
      btnStartStop.setStyle("-fx-background-color: #00ff00;");  // green
      fp3.getChildren().addAll(lblStartStop, btnStartStop);
      
      // Row 4 (Bottom) - Text Area (log)
      FlowPane fpBot = new FlowPane(8,8);
      fpBot.setAlignment(Pos.CENTER);
      taLog.setPrefWidth(400);
      taLog.setPrefRowCount(100);
      taLog.setWrapText(true);
      taLog.setEditable(false);
      fpBot.getChildren().add(taLog);
   
      // add remaining FlowPanes/GUI components to root
      root.getChildren().addAll(fp3, fpBot);
      
      // listen for buttons
      btnStartStop.setOnAction(
         new EventHandler<ActionEvent>() {
            public void handle(ActionEvent evt) {
               doStartStop();
            }
         });
      btnChooseFolder.setOnAction(
         new EventHandler<ActionEvent>() {
            public void handle(ActionEvent evt) {
               doChooseFolder();
            }
         });
      
      // Show window
      scene = new Scene(root, 400, 450);
      stage.setScene(scene);
      // set stage position
      stage.setX(800);
      stage.setY(100);
      stage.show();    
   }
   
   /**
    * doStartStop
    */
   public void doStartStop() {
      if(btnStartStop.getText().equals("Start")) {  // change Start btn to Stop btn
         btnStartStop.setText("Stop");
         btnStartStop.setStyle("-fx-background-color: #ff0000;"); // change button color to red
         lblStartStop.setText("Stop the server: ");
         tfFolder.setDisable(true);
         btnChooseFolder.setDisable(true);
         ServerThread st = new ServerThread();
         st.start();
      } else  {  // change Stop btn to Start btn
         btnStartStop.setText("Start");
         btnStartStop.setStyle("-fx-background-color: #00ff00;"); // change button color to green
         lblStartStop.setText("Start the server: ");
         tfFolder.setDisable(false);
         btnChooseFolder.setDisable(false);
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
      dirChooser.setInitialDirectory(folder);
      System.out.println("Folder = " + folder);
      tfFolder.setText(folder.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
   }


   // Inner Class
   class ServerThread extends Thread {
      public void run() {
         // Server stuff ... wait for a packet and process it
         try {
            iServer = InetAddress.getLocalHost();
            // mainSocket is a DatagramSocket declared in the global scope
            // and initialized to null
            mainSocket = new DatagramSocket(serverPort);   // Binds the socket to the port
            // wait for a packet from a new client, then
            // start a client thread
            while(true) {
               // create DatagramPacket of max size to receive first packet from a client
               // We get a DatagramPacket, instead of a Socket, in the UDP case
               DatagramPacket receivedPacket = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
               mainSocket.receive(receivedPacket);  // wait for 1st packet
               // Create a thread for the client
               // Instead of passing a Socket to the client thread, we pass the 1st packet
               ClientThread ct = new ClientThread(receivedPacket); // client socket will be created in client thread
               ct.start();
            } // of while loop
         }
         catch(UnknownHostException uhe) {
            // log();
            return;
         }
         catch(IOException ioe) {
            // log();
            // Happens when mainSocket is closed while waiting to receive - This is how we stop the server.
            return;
         }
      } // of run
      
      public void stopServer() {
         try {
            mainSocket.close();  // This terminates any blocked accepts
         }
         catch(Exception e) {
            log("Exception: " + e);
         }
      } // end stopServer
   } // end of inner class TFTPServerThread


   // Inner Class
   class ClientThread extends Thread {
      // Since attributes are per-object items, each ClientThread has its OWN
      // socket, unique to that client
      private DatagramSocket cSocket = null;
      private DatagramPacket dgmPkt = null;
   
      // Constructor for ClientThread
      public ClientThread(DatagramPacket _pkt) {
         dgmPkt = _pkt;
         // So - the new DatagramSocket is on a DIFFERENT port,
         // chosen by the OS. If we use cSocket from now on, then
         // port switching has been achieved.
      }
      
      // main program for a ClientThread
      public void run() {
         log("Client packet received!\n");
         try {
            cSocket = new DatagramSocket();
            cSocket.setSoTimeout(1000);
         } 
         catch (Exception e) {
            log("ClientThread Exception:" + e);
            return;
         } 
         
         try {
            // In this try-catch run the protocol, using dgmPkt as
            // the first packet in the conversation
            Packet packet = new Packet();
            int opcode = packet.packetChecker(dgmPkt);
            
            if (opcode == RRQ) {
               log("RRQ Packet received");
               doDownload(dgmPkt);
            } else if (opcode == WRQ) {
               log("WRQ Packet received");
               doUpload(dgmPkt);
            }
            else {
               log("Unexpected opcode...");
               // ERROR PACKET CREATE AND SEND
            }    
         }
         catch(Exception e) {
            log("IO Exception (3): " + e + "\n");
            // For TFTP, probably send an ERROR packet here
            return;
         }
      
      //       As the conversation progresses, to receive a packet:
         // byte[] holder = new holder[MAX_PACKET];  ............................................need to uncomment
         // DatagramPacket incoming = new DatagramPacket(holder, MAX_PACKET);  ...................need to uncomment
      //          cSocket.receive(incoming); ...........................................................need to uncomment
      //       Then - dissect the incoming packet and process it
      // THE NEXT SET OF NOTES DISCUSSES HOW TO DISSECT PACKETS
      
      //       To send a packet:
      //       Compute the contents of the outgoing packet
      //       Build the packet ... producing a DatagramPacket, outgoing
      //          cSocket.send(outgoing); ..............................................................need to uncomment
      // THE NEXT SET OF NOTES ALSO DISCUSSES HOW TO BUILD PACKETS
         
      //         log("Client disconnected!\n");
         log("Client completed!\n");
      } // end run 
      
      private void doUpload(DatagramPacket dgmPkt) {
         String fileName = null;
         int blockNo = 0;
         int size = 512;
         DataOutputStream dos = null;
         
         log("Start doUpload");
         
         WRQPacket wrqPkt = new WRQPacket();
         wrqPkt.dissect(dgmPkt);
         
         System.out.println("WRQ dissected");
         
         serverPort = wrqPkt.getPort();
         
         // file to upload
         fileName = wrqPkt.getFileName();
         
         // create ACK
         ACKPacket ackPkt = new ACKPacket(iServer, serverPort, blockNo);
         //  (InetAddress _toAddress, int _port, int _blockNo)
         dgmPkt = ackPkt.build();
         
         System.out.println("Ack build");
         
         log("Server sending ACKPacket " + blockNo);
         try {
            // send to Client
            ERRORPacket testPkt = new ERRORPacket(iServer, serverPort, 2, "Test Error");
            dgmPkt = testPkt.build();
            cSocket.send(dgmPkt);
         }
         catch(Exception e) {
            // ERROR
            System.out.println("Exception");
            return;
         }
         
         try {
         
         while(size == 512) {
            DatagramPacket dgmPktRec = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET); 
            try {
               cSocket.receive(dgmPktRec);   
            }
            catch(SocketTimeoutException ste) {
               log("DATA not received - upload timed out");
               return;
            }
            log("Server Received reply");
               
            Packet packet = new Packet();
               // check received packet's op code... should be 3 (DATA packet)
            if(/* check op code */ packet.packetChecker(dgmPktRec) != DATA) {
                     // CREATE ERROR PACKET AND SEND
                     // code...
               return;
            }
                  
            serverPort = packet.getPort();
            //                   log("packet.getPort method: " + serverPort);
            
            iServer = packet.getAddress();
            //                   log("packet.getAddress method: " + iServer);
            
            byte[] maxBlock = new byte[512];
               
            blockNo++;
            
               // opcode checked as 3
         //                DATAPacket dataPkt = new DATAPacket(iServer, serverPort, blockNo, maxBlock, size);
               //                   (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)
         
            DATAPacket dataPkt = new DATAPacket();
            dataPkt.dissect(dgmPktRec);
         
            size = dataPkt.getDataLen();
            
               // do on first pass (when dos is not instantiated)
            if(dos == null) {  //
               log("Download: Opening file: " + fileName);
            
               dos = new DataOutputStream(new FileOutputStream(fileName)); 
            }
               
               // NEED CODE FOR GETTING DATA TO WRITE
            dos.write(dataPkt.getData());            
               
         
               // create ACKPacket
            ackPkt = new ACKPacket(iServer, serverPort, blockNo);
            dgmPkt = ackPkt.build();
            log("Server sending ACKPacket " + blockNo);
            cSocket.send(dgmPkt);
         } // end while
      }
      catch (Exception e) {
         // ERROR
         return;
      }
      finally {
         // CLOSE
         try {
            cSocket.close();
         }
         catch(Exception e) {
            // ERROR
            return;
         }
      }
         
      } // end doUpload
      
      private void doDownload(DatagramPacket dgmPkt) {   
         String fileName = null;
         int blockNo = 0;
         int size = 512;
         DataInputStream dis = null;
         
         log("Start doDownload");
         
         RRQPacket rrqPkt = new RRQPacket();
         rrqPkt.dissect(dgmPkt);
         
         fileName = rrqPkt.getFileName();
               
               
         serverPort = rrqPkt.getPort();
         
         // check if file exists in folder
         File dir = new File(folder.getAbsolutePath());
         File[] folderContents = dir.listFiles();
         for (File f: folderContents) {
            if(fileName.equals(f.getName())) {
               break;
            } else {
               // ERROR - file doesn't exist
               System.out.println("No such file exists...");
               return;
            }
         }
      
         log("Opening file " + fileName);
         
         try {
         
            while(size == 512) {
               try {
                  if (dis == null) {                            
                     try {
                        dis = new DataInputStream(new FileInputStream(new File(fileName)));
                     }
                     catch(Exception e) {
                     // ERROR
                        return;
                     }
                  }
               
                  byte[] maxBlock = new byte[512];
                  int readSize = 0;
                  try {
                     readSize = dis.read(maxBlock);
                  }            
                  catch(EOFException eofe) {
                     readSize = 0;
                  }
                  catch(IOException ioe) {
                     return;
                  }
                     
                  if (readSize == -1) {
                     readSize = 0;
                  }
               
                  blockNo++;
               
               // create DATAPacket to send file contents
                  DATAPacket dataPkt = new DATAPacket(iServer, serverPort, blockNo, maxBlock, readSize);
                  //                   (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)
                  dgmPkt = dataPkt.build();
                     
                  log("Server sending DATAPacket " + blockNo);
                  cSocket.send(dgmPkt);               
                  size = readSize;
                  
                  dgmPkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
                  try {
                     cSocket.receive(dgmPkt);
                  }
                  catch(SocketTimeoutException ste) {
                     return;
                  }
               
                  log("Server received packet");
                  
                  Packet packet = new Packet();
                  // check received packet's op code... should be 4 (ACK packet), check blockNo... should be 0
                  if(/* check op code */ packet.packetChecker(dgmPkt) != ACK /* || check blockNo */) {
                     // CREATE ERROR PACKET AND SEND
                     String errMsg = "Bad opcode";
                     ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, ILLOP, errMsg);
                     //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
                     DatagramPacket dgmErr = errPkt.build();
                     cSocket.send(dgmErr);
                     return;
                  }
               }
               catch(Exception e) {
                  return;
               }
            } // end while
         }
         catch(Exception e) {
            // Error
            return;
         }
         
         // CLOSE SOCKET
         finally {
            try {
               cSocket.close();
            }
            catch(Exception e) {
               log("Error closing socket");
            }
         }
         // log file uploaded complete
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