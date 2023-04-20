import java.net.*;
import java.io.*;

public class WRQPacket extends Packet implements TFTPConstants
{
   //attributes
   private String fileName;
   private String mode;
   
   //constructor
   public WRQPacket (InetAddress _toAddress, int _port, String _fileName, String _mode)
   {
      super(_toAddress, _port, WRQ);
      fileName = _fileName;
      mode = _mode;
   }
   
   public WRQPacket(){}
   
   //accessors
   public String getFileName()
   {
      return fileName;
   }
   
   public String getMode()
   {
      return mode;
   }
      
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
      DatagramPacket wrqPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());
      
      return wrqPkt;
   }
   
   public void dissect(DatagramPacket wrqPkt)
   {
      super.setAddress(wrqPkt.getAddress()); //calling super mutators to set the address and port
      super.setPort(wrqPkt.getPort());
      
      ByteArrayInputStream bais = new ByteArrayInputStream(wrqPkt.getData(), wrqPkt.getOffset(), wrqPkt.getLength());
      DataInputStream dis = new DataInputStream(bais);
      
      try
      {
         super.setOpcode(dis.readShort());
         fileName = readToZ(dis);
         System.out.println("Dissect Filename: " + fileName);
      //          dis.readShort();
         mode = readToZ(dis);
         System.out.println("Dissect Mode: " + mode);
      //          dis.readShort();
         
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
