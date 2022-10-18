package esercizio21;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 *
 * @author ANDREATRAPANI
 */
public class Esercizio21 {

    private DatagramSocket socket;
    private String IP_Address;
    private int UDP_port;

    public Esercizio21(String host, int port) throws SocketException {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(10000); // 10000ms = 10s
        this.IP_Address = host;
        this.UDP_port = port;
    }

    public void close_socket() {
        this.socket.close();
    }

    public void inviaScelta(int scelta) {
        DatagramPacket datagram;
        InetAddress address;
        byte[] buffer;

        try {
            if (socket.isClosed()) {
                throw new IOException();
            }
            address = InetAddress.getByName(IP_Address);
            buffer = ByteBuffer.allocate(4).putInt(scelta).array();

            datagram = new DatagramPacket(buffer, buffer.length, address, UDP_port);
            socket.send(datagram);
        } catch (IOException e) {
        }
    }

    public int inviaVoti(int[] voti) throws IOException {
        DatagramPacket datagram;
        ByteBuffer input, output;
        InetAddress address;
        byte[] buffer;
        int risposta;

        if (socket.isClosed()) {
            throw new IOException();
        }
        output = ByteBuffer.allocate(16);
        output.putInt(voti[0]);
        output.putInt(voti[1]);
        output.putInt(voti[2]);
        output.putInt(voti[3]);
        address = InetAddress.getByName(IP_Address);

        datagram = new DatagramPacket(output.array(), 16, address, UDP_port);
        socket.send(datagram);

        buffer = new byte[4];
        datagram = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagram);
        if (datagram.getAddress().equals(address) && datagram.getPort() == UDP_port) {
            input = ByteBuffer.wrap(datagram.getData());
            risposta = input.getInt();
        } else {
            throw new SocketTimeoutException();
        }
        return risposta;
    }

    public double richiediVotoMedio(int numero) throws IOException {
        DatagramPacket datagram;
        ByteBuffer input;
        InetAddress address;
        byte[] buffer;
        double risposta;

        if (socket.isClosed()) {
            throw new IOException();
        }
        address = InetAddress.getByName(IP_Address);
        buffer = ByteBuffer.allocate(4).putInt(numero).array();

        datagram = new DatagramPacket(buffer, buffer.length, address, UDP_port);
        socket.send(datagram);
        
        buffer = new byte[8];
        datagram = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagram);
        if (datagram.getAddress().equals(address) || datagram.getPort() == UDP_port) {
            input = ByteBuffer.wrap(datagram.getData());
            risposta = input.getDouble();
        } else {
            throw new SocketTimeoutException();
        } 
        return risposta;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        Esercizio21 client;

        try {
            client = new Esercizio21("127.0.0.1", 12345);
            int[] voti = new int[4];
            int count = 1;
            Scanner input = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Vota\n2. Voto medio\n3. Esci\n");
                System.out.print("Effettuare una scelta: ");
                int scelta = input.nextInt();
                client.inviaScelta(scelta);

                switch (scelta) {
                    case 1:
                        for (int i = 0; i < voti.length; i++) {
                            System.out.print("Inserire il voto del " + (i + 1) + "° caffe': ");
                            do {
                                voti[i] = input.nextInt();
                                if (voti[i] < 1 || voti[i] > 10) {
                                    System.out.println("Errore! Inserire un voto tra 1 e 10.");
                                    System.out.print("Ritenta: ");
                                }
                            } while (voti[i] < 1 || voti[i] > 10);
                        }
                        int risposta = client.inviaVoti(voti);
                        if (risposta == 1) {
                            System.out.println("\nvoti registrati");
                        } else {
                            System.out.println("\nMassimo 10 persone!");
                        }
                        break;
                    case 2:
                        int numero = 0;
                        for (int i = 0; i < 4; i++) {
                            System.out.print((i + 1) + "° caffe' - ");
                        }
                        while (numero < 1 || numero > 4) {
                            System.out.print("\nDi quale caffe' vuoi sapere il voto medio (1-4): ");
                            numero = input.nextInt();
                        }
                        System.out.println(client.richiediVotoMedio(numero - 1));
                        break;
                    case 3:
                        client.close_socket();
                        System.exit(0);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } catch (SocketException ex) {
            System.err.println("Errore creazione socket!");
        }
    }

}
