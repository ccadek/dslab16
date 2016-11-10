Reflect about your solution!

Summary:
Client:
Each Client uses a single Socket to communicate with the server. main() sets up the whole client by registering the shell, and executing the client.
The shell listens for input on System.in and executes the correct method from the registered Client. Each Client method sends its request to the server without
listening for responses.
Client.run() listens for responses from the server. All responses from the server are displayed on System.out by the shell.
Responses starting with "!pub" are saved and then displayed by the shell.
Registering the client spawns a PrivateMessageListener-thread, which listens for incoming private messages from other clients and displays them directly.


ChatServer:
Spawns two Listenerthreads (TCPListener & UDPListener) that listen for requests. Listeners will create new Chatserver instances once new clients connect.
Connections over TCP will be kept alive since the Client only uses one Socket for the duration of its lifetime. Connections via UDP will be closed once the request is finished.
Once a new request from a client comes in, the chatserver will delegate processing to chatserver.executor.RequestParser. The Executor processes the request and calls chatserver.answer()
to send a response back to the client.

chatserver.Executor:
* Answers:
    A simple class containing a few responses, so that all responses are consistent
* ExitExecutor:
    Once a client wants to close the connection, it sends "!exit". This will close the corresponding socket, BufferedReader and Printwriter on the server and end the thread dedicated to their communication.
* ListExecutor:
    Used when the client sends "!list". This is the only request via UDP and does not require a login. The Executor parses a list of all logged in users
    formatted as "user1, user2,..." and sends it back to the client. Since this command works completely anonymous. Each request is handled in a new Thread and is not kept alive after the response.
* LoginExecutor:
    A client wants to login using the provided credentials (username + password). The executor checks user.properties for correctness and adds the client to the userMap.
    If the client is already logged in or uses the wrong credentials, the executor responds accordingly.
* LogoutExecutor:
    A client wishes to logout. Client will be logged out and get a response. If he was never logged in to begin with, the executor responds accordingly
* LookupExecutor:
    Looks up a registered user by the provided user name and sends his IP:port address back. If the user is not found, a suitable response is sent back.
* MsgExecutor:
    This works the same as the LookupExecutor, but the response starts with "!priv". The client therefore will not display the response,
    but use the address to send a private message directly to another user.
* RegisterExecutor:
    This will register a client on the provided IP:port address. If other client use "!lookup" the server will respond with this address.
* RequestParser:
    Is utilised by the Chatserver to determine the correct Executor to process the request. Also checks if the user is logged in for certain requests.
    If no Executor is applicable, an IllegalArgumentsException is thrown with the correct error message.
* SendExecutor:
    Used if a client wishes to send a public message to all logged in clients.
Note: Each executor does not check if any request (!login, !logout,...) has the correct parameters since the client takes care of this.