package simulation.thread;

import java.io.*;

public class Thread {

    private int id;
    private int pc;
    private int[] registros; // 32 enteros
    private int[] instruccion; // cada instruccion de de 4 enteros
    private int quantum;        //inicia con la cantidad de instrucciones y cada vez se le quita 1 hasta llegar a 0
    private long time_start;  //inicio de la ejecucion de un hilo
    private long time_end;

    public Thread( int cantInst, int id){
        this.id= id;
        pc = 0;
        registros = new int[32];
        quantum = cantInst;
        instruccion = new int[4];
        time_start = System.currentTimeMillis();
    }

    public Thread(int id){
        this.id = id;
        registros = new int[32];
        instruccion = new int[4];

        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        try {
            String nomb= "contexto"+id+".txt";
            archivo = new File (nomb);
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea;
            linea=br.readLine();
            String[] vecInt = linea.split(",");
            for(int i=0; i<32;++i)
                registros[i] = Integer.parseInt(vecInt[i]);

            linea=br.readLine();
            vecInt = linea.split(",");
            pc= Integer.parseInt(vecInt[0]);
            quantum = Integer.parseInt(vecInt[1]);
            time_start = Long.parseLong(vecInt[2]);
            System.out.println("pc: "+ pc+"  quan:" + quantum+ "  timeSt: "+ time_start );

        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if( null != fr ){
                    fr.close();
                }
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }

    }

    public void guardarContexto(){
        FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            String nomb= "contexto"+id+".txt";
            fichero = new FileWriter(nomb);
            pw = new PrintWriter(fichero);

            for (int i = 0; i < 32; i++)
                pw.print(registros[i]+",");

            pw.println("");
            pw.println(pc+","+quantum+","+time_start);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fichero)
                    fichero.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }

}
