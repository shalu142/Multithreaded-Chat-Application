// Client.java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int MAX_RETRIES = 5;

    public static void main(String[] args) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 Scanner scanner = new Scanner(System.in);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println(in.readLine()); // "Enter your username:"
                String username = scanner.nextLine();
                out.println(username);

                // Reader Thread
                Thread reader = new Thread(() -> {
                    try {
                        String serverMsg;
                        InputStream inputStream = socket.getInputStream();
                        while ((serverMsg = in.readLine()) != null) {
                            if (serverMsg.startsWith("/filetransfer")) {
                                String[] parts = serverMsg.split(" ");
                                String filename = parts[1];
                                long size = Long.parseLong(parts[2]);
                                File outFile = new File("downloads", filename);
                                outFile.getParentFile().mkdirs();

                                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                                    byte[] buffer = new byte[4096];
                                    long remaining = size;
                                    while (remaining > 0) {
                                        int bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                                        if (bytesRead == -1) break;
                                        fos.write(buffer, 0, bytesRead);
                                        remaining -= bytesRead;
                                    }
                                    System.out.println("📁 File downloaded: " + outFile.getAbsolutePath());
                                }
                            } else {
                                System.out.println(serverMsg);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Connection lost.");
                    }
                });
                reader.start();

                // Writer Thread
                while (true) {
                    String userInput = scanner.nextLine();
                    if (userInput.equalsIgnoreCase("bye")) {
                        out.println(userInput);
                        break;
                    }
                    out.println(userInput);
                }

                break; // Exit after disconnect
            } catch (IOException e) {
                attempts++;
                System.out.println("Connection failed. Retrying (" + attempts + "/" + MAX_RETRIES + ")...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
