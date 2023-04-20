import java.net.*;
import java.io.*;

public class ERRORPacket extends Packet implements TFTPConstants
{
   //attributes
   private int errorNo;
   private String errorMsg;
   
   //constructor
   public ERRORPacket (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
   {
      super(_toAddress, _port, ERROR);
      errorNo = _errorNo;
      errorMsg = _errorMsg;
   }
   
   //accessor methods  
   public String getErrorMsg()
   {
      return errorMsg;
   }
   
   public int getErrorNo()
   {
      return errorNo;
   }
   
   public DatagramPacket build()
   {
      //creates the output array length
      ByteArrayOutputStream baos = new ByteArrayOutputStream(
         2 /*opcode*/ + 2/*Ecode*/ + errorMsg.length() + 1 /*0*/);
      
      //sets up the data output stream and then writes all the nessary info
      DataOutputStream dos = null;
      try
      {
         dos = new DataOutputStream(baos);
         dos.writeShort(5);
         dos.writeShort(errorNo);
         dos.writeBytes(errorMsg);
         dos.writeByte(0);
      }
      catch (Exception e)
      {
         System.out.println(e);
         /*return;
          *^^can't return without return type
          *possible to change
          */
      }
      
      //closes the dataoutputstream
      try
      {
         dos.close();
      }
      catch(Exception e) {}
      
      byte[] holder = baos.toByteArray();   // Get the underlying byte[]
      DatagramPacket errPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());  // Build a DatagramPacket from the byte[]
      return errPkt; //returns the packet
   }
   
   
   
   
   public void dissect(DatagramPacket errPkt)
   {
      // Create a ByteArrayInputStream from the payload
      // NOTE: give the packet data, offset, and length to ByteArrayInputStream
      ByteArrayInputStream bais =
         new ByteArrayInputStream(errPkt.getData(), errPkt.getOffset(), errPkt.getLength());
   
      DataInputStream dis = new DataInputStream(bais);
      
      //reads the information
      try
      {
         errorNo = dis.readShort();
         errorMsg = readToZ(dis);

         // CLOSE 
         dis.close();
      }
      catch(Exception e) {}
   }
   
   
   // Utility method
   public static String readToZ(DataInputStream dis)
   {
      String value = "";
      while (true)
      {
         try
         {
            byte b = dis.readByte();
            if (b == 0)
               return value;
            value += (char) b;
         }
         catch (Exception e)
         {
            System.out.println(e);
         }
      }
   }
}