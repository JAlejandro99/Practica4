
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Descargador implements Runnable{
    protected ArrayList<String> enlaces;
    protected Boolean terminar;
    protected ArrayList<Boolean> hilosTerminados;
    protected String dominio;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);
    public Descargador(ExecutorService pool, boolean terminar, String dominio){
        this.pool = pool;
        this.terminar = terminar;
        this.dominio = dominio;
    }
    public void run(){
        try {
            //Leer recurso
            URL recurso = new URL(dominio);
            System.out.println(dominio);
            BufferedReader bf = new BufferedReader(new InputStreamReader(recurso.openStream()));
            File archivo = new File("descargado.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
            String cadena;
            while((cadena = bf.readLine())!=null){
                bw.write(cadena);
            }
            bf.close();
            bw.close();
            System.out.println("Archivo creado exitosamente");
            terminar = true;
            //Correr hilos
            //this.pool.execute(new Descargador(pool,terminar,dominio));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
