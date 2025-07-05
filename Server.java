// Server.java
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static final String LOG_FILE = "chatlog.txt";

    public static void main(String[] args) {
        new File("uploads").mkdirs();
        System.out.println("Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
        logMessage(message);
    }

    static void sendPrivate(String recipient, String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.name.equals(recipient)) {
                    client.sendMessage("(private) " + message);
                    return;
                }
            }
        }
    }

    static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    static String getUserList() {
        StringBuilder list = new StringBuilder();
        synchronized (clients) {
            for (ClientHandler client : clients) {
                list.append(client.name).append(", ");
            }
        }
        return list.length() > 0 ? list.substring(0, list.length() - 2) : "No users online.";
    }

    static void logMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error logging message: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Error setting up client IO: " + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            output.println(message);
        }

        @Override
        public void run() {
            try {
                output.println("Enter your username:");
                name = input.readLine();
                broadcast(name + " joined the chat.", this);

                String message;
                while ((message = input.readLine()) != null) {
                    if (message.equalsIgnoreCase("@list")) {
                        output.println("Users online: " + getUserList());
                    } else if (message.startsWith("@")) {
                        int spaceIndex = message.indexOf(' ');
                        if (spaceIndex != -1) {
                            String target = message.substring(1, spaceIndex);
                            String privateMsg = message.substring(spaceIndex + 1);
                            sendPrivate(target, name + ": " + privateMsg);
                        }
                    } else if (message.startsWith("/file ")) {
                        String[] parts = message.split(" ", 2);
                        File file = new File("uploads", parts[1]);
                        if (file.exists()) {
                            output.println("/filetransfer " + file.getName() + " " + file.length());
                            try (FileInputStream fis = new FileInputStream(file);
                                 OutputStream os = socket.getOutputStream()) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                                os.flush();
                                System.out.println("File sent to " + name + ": " + file.getName());
                            }
                        } else {
                            output.println("File not found on server.");
                        }
                    } else if (message.startsWith("/get ")) {
                        String filename = message.substring(5).trim();
                        File file = new File("uploads", filename);
                        if (file.exists()) {
                            output.println("/filetransfer " + file.getName() + " " + file.length());
                            try (FileInputStream fis = new FileInputStream(file);
                                 OutputStream os = socket.getOutputStream()) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                                os.flush();
                                System.out.println("Sent file to " + name + ": " + filename);
                            }
                        } else {
                            output.println("File not found.");
                        }
                    } else if (message.equalsIgnoreCase("bye")) {
                        break;
                    } else {
                        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                        broadcast("[" + time + "] " + name + ": " + message, this);
                    }
                }
            } catch (IOException e) {
                System.out.println(name + " disconnected unexpectedly.");
            } finally {
                removeClient(this);
                broadcast(name + " left the chat.", this);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
