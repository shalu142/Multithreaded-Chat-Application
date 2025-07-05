// Client.java
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int MAX_RETRIES = 5;

    public static void main(String[] args) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println(serverReader.readLine()); // Prompt for username
                String name = consoleReader.readLine();
                out.println(name);

                Thread readThread = new Thread(() -> {
                    try {
                        String serverMsg;
                        while ((serverMsg = serverReader.readLine()) != null) {
                            System.out.println(serverMsg);
                        }
                    } catch (IOException e) {
                        System.out.println("Connection closed by server.");
                    }
                });

                readThread.start();

                String userInput;
                while ((userInput = consoleReader.readLine()) != null) {
                    if (userInput.equalsIgnoreCase("bye")) {
                        out.println(name + " has left the chat.");
                        break;
                    }
                    out.println(userInput);
                }

                System.out.println("You have left the chat. Goodbye!");
                break; // Exit loop after disconnect

            } catch (IOException e) {
                attempts++;
                System.out.println("❌ Connection attempt " + attempts + " failed. Retrying in 3 seconds...");
                if (attempts >= MAX_RETRIES) {
                    System.out.println("🔁 Max retries reached. Exiting.");
                    break;
                }
                waitBeforeRetry();
            }
        }
    }

    private static void waitBeforeRetry() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
