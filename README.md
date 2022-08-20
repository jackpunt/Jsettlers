# Jsettlers

JSettlers is a web-based version of the board game Settlers of Catan
written in Java. This client-server system supports multiple
simultaneous games between people and computer-controlled
opponents. Initially created as an AI research project.

The client may be run as a Java application, or as an applet when
accessed from a web site which also hosts a JSettlers server.

The server may be configured to use a MySQL database to store account
information.  A client applet to create user accounts is also
provided.

JSettlers is an open-source project licensed under the GPL. The
software is maintained as a SourceForge project at
http://sourceforge.net/projects/jsettlers.

Forums for discussions and community based support are provided at
SourceForge.

start server: 

  > java -jar target/JSettlersServer.jar PORT 5 dbUser dbPass Able '' Baker '' Charlie '' Dawn '' &

start client:

  > java -jar JSettlers.jar SERVER_HOST SERVER_PORT
