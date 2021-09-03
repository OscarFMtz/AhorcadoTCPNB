//Cliente ahorcado
import java.nio.channels.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Iterator;

//import javax.swing.JFileChooser;

public class CAhorcadoTCPNB {
    public static void main(String[] args){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));//buffer para dis de E/S estandar        
            
            /*System.out.printf("Escriba la dirección del servidor:");
            String dir = br.readLine();                          //dir="127.0.0.1"
            System.out.printf("\n\nEscriba el puerto:");
            int pto = Integer.parseInt(br.readLine());          //9999;
            */
            String dir="127.0.0.1";
            int pto = 9999;
            int error=0,contR=0,contW=0,tamwords=0,check=0;
            char letra='+';
            char []result=null;
            ByteBuffer buffer = ByteBuffer.allocateDirect(2000), b2=null;
            InetSocketAddress dst = new InetSocketAddress(dir,pto);
            SocketChannel cl = SocketChannel.open();
            cl.configureBlocking(false);
            Selector sel = Selector.open();
            cl.register(sel, SelectionKey.OP_CONNECT);
            cl.connect(dst);

            while(true){
                sel.select();//bloqueo hasta que ocurra un evento connect
                Iterator<SelectionKey>it = sel.selectedKeys().iterator();
                while(it.hasNext()){
                    SelectionKey k = (SelectionKey)it.next();
                    it.remove();
                    if(k.isConnectable()){
                        SocketChannel ch = (SocketChannel)k.channel();
                        if(ch.isConnectionPending()){//Indica si hay una operación de conexión en curso en este canal.
                            System.out.println("Estableciendo conexion con el servidor... espere..");
                            try{
                                ch.finishConnect();//Finaliza el proceso de conexión de un canal de enchufe.
                            }catch(Exception e){
                                e.printStackTrace();
                            }//catch
                        }//if                        
                        ch.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        continue;
                    }else if(k.isReadable()){//evento de lectura
                        SocketChannel ch = (SocketChannel)k.channel();
                        ByteBuffer b = ByteBuffer.allocateDirect(2000);//cantidad en bytes del buffer
                        b.clear();                            
                        
                        if(contR==0){
                            ch.read(b);   
                            b.flip();
                            tamwords = b.getInt();
                            System.out.printf("\n("+tamwords+")");
                            result= new char[tamwords];
                            for(int i=0;i<tamwords; i++)
                                result[i]='_';
                            System.out.println(result);

                            contR++;
                        }else if(contR==1){
                            int n = ch.read(b);
                            n= n/4;
                            b.flip();
                            int[] pos = new int[n];
                            for(int i=0;i<n;i++)
                                pos[i]=b.getInt();
                            if(n==1 && pos[0]==404){
                                error++;
                                System.out.println("\nLLevas "+error+" errores");
                            }
                            if(n>=1 && pos[0]!=404){
                                for(int i=0;i<n;i++){
                                    if(result[pos[i]]!=letra)
                                        check++;
                                    result[pos[i]]=letra;
                                }
                            }
                            if(error>4){
                                System.out.println("\n\n\t Game Over");
                                ch.close();
                                System.exit(0);
                            }
                            if(check==tamwords){
                                System.out.println("\n\n\t You Win");
                                ch.close();
                                System.exit(0);
                            }
                            System.out.println(result);
                        }
                        
                        k.interestOps(SelectionKey.OP_WRITE);
                        continue;

                    }else if(k.isWritable()){//evento de escritura
                       SocketChannel ch = (SocketChannel)k.channel();
               			//Mandar Dificultad, nombre y edad

                        if(contW==0){
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutputStream oos= new ObjectOutputStream(bos);//instanciar    
                        
                            //Escribir datos Dificultad[1,2], Nombre, Edad 
                            System.out.printf("Ingrese el nivel de dificultad(1,2): \n 1-facil \n 2-difícil \n ");
                            int level = Integer.parseInt(br.readLine()); 
                            System.out.printf("\nEscriba el nombre:\n");
                            String nombre = br.readLine();
                            System.out.printf("\nEscriba su edad:\n");
                            int edad = Integer.parseInt(br.readLine());

                            Usuario u = new Usuario(level, nombre,edad);//instanciar objeto a enviar

                            oos.writeObject(u);
                            oos.flush();//me aseguro de que se envie
                            oos.close();
                            byte [] data = bos.toByteArray();
                            b2 = ByteBuffer.wrap(data);
                            ch.write(b2);      
                            System.out.println("Enviando objeto");   
                            contW++;
                        }else if(contW==1){
                            System.out.println("Ingresar letra:");   
                            String letr = br.readLine();
                            letra = letr.charAt(0);
                            letra  = Character.toLowerCase(letra);
                            buffer.clear();
                            buffer.putChar(letra);
                            buffer.flip();
                            ch.write(buffer);

                        }
                        
                        k.interestOps(SelectionKey.OP_READ);
                        continue;
                        // ch.close();
                    }
                }//while   
            }//while
        }catch(Exception e){
            e.printStackTrace();
        }//catch
       
    }//main
}

