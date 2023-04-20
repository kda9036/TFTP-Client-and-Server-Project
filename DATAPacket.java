import java.net.*;
import java.io.*;

public class DATAPacket extends Packet implements TFTPConstants
{
   //attributes
   private int blockNo;
   private int dataLen;
   private byte[] data;
   
   //constructor
   public DATAPacket (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)
   {
      super(_toAddress, _port, DATA);
      blockNo = _blockNo;
      data = _data;
      dataLen = _dataLen;
   }
   
   public DATAPacket(){}
   
   //accessor methods 
   public int getBlockNo()
   {
      return blockNo;
   }

   public byte[] getData()
   {
      return data;
   }
   
   public int getDataLen()
   {
      return dataLen;
   }

   public DatagramPacket build()
   {
      //creates the output array length
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + 2/*blockNo*/ + dataLen/*bytes 0-512*/);
      
      //sets up the data output stream and then writes all the necessary info
      DataOutputStream dos = null;
      try
      {
         dos = new DataOutputStream(baos);
         dos.writeShort(DATA);
         dos.writeShort(blockNo);
         dos.write(data, 0, dataLen);
      
         // Close
         dos.close();
      }
      catch(Exception e) {
         System.out.println(e);
      }
      
      byte[] holder = baos.toByteArray();   // Get the underlying byte[]
      DatagramPacket dataPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());
      return dataPkt; //returns the packet
   }
   
   
   public void dissect(DatagramPacket dataPkt)
   {
      // Create a ByteArrayInputStream from the payload
      // NOTE: give the packet data, offset, and length to ByteArrayInputStream
      ByteArrayInputStream bais =
         new ByteArrayInputStream(dataPkt.getData(), dataPkt.getOffset(), dataPkt.getLength());
   
      DataInputStream dis = new DataInputStream(bais);
      
      try
      {
         super.setOpcode(dis.readShort());
         blockNo = dis.readShort();
         data = new byte[dataPkt.getLength()-4];
         dataLen = dataPkt.getLength()-4;
         for (int i = 0; i < data.length; i++)
         {
            data[i] = dis.readByte();
         }
         
         // Close
         dis.close();
      }
      catch (Exception e)
      {
         System.out.println(e);
      }
   }
}