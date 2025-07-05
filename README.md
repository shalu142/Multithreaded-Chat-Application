# Multithreaded-Chat-Application

*COMPANY*: CODTECH IT SOLUTIONS PVT. LTD.

*NAME*: SHALU

*INTERN ID*: CT06DF405

*DOMAIN*: JAVA PROGRAMMING

*DURATION*: 6 WEEKS

*MENTOR*: NEELS SANTHOSH KUMAR

DESCRITON: Java Multithreaded Chat Application

This repository contains a complete implementation of a Client-Server Chat Application built using Java Sockets and Multithreading. The system supports multiple clients communicating in real time with a central server. It is ideal for understanding core networking, concurrency, and real-world communication systems using Java. This project demonstrates the fundamental concepts of socket programming, thread management, and inter-client communication using a central server.

This chat application was developed with the goal of providing a reliable, scalable, and extensible platform for real-time text communication between users. It can be used for academic learning, small team collaborations, or as a base to build more advanced communication tools. The use of Java Sockets ensures low-level control over data transmission, and multithreading allows for simultaneous client handling, making it a great educational example of concurrent programming.

🚀 Features

This chat application goes beyond basic functionality and includes:

✅ Core Features:

Multithreading Support: Each client connection runs in its own thread on the server, allowing multiple users to chat simultaneously.

Real-Time Messaging: Clients can send and receive messages instantly via TCP sockets, ensuring minimal delay and instant communication.

🎯 Advanced Features:

👤 Username Support: Each user is prompted to enter a username which is used to identify messages.

👥 Group Messaging: Clients can send messages visible to all other users.

📩 Private Messaging: Users can send private messages using @username syntax, which ensures only the target recipient sees the message.

👥 Online User List: Users can type @list to see the list of all connected users in real time.

🕒 Timestamps: All messages are timestamped with the current time to provide context and history.

📁 File Sharing Support:  Clients can request files from the server using /get filename. Files are stored in the server’s uploads/ directory and downloaded to the client’s downloads/ folder.

📝 Chat Logging: Every chat session is logged to a chatlog.txt file on the server. This can be useful for moderation or archival.

🔁 Reconnect Support: If the client fails to connect, it automatically retries up to 5 times with a delay, improving reliability.

🔚 Exit Support: Users can type bye at any time to gracefully leave the chat. The server notifies others when a user disconnects.

📁 Project Structure

task3/

├── server/

│   └── Server.java

├── client/

│   └── Client.java

├── uploads/              // Server's file storage

├── downloads/            // Client's download location

├── chatlog.txt           // Log of public messages

└── README.md


🧪 How to Run

1️⃣ Compile the Server

javac server.java
java server

2️⃣ Compile the Client

javac client.java
java client

You can open multiple terminals or command prompts and run the client program from each to simulate a real-time group chat environment.

💬 Commands

@list – Displays the list of currently connected users.

@username message – Sends a private message to the specified user.

/get filename – Downloads a file from the server’s uploads/ folder.

bye – Disconnects from the server and exits the client.


🔒 Error Handling
If the server is not available, the client will retry connection up to 5 times.

If a file requested does not exist, the server notifies the client.

Disconnection is handled gracefully with status messages.

Enter your username:

Alice

[10:15] Bob: Hello everyone!

 @list

Users online: Bob, Alice

 @Bob Can you share the notes?

/get report.pdf

📁 File downloaded: /downloads/report.pdf


This application is a solid foundation for building more complex systems. With some enhancements, it could support user authentication, graphical interfaces, end-to-end encryption, or even voice and video calls. For now, it serves as a clean and educational implementation of a terminal-based chat system using core Java technologies.

Output:

👨‍💻 Author
Shalu Baloda
Java Intern at CODTECH IT SOLUTIONS 
This project is part of a practical internship assignment showcasing networking and multithreaded application development.

📄 License
This project is open-source and free to use for educational purposes.
