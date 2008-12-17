/**
 *
 * @author jmcamacho
 */
package crawler2;

import jBittorrentAPI.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;

public class Crawler {

    /**
     * @param args the command line arguments
     */
    public static long count = 0;
    public static int open_con = 0;
    public static String base_url = "http://www.mininova.org/get/";
    public static File meta;

    public static void main(String[] args) {

        long init_file = 0;
        String local_addr = "192.168.1.10";
        HashMap<String, String> param = new HashMap<String, String>();
        long max_files = Long.MAX_VALUE;
        long last_file = 0;
        String output_folder = "./";
        boolean meta_file = false;

        if (args.length < 1 || args.length > 11 || args.length % 2 != 1) {
            Crawler.errMessage();
            System.exit(1);
        }

        for (int i = 0; i < args.length - 1; i += 2) {
            param.put(args[i], args[i + 1]);

        }

        if (!param.containsKey(args[args.length - 1])) {
            init_file = Long.parseLong(args[args.length - 1]);
        } else {
            Crawler.errMessage();
        }

        if (param.containsKey("-p")) {
            Crawler.base_url = param.get("-p");
        }
        if (param.containsKey("-d")) {
            output_folder = param.get("-d");
        }
        if (param.containsKey("-l")) {
            last_file = Long.parseLong(param.get("-l"));
        }
        if (param.containsKey("-m")) {
            max_files = Long.parseLong(param.get("-m"));
        }
        if (param.containsKey("-e")) {
            Crawler.meta = new File(param.get("-e"));
            meta_file = true;
        }
        if (param.containsKey("-a")) {
            local_addr = param.get("-a");
        }

        while (Crawler.count <= max_files && init_file > last_file) {

            File q = Crawler.fetchTorrent(init_file);

            if (q != null) {

                TorrentProcessor tp = new TorrentProcessor();
                TorrentFile t = tp.getTorrentFile(tp.parseTorrent(q.getAbsolutePath()));
                q.delete();
                if (t != null) {

                    byte[] id = Utils.generateID();
                    String length = Crawler.checkLength(t.total_length);
                    Connection c = null;

                    if (!meta_file) {
                        c = new Connection(id, t, output_folder + init_file + "_" + length + ".dat", local_addr);
                    } else {
                        c = new Connection2(id, t, output_folder + init_file + ".dat", local_addr);
                    }
                    c.start();

                    try {
                        synchronized (Crawler.class) {
                            Crawler.openCon();
                            while (Crawler.open_con >= 10) {
                                Crawler.class.wait(10000);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error Opening a Connection");
                        //e.printStackTrace();
                        System.gc();
                    }//catch
                }//if t
            }//if q

            init_file--;
            System.gc();
        }//while

        System.exit(0);
    }

    private static void errMessage() {
        System.err.println(
                "Usage:" +
                "\tjava -jar Crawler2.0.jar [OPTIONS] <init_file>");
        System.err.print("\n");
        System.err.println("  init_file\t Initial Torrent file ID.");
        System.err.println("\t\t The program continues running through the torrent list decreasing this value.");
        System.err.println("<<OPTIONS>>");
        System.err.println("  -p <url>\t Base URL of the torrent directory. Default: http://www.mininova.org/get/");
        System.err.println("  -d <folder>\t Output folder for generated files. Default: Current Folder");
        System.err.println("  -l <id>\t Last Torrent file ID to be fetched and processed");
        System.err.println("  -m <#>\t Maximum amount of Torrent files to be processed");
        System.err.println("\t\t Program will exit when this amount of torrents have been succesfully processed");
        System.err.println("  -e <folder>\t Extended torrent information");
        System.err.println("\t\t In addition to the peer list, meta info per Torrent file is stored in the given file.");
        System.err.println("  -a <ip_addr>\t Specify local \"public\" IP address");
        System.err.println("\t\t Use this option if you want to filter your own IP address from results.");
        System.err.println("\t\t Using \'-e\' option, this in not necessary unless you're running a BT Client on ports 6881-6889 of your \"public\" interface.\n");
        System.err.println("Example: Process from file ID 209473 to ID 190670 a max. of 500 torrents fetched from piratebay.org, \n         storing meta information and using the current folder.\n");
        System.err.println("\t java -jar Crawler2.0.jar -p http://piratebay.org/get/ -d ./ -l 190670 -m 500 -e torrent_file.dat 209473\n\n");
    }

    private static File fetchTorrent(long o) {

        String path = Crawler.base_url + o;
        BufferedInputStream br = null;
        BufferedOutputStream bw = null;
        File temp = null;

        try {
            URL url = new URL(path);
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (http.getContentType().equals("text/html") || http.getResponseCode() != 200) {
                System.out.println("Fetch Torrent: Torrent Not Available");
                return null;
            }

            br = new BufferedInputStream(http.getInputStream());
            String name = String.valueOf(o);
            temp = File.createTempFile(name, ".tmp");
            temp.deleteOnExit();
            bw = new BufferedOutputStream(new FileOutputStream(temp));
            int c = 0;

            while (c != -1) {
                c = br.read();
                bw.write(c);
            }

            br.close();
            bw.flush();
            bw.close();
            return temp;

        } catch (IOException e) {
            System.out.println("FetchTorrent: " + e.getMessage());
            //e.printStackTrace();
            try {
                br.close();
                bw.flush();
                bw.close();
                temp.delete();
            } catch (Exception ea) {
            }
            return null;
        }

    }

    protected static String checkLength(long length) {

        if (length > 0) {
            return Long.toString(length);
        }
        String binary = Integer.toBinaryString((int) length);
        BigInteger bi = new BigInteger(binary, 2);
        byte[] bin = bi.toByteArray();
        byte[] test = new byte[bin.length + 1];
        System.arraycopy(bin, 0, test, 1, bin.length);
        test[0] = 0;
        bi = new BigInteger(test);
        return bi.toString();
    }

    protected static synchronized boolean saveMetaData(String row){

        PrintWriter pw = null;
        try{
            pw = new PrintWriter(new BufferedWriter(new FileWriter(Crawler.meta, true)));
        }catch(IOException ioe){
            System.out.println("saveMetaData: "+ioe.getMessage());
            return false;
        }
            pw.println(row);
            pw.flush();
            pw.close();
        
            return true;
    }

    protected static synchronized void releaseCon() {
        Crawler.open_con--;
        Crawler.class.notify();
    }

    protected static synchronized void openCon() {
        Crawler.open_con++;
    }

    protected static synchronized void incCounter() {
        Crawler.count++;
    }
}
