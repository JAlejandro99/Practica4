import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.*;
import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.util.Iterator;
/**
 *
 * @author axel
 */
public class UCNB {
    public static void main(String[] args){
        try{
           int pto=7;
           InetSocketAddress dst = new InetSocketAddress("127.0.0.1",pto);
           DatagramChannel cl = DatagramChannel.open();
           cl.configureBlocking(false);
           Selector sel = Selector.open();
           cl.register(sel, SelectionKey.OP_WRITE);
           int n=0;
           while(true){
               sel.select();
               Iterator<SelectionKey>it = sel.selectedKeys().iterator();
               while(it.hasNext()){
                   SelectionKey k = (SelectionKey)it.next();
                   it.remove();
                   if(k.isWritable()){
                       Objeto o1 = new Objeto("Esta es una cadena",5.3f,1);
                       ByteArrayOutputStream baos = new ByteArrayOutputStream();
                       ObjectOutputStream oos = new ObjectOutputStream(baos);
                       oos.writeObject(o1);
                       oos.flush();
                       ByteBuffer b = ByteBuffer.wrap(baos.toByteArray());
   
   //                    sc.write(b);

                      DatagramChannel ch = (DatagramChannel)k.channel();
                      ch.send(b, dst);
                      k.cancel();
                      break;
                   }//if
               }//while
               break;
           }//while
           cl.close();
           System.out.println("Termina envio de datagrama");
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main
}
