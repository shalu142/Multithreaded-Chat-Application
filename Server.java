// ======= Server.java =======
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
        private DataInputStream dis;
        private DataOutputStream dos;
        String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Error setting up client IO: " + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            try {
                dos.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Failed to send message to " + name);
            }
        }

        @Override
        public void run() {
            try {
                dos.writeUTF("Enter your username:");
                name = dis.readUTF();

                if (name == null || name.trim().isEmpty()) {
                    dos.writeUTF("Invalid username. Connection closing.");
                    socket.close();
                    return;
                }

                synchronized (clients) {
                    for (ClientHandler client : clients) {
                        if (client != this && client.name.equals(name)) {
                            dos.writeUTF("Username already taken. Connection closing.");
                            socket.close();
                            return;
                        }
                    }
                }

                broadcast(name + " joined the chat.", this);

                while (true) {
                    String message = dis.readUTF();

                    if (message.equalsIgnoreCase("@list")) {
                        dos.writeUTF("Users online: " + getUserList());
                    } else if (message.startsWith("@")) {
                        int spaceIndex = message.indexOf(' ');
                        if (spaceIndex != -1) {
                            String target = message.substring(1, spaceIndex);
                            String privateMsg = message.substring(spaceIndex + 1);
                            sendPrivate(target, name + ": " + privateMsg);
                        }
                    } else if (message.startsWith("/get ")) {
                        String filename = message.substring(5).trim();
                        File file = new File("uploads", filename);
                        if (file.exists()) {
                            dos.writeUTF("/filetransfer " + file.getName() + " " + file.length());
                            try (FileInputStream fis = new FileInputStream(file)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    dos.write(buffer, 0, bytesRead);
                                }
                                dos.flush();
                                System.out.println("Sent file to " + name + ": " + filename);
                            }
                        } else {
                            dos.writeUTF("File not found.");
                        }
                    } else if (message.startsWith("/upload ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length < 3) {
                            dos.writeUTF("Usage: /upload <filename> <filesize>");
                            continue;
                        }

                        String filename = parts[1];
                        long fileSize = Long.parseLong(parts[2]);
                        File file = new File("uploads", filename);

                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            byte[] buffer = new byte[4096];
                            long remaining = fileSize;
                            int bytesRead;
                            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                                fos.write(buffer, 0, bytesRead);
                                remaining -= bytesRead;
                            }
                            System.out.println("Received file from " + name + ": " + filename);
                            dos.writeUTF("Upload successful: " + filename);
                            broadcast(name + " uploaded a file: " + filename + " (" + fileSize + " bytes)", this);
                        } catch (IOException e) {
                            dos.writeUTF("Error receiving file.");
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
