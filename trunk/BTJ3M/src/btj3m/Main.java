/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package btj3m;
import jBittorrentAPI.*;
/**
 *
 * @author jmcamacho
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
                try {
            TorrentProcessor tp = new TorrentProcessor();

            if(args.length < 1){
                System.err.println(
                        "Incorrect use, please provide the path of the torrent file...\r\n" +
                        "\r\nUsage:\r\n"+
                        "Main <torrent_file_path");

                System.exit(1);
            }
            TorrentFile t = tp.getTorrentFile(tp.parseTorrent(args[0]));
            if(args.length > 1)
                Constants.SAVEPATH = args[1];
            if (t != null) {
                boolean enabled = Boolean.parseBoolean(args[2]);
                int unb = Integer.parseInt(args[3]);
                DwManager dm = new DwManager(t, Utils.generateID(),enabled,unb);
                dm.startListening(6881, 6889);
                dm.startTrackerUpdate();
                dm.blockUntilCompletion();
                dm.stopTrackerUpdate();
                dm.closeTempFiles();
            } else {
                System.err.println(
                        "Provided file is not a valid torrent file");
                System.err.flush();
                System.exit(1);
            }
        } catch (Exception e) {

            System.out.println("Error while processing torrent file. Please restart the client");
            //e.printStackTrace();
            System.exit(1);
        }
    }

}
