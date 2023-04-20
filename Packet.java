import java.net.*;
import java.io.*;

public class Packet implements TFTPConstants
{
   private InetAddress toAddress;//every class has these three
   private int port;
   private int opcode;
   
   public Packet(InetAddress _toAddress, int _port, int _opcode)
   {
      toAddress = _toAddress;
      port = _port;
      opcode = _opcode;
   }
   
   public Packet(){}
   
   public InetAddress getAddress()
   {
      return toAddress;
   }
   
   public int getPort()
   {
      return port;
   }
   
   public int getOpcode()
   {
      return opcode;
   }
   
   public void setAddress(InetAddress _toAddress)
   {
      toAddress = _toAddress;
   }
   
   public void setPort(int _port)
   {
      port = _port;
   }
   
   public void setOpcode(int _opcode)
   {
      opcode = _opcode;
   }
   
   public void packetChecker(DatagramPacket packet)//all packets share same dissect start, need to figure out how to implement in each method
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
      DataInputStream dis = new DataInputStream(bais);
      
      try
      {
         opcode = dis.readShort();
         this.setAddress(packet.getAddress());
         this.setPort(packet.getPort());
         bais.close();
         dis.close();
      }
      
      catch(Exception e)
      {
         System.out.println("Error in packetChecker: " + e);
      }
   }
}