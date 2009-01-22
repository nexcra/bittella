/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geoit;

/* CountryLookupTest.java */

/* Only works with GeoIP Country Edition */
/* For Geoip City Edition, use CityLookupTest.java */

import com.maxmind.geoip.*;
import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

class Locator {
    
    private LookupService cl;
    private boolean init = false;
    private boolean codes = false;
    private File asm;
    private File nodeIP;
    private File geoIP;
    private File codeAS;
    private String path;
    private HashMap<String,String> countryCode = new HashMap<String,String>();
    
    public static void main(String[] args) {
	try {

            Locator locator = new Locator();
            locator.parseArgs(args);
            locator.initialize();
            if(!locator.codes) locator.getCountryCodes();
            else locator.parseASCodes();
            locator.parseEdgeFile();
	}
	catch (Exception e) {
	    System.out.println("Exception.Main: "+e.getMessage());
            e.printStackTrace();
            System.err.println("Usage: java -jar GeoIT.jar -a <asm_file> [-n <nodes_csv_file>|-cc <codes_cvs_file>] [-d <path_to_files>]");
	}
    }
    
    public void parseArgs(String[] args)throws Exception{
               
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        String sep = System.getProperty("file.separator");
        
        for(int i=0;i<args.length;i++){
            map.put(args[i], i);
        }
        
        if(map.containsKey("-d")){
            int pos = map.get("-d");
            String dir = args[pos+1];
            System.out.println("Path to Data Files: "+dir);
            this.geoIP = new File(dir+sep+"GeoIP.dat");
            this.path = dir;
        }else{
            System.out.println("Path to GeoIP.dat File: .");
            this.geoIP = new File("."+sep+"GeoIP.dat");
            this.path = ".";
        }
        
        if(map.containsKey("-a")){
            int pos = map.get("-a");
            String asmf = args[pos+1];
            this.asm = new File(this.path+sep+asmf);
        }else{
            throw new Exception("Edge File to be modified, must be specified");
        }

        if(map.containsKey("-cc")){
            this.codes = true;
            int pos = map.get("-cc");
            String ccf = args[pos+1];
            this.codeAS = new File(this.path+sep+ccf);
        }
        
        if(map.containsKey("-n")){
            int pos = map.get("-n");
            String nodes = args[pos+1];
            this.nodeIP = new File(this.path+sep+nodes);
            this.codes = false;
        }else if(!this.codes){
            throw new Exception("Nodes File must be specified");
        }
        
    }
    
    public void initialize()throws IOException{
        
            if(this.init) return;
            else this.init = true;
	    // You should only call LookupService once, especially if you use
	    // GEOIP_MEMORY_CACHE mode, since the LookupService constructor takes up
	    // resources to load the GeoIP.dat file into memory
	    //LookupService cl = new LookupService(dbfile,LookupService.GEOIP_STANDARD);
	    this.cl = new LookupService(this.geoIP,LookupService.GEOIP_MEMORY_CACHE);   
            if(this.cl == null) System.err.println("Error Occurred, could not initilize lookup service");
            else System.out.println("LookupService Initialize....waiting for queries");
    }
    
    public void getCountryCodes()throws IOException{
        
        String sep = System.getProperty("file.separator");
        LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(this.nodeIP)));
        File ccf = new File(this.path+sep+"country_codes.csv");
        ccf.createNewFile();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(ccf)));
        File ccf2 = new File(this.path+sep+"ips.csv");
        ccf2.createNewFile();
        PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(ccf2)));
        String line = "";
        int counter = 0;
        System.out.println(this.cl.getDatabaseInfo().getDate());
        while (line != null){
            
            line = lnr.readLine();
            if(line == null) continue;
            StringTokenizer st = new StringTokenizer(line,",",false);
            String ip = st.nextToken();
            if(ip == null || ip.indexOf("-")!=-1) continue;
            for(int i=0;i<3;i++) st.nextToken();
            String asnum = st.nextToken();
            if(asnum == null || asnum.equals("-1")) continue;
            
  
            Country country = null;
            System.out.println(ip);
            try{country =  this.cl.getCountry(ip);}catch(Exception e){System.err.println(e.getMessage());}
            if(country == null){
                System.out.println("Country for IP: "+ip+" not found. "+counter);
                continue;
            }
            counter++;
            String cc = country.getCode();
            this.countryCode.put(asnum, cc);
            System.out.println("Found Country Code "+cc+" for IP "+ip);
            pw.println(asnum+","+cc);
            pw2.println(ip);
            
        }
        
        pw.flush();
        pw.close();
        pw2.flush();
        pw2.close();
        lnr.close();
        System.out.println("Got Codes!");
    }
    
    
    public int parseEdgeFile()throws Exception{
        
        System.out.println("Reading Edge files....");
        LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(this.asm)));
        File f = new File(this.asm.getName()+".asm");
        f.createNewFile();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        String line  = "";
        int counter = 0;
        java.text.SimpleDateFormat datf = new java.text.SimpleDateFormat("dd/mm/yyyy");
        
        while(line != null){
            
            line = lnr.readLine();
            if(line == null) continue;
            StringTokenizer st = new StringTokenizer(line,",",false);
            String asOrig = st.nextToken();
            String asDest = st.nextToken();
            String date = st.nextToken();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.text.SimpleDateFormat sdfa = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
            date = sdf.format(sdfa.parse(date));
            String minDel = st.nextToken();
            String maxDel = st.nextToken();

            if(!this.countryCode.containsKey(asOrig)) continue;
            String ccOrig = this.countryCode.get(asOrig);
            if(!this.countryCode.containsKey(asDest)) continue;
            String ccDest = this.countryCode.get(asDest);
            String out = asOrig+","+asDest+","+ccOrig+","+ccDest+",null,null,"+date+","+minDel+","+maxDel+"\r"; 
            pw.println(out);
            System.out.println(out);
            counter++;
            
        }
        
        lnr.close();
        pw.flush();
        pw.close();
        return counter;
    }
    
    public void close(){
        cl.close();
    }
    
    public void parseASCodes()throws IOException{
        LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(this.codeAS)));
        String line  = "";
        
        while(line != null){
            
            line = lnr.readLine();
            if(line == null) continue;
            String asnum = line.substring(0,line.indexOf(","));
            String cc = line.substring(line.indexOf(",")+1);
            this.countryCode.put(asnum, cc);
        }
        
        lnr.close();
    }
    
}

