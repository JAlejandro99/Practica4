/**
*Materia: Aplicaciones para Comunicaciones en Red
*Autores:
*   Domínguez Reyes Jesús Alejandro
*   Pérez Federico José Joel
*Fecha: 13 de Enero de 2020
*Práctica 4: Descargador de Dominios
**/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PoolDescargadores implements Runnable{
    protected int puerto = 9000;
    protected boolean detenido = false;
    protected Thread runningThread = null;
    protected ExecutorService pool = Executors.newFixedThreadPool(50);
    protected String dominio;
    protected String directorio;
    protected String posicion;
    public PoolDescargadores(int puerto, String dominio, String directorio, String posicion){
        this.puerto = puerto;
        this.dominio = dominio;
        this.directorio = directorio;
        this.posicion = posicion;
    }
    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        //Boolean terminar = false;
        if(dominio.endsWith("/"))
            pedirFaviconico(dominio,directorio);
        else
            pedirFaviconico(dominio+"/",directorio);
        this.pool.execute(new Descargador(pool,dominio,directorio,posicion));
        //¿Cuándo detener?}
        //while(!terminar){
            //if
        //}
        //this.pool.shutdown();
        //System.out.println("El Dominio especificado se ha terminado de descargar.");
    }
    public void pedirFaviconico(String dominio, String directorio){
        try {
            URL enlace = new URL(dominio+"favicon.ico");
            File archivo = new File(directorio+"/favicon.ico");
            File dir = new File(directorio);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            archivo.createNewFile();
            URLConnection conn = enlace.openConnection();
            conn.connect();
            InputStream in = conn.getInputStream();
            OutputStream out = new FileOutputStream(archivo);
            int b = 0;
            while (b != -1) {
                b = in.read();
                if (b != -1)
                    out.write(b);
            }
            out.close();
            in.close();
        } catch (IOException ex) {}
    }
    public static void main(String[] args) {
        System.out.println("Escribe el Dominio que quieres descargar:");
        String dominio = "";
        Scanner entradaEscaner = new Scanner (System.in);
        dominio = entradaEscaner.nextLine ();
        System.out.println("Escribe la carpeta en que lo quieres guardar");
        String carpeta = "";
        entradaEscaner = new Scanner (System.in);
        carpeta = entradaEscaner.nextLine ();
        File fichero = new File("");
        String posicion = fichero.getAbsolutePath();
        posicion = posicion.replace('\\','/');
        PoolDescargadores des = new PoolDescargadores(9000,dominio,carpeta,posicion);
        new Thread(des,dominio).start();
    }
}