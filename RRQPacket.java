import java.net.*;
import java.io.*;

public class RRQPacket extends Packet implements TFTPConstants
{
   //attributes
   private String fileName;
   private String mode;

   //constructor
   public RRQPacket (InetAddress _toAddress, int _port, String _fileName, String _mode)
   {
      super(_toAddress, _port, RRQ);
      fileName = _fileName;
      mode = _mode;
   }
   
   public RRQPacket(){}
   
   //accessors
   public String getFileName()
   {
      return fileName;
   }
   
   public String getMode()
   {
      return mode;
   }
   
   //need to implement build
   //method to actually convert data which is read in binary and converts it into a packet, bytes
   //document 6 
   public DatagramPacket build()
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + fileName.length() + 1 + "octet".length() + 1);
      DataOutputStream dos = new DataOutputStream(baos);
      
      try
      {
         dos.writeShort(super.getOpcode());
         dos.writeBytes(fileName);
         dos.writeByte(0);
         dos.writeBytes("octet");
         dos.writeByte(0);
         
         // close
         dos.close();
      }
      
      catch(Exception e){}
            
      byte[] holder = baos.toByteArray();
      DatagramPacket rrqPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());
      
      return rrqPkt;
   }
   
   public void dissect(DatagramPacket rrqPkt)
   {
      System.out.println("Start RRQ Dissect method");
      super.setAddress(rrqPkt.getAddress());
      super.setPort(rrqPkt.getPort());
      
      ByteArrayInputStream bais = new ByteArrayInputStream(rrqPkt.getData(), rrqPkt.getOffset(), rrqPkt.getLength());
      DataInputStream dis = new DataInputStream(bais);
      
      try
      {
         super.setOpcode(dis.readShort());         
         fileName = readToZ(dis);
         System.out.println("Dissect Filename: " + fileName);
         //dis.readShort();
         mode = readToZ(dis);
         System.out.println("Dissect Mode: " + mode);
         //dis.readShort();
      
         // close
         dis.close();   
      }
      catch(Exception e){}
   }  
   
   public static String readToZ(DataInputStream dis) 
   {
      try
      {
         String value = "";
         while (true) 
         {
            byte b = dis.readByte();
            if (b == 0)
               return value;
            value += (char) b;
         }
      }
      
      catch(IOException ioe){}
      
      return "";
   }

}