package simulation.simulator.impl;

import simulation.simulator.Simulator;

import java.io.FileWriter;
import java.io.PrintWriter;

public class SimulatorImpl implements Simulator{

    /* Thread
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
    */

    /* guardarContexto()
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
    */
}
