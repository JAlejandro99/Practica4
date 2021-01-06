
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
    protected ArrayList<Boolean> hilosTerminados;
    //Este es solo el url
    protected String dominio;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);
    protected String directorio;
    //Este será el verdadero dominio
    protected String dominio2;
    protected String ubicacion;
    public Descargador(ExecutorService pool, String dominio, String directorio){
        this.pool = pool;
        this.dominio = dominio;
        this.directorio = directorio;
    }
    public void run(){
        try {
            //Leer recurso
            URL recurso = new URL(dominio);
            System.out.println(dominio);
            BufferedReader bf = new BufferedReader(new InputStreamReader(recurso.openStream()));
            String nombre = determinarNombre(dominio);
            //Identificar si ya tenemos la ruta creada
            File archivo = new File(nombre);
            if(!archivo.exists()){
                BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
                String cadena;
                while((cadena = bf.readLine())!=null){
                    bw.write(cadena+"\n");
                }
                bf.close();
                bw.close();
                System.out.println("Archivo creado exitosamente");
                dominio2 = getDominio(dominio);
                enlaces = getURLS(nombre,dominio2,ubicacion);
                for(int i=0;i<enlaces.size();i++){
                    //Correr hilos
                    this.pool.execute(new Descargador(pool,enlaces.get(i),directorio));
                }
                eliminarDominios(nombre);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void eliminarDominios(String nombre){
        String cadena,cadena2="",aux="";
        int auxiliar,auxiliar2=0;
        boolean seguir = true;
        try {
            File temporal = new File("temp.txt");
            FileWriter escribir;
            temporal.createNewFile();
            escribir = new FileWriter(temporal);
            File arch = new File(nombre);
            FileReader f = new FileReader(arch);
            BufferedReader b = new BufferedReader(f);
            while(seguir){
                cadena = b.readLine();
                if(cadena==null)
                    seguir = false;
                else{
                    for(int i=0;i<cadena.length();i++){
                        if(i+7<cadena.length()){
                            if((cadena.substring(i,i+4).equals("href")) || (cadena.substring(i,i+4).equals("src"))){
                                i=i+4;
                                boolean seguir2 = true;
                                while(cadena.charAt(i)!='='){
                                    if(cadena.charAt(i)!=' ')
                                        seguir = false;
                                    i+=1;
                                }
                                i+=1;
                                if(seguir){
                                    while(cadena.charAt(i)!='"'){
                                        if(cadena.charAt(i)!=' ')
                                            seguir = false;
                                        i+=1;
                                    }
                                    i+=1;
                                    if(seguir){
                                        if(cadena2.equals(""))
                                            cadena2 = cadena.substring(0,i-1);
                                        else
                                            cadena2 = cadena2+aux+cadena.substring(auxiliar2,i-1);
                                        aux = "";
                                        while(cadena.charAt(i)!='"'){
                                            aux=aux+String.valueOf(cadena.charAt(i));
                                            i+=1;
                                        }
                                        auxiliar2 = i;
                                        if(aux.startsWith("http")){
                                            int aux3=-1;
                                            for(int j=0;j<aux.length();j++){
                                                if(aux.charAt(i)=='.'){
                                                    aux3 = 0;
                                                }else if(aux.charAt(i)=='/'){
                                                    if(aux3==0)
                                                        aux = aux.substring(i+1);
                                                }
                                            }
                                        }else{
                                            //Agregarle el dominio
                                            if(aux.charAt(0)=='/')
                                                aux = aux.substring(1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    cadena2 = cadena2+aux+cadena.substring(auxiliar2);
                    escribir.write(cadena2+"\n");
                    cadena2 = "";
                }
            }
            b.close();
            arch.delete();
            escribir.close();
            temporal.renameTo(new File(nombre));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getDominio(String dominio){
        int aux=-1;
        String ret="";
        for(int i=0;i<dominio.length();i++){
            if(dominio.charAt(i)=='.'){
                aux = 0;
            }else if(dominio.charAt(i)=='/'){
                if(aux==0){
                    ret = dominio.substring(0,i);
                    i = dominio.length();
                }
            }
        }
        return ret;
    }
    public boolean diferentesDominios(String dom1, String dom2){
        return !getDominio(dom1).equals(getDominio(dom2));
    }
    public ArrayList<String> getURLS(String nombre, String dom, String ub){
        ArrayList<String> ret = new ArrayList<String>();
        String cadena = "";
        try {
            FileReader f = new FileReader(nombre);
            BufferedReader b = new BufferedReader(f);
            while((cadena = b.readLine())!=null){
                for(int i=0;i<cadena.length();i++){
                    if(i+7<cadena.length()){
                        if((cadena.substring(i,i+4).equals("href")) || (cadena.substring(i,i+4).equals("src"))){
                            i=i+4;
                            boolean seguir = true;
                            while(cadena.charAt(i)!='='){
                                if(cadena.charAt(i)!=' ')
                                    seguir = false;
                                i+=1;
                            }
                            i+=1;
                            if(seguir){
                                while(cadena.charAt(i)!='"'){
                                    if(cadena.charAt(i)!=' ')
                                        seguir = false;
                                    i+=1;
                                }
                                i+=1;
                                if(seguir){
                                    String aux = "";
                                    while(cadena.charAt(i)!='"'){
                                        aux=aux+String.valueOf(cadena.charAt(i));
                                        i+=1;
                                    }
                                    if(aux.startsWith("http")){
                                        if(!diferentesDominios(this.dominio,aux))
                                            ret.add(aux);
                                    }else{
                                        //Agregarle el dominio
                                        if(aux.charAt(0)=='/')
                                            aux = dom+aux;
                                        else
                                            aux = dom+ub+"/"+aux;
                                        ret.add(aux);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
            return ret;
        }
        return ret;
    }
    public String determinarNombre(String dominio){
        int aux=-1;
        String nombre="";
        for(int i=0;i<dominio.length();i++){
            if(dominio.charAt(i)=='.'){
                aux = 0;
            }else if(dominio.charAt(i)=='/'){
                if(aux==0){
                    aux = i+1;
                }
            }else if(dominio.charAt(i)=='?'){
                nombre = dominio.substring(aux,i);
                if(nombre.equals("")){
                    nombre="index.html";
                }
                i = dominio.length();
            }
        }
        if(nombre.equals("")){
            if(aux>0)
                nombre = dominio.substring(aux);
        }
        if(nombre.equals("")){
            nombre = "index.html";
        }
        nombre = directorio+"/"+nombre;
        String carpetas="";
        for(int i=nombre.length()-1;i>-1;i--){
            if(nombre.charAt(i)=='/'){
                carpetas = nombre.substring(0,i);
                i=-1;
            }
        }
        if(!carpetas.equals("")){
            File dir = new File(carpetas);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    System.out.println("Directorio creado");
                } else {
                    System.out.println("Error al crear directorio");
                }
            }
        }
        this.ubicacion = carpetas.substring(this.directorio.length());
        if(nombre.charAt(nombre.length()-1)=='/')
            nombre = nombre+"index.html";
        for(int i=nombre.length()-1;i>-1;i--){
            if(nombre.charAt(i)=='.'){
                i = -1;
            }else if(nombre.charAt(i)=='/'){
                nombre = nombre+".html";
                i = -1;
            }
        }
        System.out.println("Nombre: "+nombre);
        return nombre;
    }
}
