import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPserver{
        
    public static void main(String[] args) {
	
        // Puerto de escucha
        int port = 8989;
        
        //Creamos los Sockets
        ServerSocket socketServidor;
        Socket socketServicio;
		
        try {
        	socketServidor = new ServerSocket(port);
		    
            do {
            	System.out.println("Esperando conexión...");
                socketServicio = socketServidor.accept();
                System.out.println("Conexión establecida");
                
                ThreadProcesadorFTP h = new ThreadProcesadorFTP(socketServicio);
                h.start();
				
            } while (true);
			
        } catch (IOException e) {
            System.err.println("Error al escuchar en el puerto "+port);
        }
    }
}