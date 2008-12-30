/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package btj3m;
import jBittorrentAPI.*;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileReader;
/**
 *
 * @author jmcamacho
 */
public class Main {

    private static LinkedHashMap<TorrentFile,String> savepaths;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         try {
            ArrayList<String> files = new ArrayList<String>();
            ArrayList<Boolean> enabled = new ArrayList<Boolean>();
            ArrayList<Integer> initU = new ArrayList<Integer>();
            ArrayList<Long> initT = new ArrayList<Long>();
            ArrayList<String> paths = new ArrayList<String>();
            ArrayList<DwManager> dwm = new ArrayList<DwManager>();
            int init_port = 6881;
            
            TorrentProcessor tp = new TorrentProcessor();

            if(args.length < 1){
                System.err.println(
                        "Incorrect use, please provide the path of the torrent file...\r\n" +
                        "\r\nUsage:\r\n"+
                        "Main <config_file>");

                System.exit(1);
            }
            
            
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(args[0])));
            String line = "";
            
            while(line!=null){
                
                line = lnr.readLine();
                StringTokenizer st = new StringTokenizer(line,"\t");
                
                if(st.countTokens() < 2) continue;
                initT.add(Long.parseLong(st.nextToken()));
                files.add(st.nextToken());
                if(st.countTokens() < 1) paths.add("./");
                else paths.add(st.nextToken());
                if(st.countTokens() < 1) enabled.add(false);
                else enabled.add(Boolean.getBoolean(st.nextToken()));
                if(st.countTokens() < 1) initU.add(4);
                else initU.add(Integer.parseInt(st.nextToken()));
                
                
            }//while
            
            
            for(int i=0;i<files.size();i++){
                TorrentFile t = tp.getTorrentFile(tp.parseTorrent(files.get(i)));
                Main.savepaths.put(t, paths.get(i));
                AvPieces pManager = new AvPieces(t.piece_hash_values_as_binary.size());
                boolean en = enabled.get(i);
                int init_u = initU.get(i);
                DwManager dm = new DwManager(t, Utils.generateID(),pManager,en,init_u);
                dwm.add(dm);
                dm.startListening(init_port, init_port);
                init_port++;
            }
                
            for(int i=0;i<initT.size();i++){
                long wTime = initT.get(i);
                try{
                    Thread.currentThread().wait(wTime);
                }catch(InterruptedException e){}
                 DwManager dm = dwm.get(i);
                 dm.startTrackerUpdate();
                 dm.start();
//                 dm.stopTrackerUpdate();
//                 dm.closeTempFiles();
            } 


        } catch (Exception e) {

            System.out.println("Error while processing torrent file. Please restart the client");
            //e.printStackTrace();
            System.exit(1);
        }
    }

}
