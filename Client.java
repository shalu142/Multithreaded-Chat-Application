import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String msg = dis.readUTF();
                        if (msg.startsWith("/filetransfer")) {
                            String[] parts = msg.split(" ");
                            String fileName = parts[1];
                            long fileSize = Long.parseLong(parts[2]);

                            new File("downloads").mkdirs();
                            File downloadedFile = new File("downloads/" + fileName);
                            try (FileOutputStream fos = new FileOutputStream(downloadedFile)) {
                                byte[] buffer = new byte[4096];
                                long remaining = fileSize;
                                int bytesRead;
                                while (remaining > 0 &&
                                        (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                    remaining -= bytesRead;
                                }
                                System.out.println("File downloaded: " + fileName);

                                // Try to open the file
                                try {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop.getDesktop().open(downloadedFile);
                                    } else {
                                        System.out.println("Auto-open not supported on your system.");
                                    }
                                } catch (IOException e) {
                                    System.out.println("Failed to open the downloaded file: " + e.getMessage());
                                }
                            }
                        } else {
                            System.out.println(msg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            reader.start();

            while (true) {
                String input = scanner.nextLine();

                if (input.startsWith("/upload ")) {
                    String fullPath = input.substring(8).trim();
                    File file = new File(fullPath);
                    if (!file.exists()) {
                        System.out.println("File not found.");
                        continue;
                    }

                    String fileName = file.getName();
                    long fileSize = file.length();
                    dos.writeUTF("/upload " + fileName + " " + fileSize);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                        }
                        dos.flush();
                    } catch (IOException e) {
                        System.out.println("Error uploading file.");
                    }
                } else {
                    dos.writeUTF(input);
                }

                if (input.equalsIgnoreCase("bye")) break;
            }
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }
}
