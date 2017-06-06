# Waterfall Chat  
CS300 Term Project for Tegan Lamoureux.  

This program, designed via the waterfall process, will create a chatroom. It will consist of a server to which an indefinite number of clients connect, which allows them to chat with the entire server (chat room), or between individual clients (private messaging).  

Users (clients) will have accounts stored on the server, with login credentials and logs of all chats participated in. When connecting to the server, the user will enter their credentials. If these do not match any records in the server, they will receive an error message and a prompt to make a new user account. If it matches, they will be logged in, which does multiple things. It notifies all users (clients) on the server that the new user has logged in, it gives them access to account details, and gives them access to chat logs. It also allows them to send chats from their user account to the server, or other users. Along with this, once a user is logged in, they can see a list of all other users logged into the server.  

The program will be implemented such that it will run all on the same machine (server and all clients). It will not be capable of communicating across a network other than localhost for the time being. All passwords will be stored in plaintext in a data structure/text file, and therefore should not be sensitive passwords used for other accounts. User logs will also be stored in plaintext on the machine, and therefore should not contain data or information the user would like to keep private.
