# TFTP-Client-and-Server-Project

RIT Spring 2021

Course: Computational Problem Solving in the Information Domain II

See ProjectDemo > view raw for video demo of project

# Project Description

Group term project. Group of 3.

Write a TFTP client and server.

The client and server communicate via UDP. The server is multi-threaded, meaning that several clients may be communicating with it at once. To enable this, the server accepts the initial packet in a conversation from the client through its published port (69), but then switches to another port for the reply and all further communications during that conversation. This way the server can keep client conversations separated. The client has to know to switch ports to the new port when it receives the reply to its first packet. Except for the first packet to the server, each client has its own port for a conversation.

Your server must be multithreaded and employ port switching.

# Summary
- Coded a client and server in Java with two other group members that utilized the Trivial File Transfer Protocol (TFTP) to communicate with each other via User Datagram Protocol (UDP) to upload and download files
- Employed multi-threading and port switching in server creation
- Packet classes were generated using object-oriented principles, like inheritance, for building and dissecting datagrams
- Extensive testing was performed for error handling and to ensure interoperability with other clients and servers

# Technologies / Software

Java, JavaFX, CSS
