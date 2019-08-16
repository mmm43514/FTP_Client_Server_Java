import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;

//Para la transmisión de archivos:
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import static java.lang.Math.*;

//
// Nota: si esta clase extendiera la clase Thread, y el procesamiento lo hiciera el método "run()",
// ¡Podríamos realizar un procesado concurrente! 
//

public class ProcesadorFTP {
    // Socket para enviar/recibir las peticiones/respuestas
    private Socket socketServicio;
    
    private PrintWriter outPrinter = null;
    private BufferedReader inReader = null;
	
    private DataInputStream data_input_s;
    private DataOutputStream data_output_s;
    private FileOutputStream file_output_s;
    private FileInputStream file_input_s;
    
    private Boolean exit = false;
    
    public ProcesadorFTP(Socket socketServicio) {
        this.socketServicio = socketServicio;
    }
    
    public void openControlConnection(){
        try{
            outPrinter = new PrintWriter(socketServicio.getOutputStream(),true);
            inReader = new BufferedReader(new InputStreamReader(socketServicio.getInputStream()));
        }catch (IOException e) {
            System.err.println("Error al obtener los flujos de entrada/salida.");
        }
		
    }
    
    public void openDataConnection(){
        try{
            data_input_s = new DataInputStream(socketServicio.getInputStream());
            data_output_s = new DataOutputStream(socketServicio.getOutputStream());
        }catch (IOException e){
            System.err.println("Error al obtener los flujos de entrada/salida.");
        }
    }
        
    public boolean successfulLogIn(String us, String pass){
        if (us.equals("FR") && pass.equals("FR"))
            return true;
        else
            return false;
    }
    
    public void manageLogIn(){
        String usuario, contrasenia;
        try {
            Boolean logged;
            
            do{
                usuario = inReader.readLine();
                contrasenia = inReader.readLine();
                        
                if (successfulLogIn(usuario,contrasenia) == true){
                    outPrinter.println(0);
                    outPrinter.flush();
                    logged = true;
                }
                else{
                    outPrinter.println(1);
                    outPrinter.flush();
                    logged = false;
                }
            }while(!logged);
			
        } catch (IOException e) {
            System.err.println("Error al obtener los flujos de entrada/salida.");
        }
    }
    
    public void receiveFile(){
        
        int file_size;
        try{
            file_output_s = new FileOutputStream(inReader.readLine());
            file_size = Integer.parseInt(inReader.readLine());
            
            byte[] buffer = new byte[4096];
            int count;
            int total = 0;
            int remaining = file_size;
            
            while( (count = data_input_s.read(buffer, 0, Math.min(buffer.length, remaining))) > 0 && total < file_size){
                remaining -= count;
                file_output_s.write(buffer, 0, count);
                total += count;
                System.out.println("Leido: "+count+"\t Total: "+total);
                System.out.println("total mayor que file size"+total+" > "+file_size+ " Remaining: "+remaining);
            }
             
            file_output_s.close();
            
        } catch(FileNotFoundException e){
            System.err.println("Archivo no encontrado");
        } catch (IOException e) {
            System.err.println("Error al obtener los flujos de entrada/salida.");
	}
    }
    
    public void sendFile(){
       
    	try{
            String file = inReader.readLine();
    		
            File f = new File(file);
            long file_size = f.length();
    		    		
            file_input_s = new FileInputStream(f); //archivo
    		

            outPrinter.println(file_size);
            outPrinter.flush();
            
            file_input_s = new FileInputStream(file);
            BufferedInputStream buffered_file = new BufferedInputStream(file_input_s);
            DataInputStream d_input_stream = new DataInputStream(buffered_file);
            
            byte[] buffer = new byte[(int) file_size];
            
            d_input_stream.readFully(buffer, 0, buffer.length);
            
            data_output_s.write(buffer, 0, buffer.length);
            data_output_s.flush();
            
            file_input_s.close();
            
        } catch (FileNotFoundException e){
            System.err.println("Archivo no encontrado");
        } catch (IOException e){
            System.err.println("Error de entrada/salida al abrir el socket");
        }
    }
	
    public void manageMenuOptions(){
        int option_number = -1;
        String s = null;
        
        try{
            s = inReader.readLine();
            
            option_number = Integer.parseInt(s);
            
        } catch (IOException e) {
            System.err.println("Error al obtener los flujos de entrada/salida.");
        }
        
        if (option_number == 1){
            receiveFile();
        }
        else{
            if(option_number == 2){
            	sendFile();
            }
            else if (option_number == 3)
                exit = true;
        }
    }
    
    
    // Aquí es donde se realiza el procesamiento realmente:
    void procesa(){
        
        openControlConnection();
        
        openDataConnection();
        
        manageLogIn();
        
        while(!exit){
            manageMenuOptions();	
        }
    }
}