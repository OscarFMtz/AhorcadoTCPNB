//Servidor Ahorcado
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import javax.swing.JFileChooser;

public class SAhorcadoTCPNB {
    public static void main(String[] args){
        try{           
            int pto=9999;
            int error=0,contW=0,contR=0;
            char letra='+';
            ByteBuffer b1=null;
            ByteBuffer buffer = ByteBuffer.allocateDirect(2000);
            boolean flag = false;
            String[] listwords = new String[10];
            Usuario u=null;
            String selectW=null;

            //                      Elegir archivo para extraer palabras
            JFileChooser jf = new JFileChooser();
            int r = jf.showOpenDialog(null);//
            if(r==JFileChooser.APPROVE_OPTION){                
                String cadena;
                File f = jf.getSelectedFile(); 
                String archivo = f.getAbsolutePath(); //Dirección
                FileReader fr = new FileReader(archivo);
                BufferedReader b = new BufferedReader(fr);
                int cont=0;

                while((cadena = b.readLine())!=null) {
                    listwords[cont]= cadena;
                    //System.out.println(listwords[cont] +":"+listwords[cont].length());
                    cont++;
                }
                //System.out.println(listwords.length);
                b.close();
            }

            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(pto));
            
            Selector selctr = Selector.open();
            ssc.register(selctr, SelectionKey.OP_ACCEPT);

            while(true){
                selctr.select();
                Iterator<SelectionKey>iter = selctr.selectedKeys().iterator();

                while(iter.hasNext()){	//while para recorrer eventos por realizar.                 
                    SelectionKey skey = (SelectionKey)iter.next();
                    iter.remove();
                    
                    if(skey.isAcceptable()){//evento de conexión
                        SocketChannel schanel = ssc.accept(); 
                        schanel.configureBlocking(false);
                        System.out.println("Cliente conectado desde "+schanel.socket().getInetAddress()+":"+ schanel.socket().getPort());
                        schanel.register(selctr,SelectionKey.OP_READ | SelectionKey.OP_WRITE);                       
                        continue;

                    }else if(skey.isReadable()){//evento de lectura
                        try{
                            ByteBuffer b = ByteBuffer.allocate(2000);//cantidad en bytes del buffer
                            b.clear();                            
                            //ver en cual de los canales conectados ocurrio el evento.
                            SocketChannel sch = (SocketChannel)skey.channel();   
                                                                                   
                            if(contR==0){
                                sch.read(b); 
                                ByteArrayInputStream bais = new  ByteArrayInputStream (b.array());
                                ObjectInputStream ois = new ObjectInputStream(bais); 
                                u = (Usuario)ois.readObject();//case para adaptarlo al tipo de dato que voy a recibir.
                                System.out.println("Difficultad = "+u.getDif());
                                System.out.println("Nombre = "+u.getNombre());      
                                System.out.println("Edad = "+u.getEdad());

                                //Seleccion de palabra en base a la dificultad
                                //flag = false;
                                int numero;
                                while(flag==false){
                                    numero = (int)(Math.random()*10); //numero entre 0 y 9 que son 10 elementos de listwords
                                    if(u.getDif()==1){
                                        if(listwords[numero].length()<6){
                                            flag=true;
                                            selectW=listwords[numero];
                                        }
                                    }
                                    if(u.getDif()==2){
                                        if(listwords[numero].length()>5){
                                            flag=true;
                                            selectW=listwords[numero];
                                        }   
                                    }
                                }//Fin seleccion 
                                contR++;
                            }else if(contR==1){
                                //realizamos la lectura
                                sch.read(b);
                                b.flip();
                                letra = b.getChar();
                                System.out.print(" \n Letra recibida: "+letra);
                            }

                            skey.interestOps(SelectionKey.OP_WRITE);
                        //    sch.close();
                        }catch(IOException io){}
                        continue;
                    }else if(skey.isWritable()){ //evento de escritura
                        //System.out.println("(write)");
                        try{
                            SocketChannel ch = (SocketChannel)skey.channel();
                            if(contW==0){
                                contW++;    
                            }else if(contW==1){                                
                                System.out.println("Palabra: "+selectW);
                                buffer.clear();
                                buffer.putInt(selectW.length());
                                buffer.flip();
                                ch.write(buffer);
                                contW++;

                            }else if(contW==2){
                                buffer.clear();
                                char[] charA_selectW = selectW.toCharArray(); 
                                int npocisiones=0;
                                System.out.print("\t i=[");
                                for(int i=0;i<selectW.length();i++){
                                    //toUpperCase()
                                    char x  = Character.toLowerCase(charA_selectW[i]);
                                    if(x==letra){
                                        System.out.printf(" "+i);
                                        buffer.putInt(i);
                                        npocisiones++;
                                    }
                                }

                                System.out.print("]");
                                if(npocisiones==0){     //error= Letra diferentes
                                    error++;
                                    buffer.putInt(404);
                                    buffer.flip();
                                    ch.write(buffer);
                                }else{
                                    buffer.flip();
                                    ch.write(buffer);
                                }
                                if(error>4){
                                    ch.close();
                                    System.exit(0);
                                }

                            }   
                        }catch(IOException io){}                        
                        skey.interestOps(SelectionKey.OP_READ);
                        continue;
                    }//if
                }//while
            }//while
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main
}