Reflect about your solution!

Summary:
Client:
Contains a single Socket for Communication with Server. If the client registers itself on the chatserver, the Client spawns a TCPListener Thread which listens for incoming private messages and displays them on System.out.

ChatServer:
Spawns two Listenerthreads (TCPListener & UDPListener) that listen for requests. Listeners will create a new Chatserver instance on an incoming request.
The Chatserver delegates parsing of the Request to chatserver.util.RequestParser which parses an appropiate response. The Chatserver then sends this response back to the client.