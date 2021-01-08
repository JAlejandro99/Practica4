
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
    protected ArrayList<URL> enlaces;
    //Este es solo el url
    protected URL enlace;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);
    protected String directorio;
    protected String ubicacion;
    public Descargador(ExecutorService pool, String enlace, String directorio){
        try {
            this.pool = pool;
            this.enlace = new URL(enlace);
            this.directorio = directorio;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Descargador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void run(){
        try {
            //Leer recurso
            /*System.out.println(enlace.toString());
            System.out.println("Nombre del recurso: "+rutaRecurso(enlace));
            System.out.println("Ruta relativa: "+rutaRelativa(enlace));*/
            BufferedReader bf = new BufferedReader(new InputStreamReader(enlace.openStream()));
            String nombre = (directorio+"/"+rutaRecurso(enlace)).replaceAll("%20"," ");
            //Identificar si ya tenemos la ruta creada
            File archivo = new File(nombre);
            if(!archivo.exists()){
                crearDirectorio(enlace);
                archivo.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
                String cadena;
                while((cadena = bf.readLine())!=null){
                    bw.write(cadena+"\n");
                }
                bf.close();
                bw.close();
                System.out.println("Archivo creado exitosamente");
                if(nombre.endsWith(".html")){
                    enlaces = getURLS(nombre,enlace.getHost(),ubicacion);
                    /*for(int i=0;i<enlaces.size();i++){
                        System.out.println(enlaces.get(i).toString());
                    }*/
                    for(int i=0;i<enlaces.size();i++){
                        //Correr hilos
                        this.pool.execute(new Descargador(pool,enlaces.get(i).toString(),directorio));
                    }
                    eliminarDominios(nombre,enlace.getHost(),ubicacion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void crearDirectorio(URL enlace){
        String ruta = this.directorio+'/'+rutaRelativa(enlace);
        File dir = new File(ruta);
        this.ubicacion = ruta.substring(this.directorio.length());
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directorio creado");
            } else {
                System.out.println("Error al crear directorio");
            }
        }
    }
    public String rutaNueva(URL enlace){
        //System.out.println("url actual: "+enlace.toString());
        String ret = rutaRecurso(enlace);
        int aux = enlace.getFile().indexOf("?");
        if(aux>-1)
            ret = ret+enlace.getFile().substring(aux);
        //System.out.println("Ruta Nueva1: "+ret);
        ret = ret.replaceAll(ubicacion.substring(1),"");
        /*System.out.println("Ubicacion: "+ubicacion);
        System.out.println("Ruta Nueva2: "+ret);*/
        ret = ret.replaceAll("%20"," ");
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
        try {
            b = !dom1.equals(new URL(dom2).getHost());
        } catch (MalformedURLException ex) {
            Logger.getLogger(Descargador.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                                        if(!diferentesDominios(dom,aux))
                                            ret.add(new URL(aux));
                                    }else{
                                        //Agregarle el dominio
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
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                                boolean seguir2 = true;
                                while(cadena.charAt(i)!='='){
                                    if(cadena.charAt(i)!=' ')
                                        seguir2 = false;
                                    i+=1;
                                }
                                i+=1;
                                if(seguir2){
                                    while(cadena.charAt(i)!='"'){
                                        if(cadena.charAt(i)!=' ')
                                            seguir2 = false;
                                        i+=1;
                                    }
                                    i+=1;
                                    if(seguir2){
                                        String aux = "";
                                        while(cadena.charAt(i)!='"'){
                                            aux=aux+String.valueOf(cadena.charAt(i));
                                            i+=1;
                                        }
                                        if(aux.startsWith("http")){
                                            cadena2 = cadena2.replaceAll(aux,rutaNueva(new URL(aux)));
                                        }else{
                                            //Agregarle el dominio
                                            if(aux.charAt(0)=='/'){
                                                cadena2 = cadena2.replaceAll(aux,rutaNueva(new URL("http://"+dom+aux)));
                                            }else if(aux.charAt(0)!='?'){
                                                cadena2 = cadena2.replaceAll(aux,rutaNueva(new URL("http://"+dom+ub+aux)));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
