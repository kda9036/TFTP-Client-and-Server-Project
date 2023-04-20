import java.net.*;
import java.io.*;

public class ACKPacket extends Packet implements TFTPConstants
{
   //attributes
   private int blockNo;
   
   //constructor
   public ACKPacket (InetAddress _toAddress, int _port, int _blockNo)
   {
      super(_toAddress, _port, ACK);
      blockNo = _blockNo;
   }
   
   public ACKPacket() {}
   
   //accessor methods   
   public int getBlockNo()
   {
      return blockNo;
   }
   
   public DatagramPacket build()
   {
      //creates the output array length
      ByteArrayOutputStream baos = new ByteArrayOutputStream(
         2 /*opcode*/ + 2/*blockNo*/);
      
      //sets up the data output stream and then writes all the nessary info
      DataOutputStream dos = null;
      try
      {
         dos = new DataOutputStream(baos);
         dos.writeShort(ACK);
         dos.writeShort(blockNo);
         
         // Close
         dos.close();
      }
      catch (Exception e)
      {
         System.out.println(e);
      }
      
      byte[] holder = baos.toByteArray();   // Get the underlying byte[]
      DatagramPacket ackPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());  // Build a DatagramPacket from the byte[]
      return ackPkt; //returns the packet
   }
   
   
   public void dissect(DatagramPacket ackPkt)
   {
      // Create a ByteArrayInputStream from the payload
      // NOTE: give the packet data, offset, and length to ByteArrayInputStream
      ByteArrayInputStream bais =
         new ByteArrayInputStream(ackPkt.getData(), ackPkt.getOffset(), ackPkt.getLength());
   
      DataInputStream dis = new DataInputStream(bais);
      
      //gets opcode
      try
      {
         super.setOpcode(dis.readShort());
         blockNo = dis.readShort();
         
         // Close
         dis.close();
      }
      catch (Exception e)
      {
         System.out.println(e);
      }
   }
}