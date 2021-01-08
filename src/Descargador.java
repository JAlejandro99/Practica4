
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Descargador implements Runnable{
    protected ArrayList<URL> enlaces;
    //Este es solo el url
    protected URL enlace;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);
    protected String directorio;
    protected String ubicacion;
    protected String posicion;
    public Descargador(ExecutorService pool, String enlace, String directorio, String posicion){
        try {
            this.pool = pool;
            this.enlace = new URL(enlace);
            this.directorio = directorio;
            this.posicion = posicion;
        } catch (MalformedURLException ex) {}
    }
    public void run(){
        try {
            //Leer recurso
            String nombre = (directorio+"/"+rutaRecurso(enlace)).replaceAll("%20"," ");
            //Identificar si ya tenemos la ruta creada
            File archivo = new File(nombre);
            if(!archivo.exists()){
                crearDirectorio(enlace);
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
                //System.out.println("Archivo creado exitosamente");
                if(nombre.endsWith(".html") || nombre.endsWith(".php")){
                    enlaces = getURLS(nombre,enlace.getHost(),ubicacion);
                    for(int i=0;i<enlaces.size();i++){
                        //Correr hilos
                        this.pool.execute(new Descargador(pool,enlaces.get(i).toString(),directorio,posicion));
                    }
                    eliminarDominios(nombre,enlace.getHost(),ubicacion);
                }
            }
        } catch (IOException e) {}
    }
    public void crearDirectorio(URL enlace){
        String ruta = this.directorio+'/'+rutaRelativa(enlace);
        File dir = new File(ruta);
        this.ubicacion = ruta.substring(this.directorio.length());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    public String rutaNueva(URL enlace){
        //System.out.println("url actual: "+enlace.toString());
        String ret = rutaRecurso(enlace);
        int aux = enlace.getFile().indexOf("?");
        if(aux>-1)
            ret = ret+enlace.getFile().substring(aux);
        //System.out.println("Ruta Nueva1: "+ret);
        //ret = ret.replaceAll(ubicacion.substring(1),"");
        ret = ret.replaceAll("%20"," ");
        ret = posicion+"/"+directorio+"/"+ret;
        return ret;
    }
    public String rutaRecurso(URL enlace){
        String aux=enlace.getPath();
        boolean agregarExtension = true;
        for(int i=aux.length()-1;i>-1;i--){
            if(aux.charAt(i)=='.')
                agregarExtension = false;
            else if(aux.charAt(i)=='/'){
                i = -1;
            }
        }
        if(aux.equals("")){
            aux = "index.html";
        }else if(aux.charAt(aux.length()-1)=='/'){
            aux = aux+"index.html";
        }else if(agregarExtension){
            aux = aux+".html";
        }
        if(aux.charAt(0)=='/')
            aux = aux.substring(1,aux.length());
        return aux;
    }
    public String rutaRelativa(URL enlace){
        String aux = enlace.getPath();
        int i,aux2=aux.length();
        for(i=aux2-1;i>-1;i--){
            if(aux.charAt(i)=='/'){
                aux2 = i+1;
                i = -1;
            }
        }
        //Si queremos la primer diagonal 1 debbería de ser 0
        if(aux.length()>1)
            aux = aux.substring(1,aux2);
        if(aux.length()==1){
            if(aux.charAt(0)=='/')
                aux = "";
        }
        return aux;
    }
    public boolean diferentesDominios(String dom1, String dom2){
        boolean b = true;
        String aux1,aux2;
        try {
            aux1 = dom1.replaceFirst("www.","");
            aux2 = new URL(dom2).getHost();
            aux2 = aux2.replaceFirst("www.","");
            b = !aux1.equals(aux2);
        } catch (IOException ex) {}
        return b;
    }
    public ArrayList<URL> getURLS(String nombre, String dom, String ub){
        ArrayList<URL> ret = new ArrayList<URL>();
        String cadena = "";
        try {
            FileReader f = new FileReader(nombre);
            BufferedReader b = new BufferedReader(f);
            while((cadena = b.readLine())!=null){
                for(int i=0;i<cadena.length();i++){
                    if(i+7<cadena.length()){
                        if((cadena.substring(i,i+4).equals("href")) || (cadena.substring(i,i+3).equals("src"))){
                            if(cadena.substring(i,i+3).equals("src"))
                                i=i+3;
                            else
                                i=i+4;
                            boolean seguir = true, seguir2 = true;
                            while(seguir2){
                                if(cadena.charAt(i)=='=')
                                    seguir2 = false;
                                else if(cadena.charAt(i)!=' '){
                                    seguir = false;
                                    seguir2 = false;
                                }
                                if(i>=cadena.length()){
                                    seguir = false;
                                    seguir2 = false;
                                }
                                i+=1;
                            }
                            //i+=1;
                            if(seguir){
                                seguir2 = true;
                                while(seguir2){
                                    if(cadena.charAt(i)=='"')
                                        seguir2 = false;
                                    else if(cadena.charAt(i)!=' '){
                                        seguir = false;
                                        seguir2 = false;
                                    }
                                    if(i>=cadena.length()){
                                        seguir = false;
                                        seguir2 = false;
                                    }
                                    i+=1;
                                }
                                //i+=1;
                                if(seguir){
                                    String aux = "";
                                    seguir = true;
                                    seguir2 = true;
                                    while(seguir2){
                                        if(cadena.charAt(i)=='"')
                                            seguir2 = false;
                                        else
                                            aux=aux+String.valueOf(cadena.charAt(i));
                                        if(i>=cadena.length()){
                                            seguir = false;
                                            seguir2 = false;
                                        }
                                        i+=1;
                                    }
                                    if(seguir){
                                        if(aux.startsWith("http")){
                                            if(!diferentesDominios(dom,aux))
                                                ret.add(new URL(aux));
                                        }else{
                                            //Agregarle el dominio
                                            if(!aux.equals("")){
                                                if(aux.charAt(0)=='/')
                                                    ret.add(new URL("http://"+dom+aux));
                                                else if(aux.charAt(0)!='?')
                                                    ret.add(new URL("http://"+dom+ub+aux));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            b.close();
        } catch (IOException e) {
            return ret;
        }
        return ret;
    }
    public void eliminarDominios(String nombre, String dom, String ub){
        String cadena,cadena2="";
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
                    cadena2 = cadena;
                    for(int i=0;i<cadena.length();i++){
                        if(i+7<cadena.length()){
                            if((cadena.substring(i,i+4).equals("href")) || (cadena.substring(i,i+3).equals("src"))){
                                if(cadena.substring(i,i+3).equals("src"))
                                    i=i+3;
                                else
                                    i=i+4;
                                boolean seguir2 = true,seguir3 = true;
                                while(seguir3){
                                    if(cadena.charAt(i)=='=')
                                        seguir3 = false;
                                    else if(cadena.charAt(i)!=' '){
                                        seguir2 = false;
                                        seguir3 = false;
                                    }
                                    if(i>=cadena.length()){
                                        seguir2 = false;
                                        seguir3 = false;
                                    }
                                    i+=1;
                                }
                                //i+=1;
                                if(seguir2){
                                    seguir3 = true;
                                    while(seguir3){
                                        if(cadena.charAt(i)=='"')
                                            seguir3 = false;
                                        else if(cadena.charAt(i)!=' '){
                                            seguir2 = false;
                                            seguir3 = false;
                                        }
                                        if(i>=cadena.length()){
                                            seguir2 = false;
                                            seguir3 = false;
                                        }
                                        i+=1;
                                    }
                                    //i+=1;
                                    if(seguir2){
                                        String aux = "";
                                        seguir2 = true;
                                        seguir3 = true;
                                        while(seguir3){
                                            if(cadena.charAt(i)=='"')
                                                seguir3 = false;
                                            else
                                                aux=aux+String.valueOf(cadena.charAt(i));
                                            if(i>=cadena.length()){
                                                seguir2 = false;
                                                seguir3 = false;
                                            }
                                            i+=1;
                                        }
                                        if(seguir2){
                                            if(aux.startsWith("http")){
                                                if(!diferentesDominios(dom,aux))
                                                    cadena2 = cadena2.replaceFirst(aux,rutaNueva(new URL(aux)));
                                            }else{
                                                //Agregarle el dominio
                                                if(!aux.equals("")){
                                                    if(aux.charAt(0)=='/'){
                                                        cadena2 = cadena2.replaceFirst(aux,rutaNueva(new URL("http://"+dom+aux)));
                                                    }else if(aux.charAt(0)!='?'){
                                                        cadena2 = cadena2.replaceFirst(aux,rutaNueva(new URL("http://"+dom+ub+aux)));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    escribir.write(cadena2+"\n");
                    cadena2 = "";
                }
            }
            b.close();
            arch.delete();
            escribir.close();
            temporal.renameTo(new File(nombre));
        } catch (IOException e) {}
    }
}
