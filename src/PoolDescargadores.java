import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolDescargadores implements Runnable{
    protected int puerto = 9000;
    protected boolean detenido = false;
    protected Thread runningThread = null;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);
    protected String dominio;
    public PoolDescargadores(int puerto, String dominio){
        this.puerto = puerto;
        this.dominio = dominio;
    }
    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        Boolean terminar = false;
        this.pool.execute(new Descargador(pool,terminar,dominio));
        //¿Cuándo detener?}
        while(!terminar){}
        System.out.println(terminar);
        this.pool.shutdown();
        System.out.println("El Domoinio especificado se ha terminado de descargar.");
    }
    public static void main(String[] args) {
        System.out.println("Escribe el Dominio que quieres descargar:");
        String dominio = "";
        Scanner entradaEscaner = new Scanner (System.in);
        dominio = entradaEscaner.nextLine ();
        PoolDescargadores des = new PoolDescargadores(9000,dominio);
        new Thread(des,dominio).start();
    }
}