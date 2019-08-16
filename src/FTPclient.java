import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
//Para la transmisión de archivos:
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static java.lang.Math.*;


public class FTPclient {
    static String host; // Dirección IP del Servidor
    static int port; 
    static Socket socketServicio = null; // Socket para la conexión TCP
    
    static PrintWriter outPrinter = null; // Servirá para enviar Strings al servidor
    static BufferedReader inReader = null; // Servirá para recibir Strings del servidor
    
    static DataOutputStream data_output_s;
    static DataInputStream data_input_s;
    static FileInputStream file_input_s;
    static FileOutputStream file_output_s;
        
    public static void openControlConnection(String s, int p){
        host = s;
        port = p;
        
        try{
            socketServicio = new Socket(host, port);
            outPrinter = new PrintWriter(socketServicio.getOutputStream(), true);
            inReader = new BufferedReader(new InputStreamReader(socketServicio.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Error: Nombre de host no encontrado.");
        } catch (IOException e) {
            System.err.println("Error de entrada/salida al abrir el socket.");
        }      
    }
    
    public static void openDataConnection(){
        try{
            data_input_s = new DataInputStream(socketServicio.getInputStream());
            data_output_s = new DataOutputStream(socketServicio.getOutputStream());
        }catch (IOException e){
            System.err.println("Error al obtener los flujos de entrada/salida.");
        }
    }
        
    public static String[] getUserPass(){
        Scanner sc = new Scanner(System.in);
        String[] ret = new String[2];
         
        System.out.println("Usuario: ");
        ret[0] = sc.nextLine();
        
        System.out.println("Contraseña: ");
        ret[1] = sc.nextLine();
        
        return ret;
    }
      
    public static void logIn(){
        int estado = -1;
                    
        do{
            String[] user_pass = getUserPass();
             
            outPrinter.println(user_pass[0]);
            outPrinter.flush();
            outPrinter.println(user_pass[1]);
            outPrinter.flush();
                       
            try{
                estado = Integer.parseInt(inReader.readLine());
            } catch (IOException e) {
                System.err.println("Error de entrada/salida al abrir el socket.");
            }
                      
            if (estado == 0)
                System.out.println("Logged in");
            else if (estado == 1)
                System.err.println("Login error");
                        
        }while(estado == 1);
    }
    
    public static void sendFile(String file, String directorio){
        
    	try{
            File f = new File(file);
            long file_size = f.length();
            //envío directorio destino con nombre archivo
            outPrinter.println(directorio);
            outPrinter.flush();
            //envío tamaño archivo
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
    
    public static void getFile(String file, String directorio){
    	int file_size;
        try{
            outPrinter.println(file); //mandamos la dirección del archivo a descargar
            outPrinter.flush();
            file_size = Integer.parseInt(inReader.readLine());
        	
            file_output_s = new FileOutputStream(directorio); //aquí se guardará la descarga        
            
            byte[] buffer = new byte[4096];
            int count;
            int total = 0;
            int remaining  = file_size;
            
            while( (count = data_input_s.read(buffer, 0, Math.min(buffer.length, remaining))) > 0 && total < file_size){
                if (count != -1){
                remaining -= count;
                file_output_s.write(buffer, 0, count);
                file_output_s.flush();
                total += count;
                }
            }
                 
            file_output_s.close();
        } catch(FileNotFoundException e){
            System.err.println("Archivo no encontrado");
        } catch (IOException e) {
            System.err.println("Error al obtener los flujos de entrada/salida.");
		}
    }
    
    public static void menu(){
                    
        System.out.println("  COMANDOS  ");
        System.out.println("\t put file_path");
        System.out.println("\t get file_path");
        System.out.println("\t logout");
        
        Scanner sc;
        String opcion, directorio;
        String[] com_path;
        
        while (true){    
            sc = new Scanner(System.in);
            opcion = sc.nextLine();
            
            if (opcion.equals("logout")){
                outPrinter.println(3);
                outPrinter.flush();
                
                System.exit(0);
            }
            
            com_path = opcion.split(" ",2);
            
            if(com_path[0].equals("put")){
                outPrinter.println(1);
                outPrinter.flush();
                
                System.out.println("Introduzca \"directorio_destino/nombre_nuevo_archivo\": ");
                directorio = sc.nextLine();
                
                sendFile(com_path[1], directorio);
                System.out.println("Archivo enviado");
            }
            else{ 
                if(com_path[0].equals("get")){
                    outPrinter.println(2);
                    outPrinter.flush();
                    
                    System.out.println("Introduzca \"directorio_destino/nombre_archivo_descargado\": ");
                    directorio = sc.nextLine();
                    
                    getFile(com_path[1], directorio);
                    System.out.println("Archivo descargado");
                }
                else{
                    System.err.println("Comando incorrecto.");
                }
            }
        }
    }
        
    public static void main(String[] args) {
                
	if(args.length != 1)
        {
            System.out.println("Ha de añadirse la dirección del servidor como argumento");
            System.exit(0);
        }
        
    	openControlConnection(args[0], 8989);
        
        openDataConnection();
        
        logIn();
        
        menu();
	
        try{
            socketServicio.close();
        } catch (IOException e) {
            System.err.println("Error de entrada/salida al abrir el socket.");
        }
    }
}