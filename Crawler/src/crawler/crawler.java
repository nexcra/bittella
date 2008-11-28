/**
 *
 * @author jmcamacho
 */
package crawler;

import jBittorrentAPI.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.BufferedWriter;


public class Crawler {

    /**
     * @param args the command line arguments
     */
    public static long count = 0;
    public static int open_con = 0;
    public static Boolean lock = new Boolean(false);
            
    public static void main(String[] args) {


        
        if (args.length < 3 || args.length > 4) {
            System.err.println(
                    "Usage:\r\n" +
                    "java -jar Crawler.jar <save_path> <init_file> <local_ip_addr> [<max_files>]");

            System.exit(1);
        }

        long init_file = Long.parseLong(args[1]);
        long max_files = 100000;
        if (args.length == 4) {
            max_files = Long.parseLong(args[3]);
        }


        while (Crawler.count <= max_files) {

            File q = Crawler.fetchTorrent(init_file);
            if (q != null) {

                try {
                    TorrentProcessor tp = new TorrentProcessor();
                    TorrentFile t = tp.getTorrentFile(tp.parseTorrent(q.getAbsolutePath()));
                    q.delete();
                    byte[] id = Utils.generateID();
                    if(t != null){
                        Connection c = new Connection(id, t, args[0] + "/" + init_file +"_"+t.total_length+".dat", args[2]);
                        c.start();
                        synchronized(Crawler.lock){
                            Crawler.open_con++;
                            System.out.println("Cons= "+Crawler.open_con);                       
                            while(Crawler.open_con >= 10) Crawler.lock.wait(10000);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error while processing torrent file. Please restart the client");
                    //e.printStackTrace();
                    System.gc();
                }
            }
            
            init_file--;
            System.out.println("Current Downloaded IPs from Torrents = "+Crawler.count);
            System.gc();
        }
        
        System.exit(0);
    }
    
    
    private static File fetchTorrent(long o){
        
        String path = "http://www.mininova.org/get/"+o;
        BufferedReader br = null;
        BufferedWriter bw = null;
        File temp = null;
        
        try{
            URL url = new URL(path);
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if(http.getContentType().equals("text/html") || http.getResponseCode()!=200){
                    System.out.println("Torrent Not Available");
                    return null;
            }

            br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String name = String.valueOf(o);
            System.out.println(name);
            temp = File.createTempFile(name, ".tmp");
            temp.deleteOnExit();
            bw = new BufferedWriter(new FileWriter(temp));
            int c = 0;
            
            while(c != -1){
                c = br.read();
                bw.write(c);
            }
            
            br.close();
            bw.flush();
            bw.close();
            return temp;
            
        }catch(IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            try{
                br.close();
                bw.flush();
                bw.close();
                temp.delete();
               }catch(Exception ea){}
            return null;
        }
        
        //return null;
    }
}
