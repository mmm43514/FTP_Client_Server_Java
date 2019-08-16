/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.Socket;
/**
 *
 * @author mx3
 */
public class ThreadProcesadorFTP extends Thread{
    Socket s;
    ThreadProcesadorFTP(Socket s){
        this.s = s;
    }
    @Override
    public void run(){
        ProcesadorFTP procesador = new ProcesadorFTP(this.s);
	procesador.procesa();
    }
}