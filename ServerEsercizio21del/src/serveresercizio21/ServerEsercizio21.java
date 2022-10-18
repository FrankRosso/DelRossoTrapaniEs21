package serveresercizio21;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author FRANCESCODELROSSO
 */
public class ServerEsercizio21 implements Runnable {

    private DatagramSocket socket;

    public ServerEsercizio21(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(10000); // 10000 ms = 10s
    }

    public int[] leggiVoti() {
        int[] voti = new int[4];
        byte[] buffer = new byte[16];
        ByteBuffer data;
        DatagramPacket request;

        try {
            request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);

            data = ByteBuffer.wrap(buffer, 0, 16);
            for (int i = 0; i < voti.length; i++) {
                voti[i] = data.getInt();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerEsercizio21.class.getName()).log(Level.SEVERE, null, ex);
        }
        return voti;
    }

    public int salvaVoti() throws IOException {
        int[] voti = leggiVoti();
        FileWriter file = new FileWriter("voti.csv", true);
        
        for (int i = 0; i < voti.length; i++) {
            file.write(String.valueOf(voti[i]));
            file.write(",");
        }
        file.write("\n");
        file.close();
        return 1;
    }
    
    public double votoMedio(int codice) throws FileNotFoundException, IOException {
        String line;
        int voti = 0;
        int numeroVoti = 0;
        double votoMedio;
        BufferedReader br = new BufferedReader(new FileReader("voti.csv"));
        
        while ((line = br.readLine()) != null) {
            String[] b = line.split(",");
            voti += Integer.parseInt(b[codice]);
            numeroVoti++;
        }
        votoMedio = voti / (double) numeroVoti;
        return votoMedio;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4];
        ByteBuffer data;
        DatagramPacket answer, request;
        int scelta = 0;
        int count = 1;
        
        while (!Thread.interrupted()) {
            try {
                request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                scelta = ByteBuffer.wrap(buffer, 0, 4).getInt();

                switch (scelta) {
                    case 1:
                        if (count > 10) {
                            data = ByteBuffer.allocate(4);
                            data.putInt(-1);
                            answer = new DatagramPacket(data.array(), 4, request.getAddress(), request.getPort());
                            socket.send(answer);
                            break;
                        }
                        data = ByteBuffer.allocate(4);
                        data.putInt(salvaVoti());
                        answer = new DatagramPacket(data.array(), 4, request.getAddress(), request.getPort());
                        socket.send(answer);
                        count++;
                        break;
                    case 2:
                        socket.receive(request);
                        int codice = ByteBuffer.wrap(buffer, 0, 4).getInt();
                        double risposta = votoMedio(codice);
                        buffer = new byte[8];
                        data = ByteBuffer.wrap(buffer, 0, 8);
                        data.putDouble(risposta);
                        answer = new DatagramPacket(data.array(), 8, request.getAddress(), request.getPort());
                        socket.send(answer);
                        break;
                    case 3:
                        System.exit(0);
                        break;
                    default:
                        throw new AssertionError();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerEsercizio21.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            ServerEsercizio21 server = new ServerEsercizio21(12345);
            Thread thrServer = new Thread(server);
            thrServer.start();
            //thrServer.interrupt();
            thrServer.join();
        } catch (SocketException ex) {
            Logger.getLogger(ServerEsercizio21.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerEsercizio21.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
