/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package h3agwas.config;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author scott
 */
public class H3agwasConfig {

    
    static HashMap<String,String> harg = new HashMap<>();
    
    private static String showArg(String arg) {
        String out[] = {arg,harg.get(arg)};
        return
        String.format("    %-20s = \"%s\"\n",out);
    }
    
    private static String template() {
        String template = 
                  "plinkImage = '"+harg.get("plinkImage")+"'\n"
                + "rEngineImage = '"+harg.get("rEngineImage")+"'\n"
                + "swarmPort = '2376'\n"
                + "\n"
                + "manifest {\n"
                + "    homePage = 'http://github.com/h3abionet/h3agwas'\n"
                + "    description = 'GWAS Pipeline for H3Africa'\n"
                + "    mainScript = 'gwas.nf'\n"
                + "}\n"
                + "\n"
                + "params {\n"
                + "\n"
                + "    // Directories\n"
                + "    work_dir                = \""+harg.get("work_dir")+"\"\n"
                + "    input_dir               = \"${params.work_dir}/"+harg.get("input_dir")+"\"\n"
                + "    output_dir              = \"${params.work_dir}/"+harg.get("output_dir")+"\"\n"
                + "    scripts                 = \"${params.work_dir}/"+harg.get("scripts")+"\"\n"
                + "\n"
                + "    // Data\n";
        String parms [] = {"data_name","high_ld_regions_fname","sexinfo_available",
                           "cut_het_high","cut_het_low","cut_miss","cut_diff_miss",
                           "cut_maf","cut_mind","cut_geno","cut_hwe","pi_hat",
                           "plink_process_memory","other_process_memory",
                           "max_plink_cores"};
        for (String arg : parms)
           template = template + showArg(arg);
        template = template
                + "\n"
                + "}\n"
                + "profiles {\n"
                + "\n"
                + "    // For execution on a local machine, no containerization. -- Default\n"
                + "    standard {\n"
                + "        process.executor = 'local'\n"
                + "    }\n"
                + "\n"
                + "    // For execution on a PBS scheduler, no containerization.\n"
                + "    pbs {\n"
                + "        process.executor = 'pbs'\n"
                + "        process.queue = '"+harg.get("queue")+"'\n"
                + "    }\n"
                + "\n"
                + "    // For execution on a PBS scheduler with containerization.\n"
                + "    pbsDocker {\n"
                + "\n"
                + "        process.executor = 'pbs'\n"
                + "        process.queue = 'long'\n"
                + "        process.memory= '10GB'\n"
                + "\n"
                + "        process.$removeDuplicateSNPs.container = \"$plinkImage\"\n"
                + "        process.$identifyIndivDiscSexinfo.container = \"$plinkImage\"\n"
                + "        process.$calculateSampleMissing.container = \"$plinkImage\"\n"
                + "        process.$calculateSampleHetrozygosity.container = \"$plinkImage\"\n"
                + "        process.$pruneForIBD.container = \"$plinkImage\"\n"
                + "        process.$removeQCIndivs.container = \"$plinkImage\"\n"
                + "        process.$calculateMaf.container = \"$plinkImage\"\n"
                + "        process.$calculateSnpMissigness.container = \"$plinkImage\"\n"
                + "        process.$calculateSnpSkewStatus.container = \"$plinkImage\"\n"
                + "        process.$removeQCPhase1.container = \"$plinkImage\"\n"
                + "        process.$computePhase0.container = \"$plinkImage\"\n"
                + "\n"
                + "        process.$generateMissHetPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateMafPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateSnpMissingnessPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateDifferentialMissingnessPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateHwePlot.container = \"$rEngineImage\"\n"
                + "\n"
                + "        docker.remove = true\n"
                + "        docker.runOptions = '--rm'\n"
                + "        docker.registry = 'quay.io'\n"
                + "        docker.enabled = true\n"
                + "        docker.temp = 'auto'\n"
                + "        docker.fixOwnership = true\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    // Execute pipeline with Docker locally\n"
                + "    docker {\n"
                + "        process.$removeDuplicateSNPs.container = \"$plinkImage\"\n"
                + "        process.$identifyIndivDiscSexinfo.container = \"$plinkImage\"\n"
                + "        process.$calculateSampleMissing.container = \"$plinkImage\"\n"
                + "        process.$calculateSampleHetrozygosity.container = \"$plinkImage\"\n"
                + "        process.$pruneForIBD.container = \"$plinkImage\"\n"
                + "        process.$removeQCIndivs.container = \"$plinkImage\"\n"
                + "        process.$calculateMaf.container = \"$plinkImage\"\n"
                + "        process.$calculateSnpMissigness.container = \"$plinkImage\"\n"
                + "        process.$calculateSnpSkewStatus.container = \"$plinkImage\"\n"
                + "        process.$removeQCPhase1.container = \"$plinkImage\"\n"
                + "        process.$computePhase0.container = \"$plinkImage\"\n"
                + "\n"
                + "        process.$generateMissHetPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateMafPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateSnpMissingnessPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateDifferentialMissingnessPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateHwePlot.container = \"$rEngineImage\"\n"
                + "\n"
                + "        docker.remove = true\n"
                + "        docker.runOptions = '--rm'\n"
                + "\t      docker.registry = 'quay.io'\n"
                + "        docker.enabled = true\n"
                + "        docker.temp = 'auto'\n"
                + "        docker.process.executor = 'local'\n"
                + "        docker.fixOwnership = true\n"
                + "    }\n"
                + "\n"
                + "    dockerpbs {\n"
                + "        process.executor = 'pbs'\n"
                + "        process.queue = 'WitsLong'\n"
                + "        process.$removeDuplicateSNPs.container = 'plink'\n"
                + "        process.$identifyIndivDiscSexinfo.container = 'plink'\n"
                + "        process.$calculateSampleMissing.container = 'plink'\n"
                + "        process.$calculateSampleHetrozygosity.container = 'plink'\n"
                + "        process.$pruneForIBD.container = 'plink'\n"
                + "        process.$removeQCIndivs.container = 'plink'\n"
                + "        process.$calculateMaf.container = 'plink'\n"
                + "        process.$calculateSnpMissigness.container = 'plink'\n"
                + "        process.$calculateSnpSkewStatus.container = 'plink'\n"
                + "        process.$removeQCPhase1.container = 'plink'\n"
                + "        process.$computePhase0.container = 'plink'\n"
                + "\n"
                + "        process.$generateMissHetPlot.container = 'r'\n"
                + "        process.$generateMafPlot.container = 'r'\n"
                + "        process.$generateSnpMissingnessPlot.container = 'r'\n"
                + "        process.$generateDifferentialMissingnessPlot.container = 'r'\n"
                + "        process.$generateHwePlot.container = 'r'\n"
                + "\n"
                + "        docker.enabled = true\n"
                + "        temp = 'auto'\n"
                + "        fixOwnership = true\n"
                + "    }\n"
                + "\n"
                + "\n"
                + "    // Execute pipeline with Docker Swarm setup\n"
                + "    dockerSwarm {\n"
                + "\n"
                + "        process.$removeDuplicateSNPs.container = \"$plinkImage\"\n"
                + "        process.$identifyIndivDiscSexinfo.container = \"$plinkImage\"\n"
                + "        process.$calculateSampleMissing.container = \"$plinkImage\"\n"
                + "        process.$calculateSampleHetrozygosity.container = \"$plinkImage\"\n"
                + "        process.$pruneForIBD.container = \"$plinkImage\"\n"
                + "        process.$removeQCIndivs.container = \"$plinkImage\"\n"
                + "        process.$calculateMaf.container = \"$plinkImage\"\n"
                + "        process.$calculateSnpMissigness.container = \"$plinkImage\"\n"
                + "        process.$calculateSnpSkewStatus.container = \"$plinkImage\"\n"
                + "        process.$removeQCPhase1.container = \"$plinkImage\"\n"
                + "        process.$computePhase0.container = \"$plinkImage\"\n"
                + "\n"
                + "        process.$generateMissHetPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateMafPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateSnpMissingnessPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateDifferentialMissingnessPlot.container = \"$rEngineImage\"\n"
                + "        process.$generateHwePlot.container = \"$rEngineImage\"\n"
                + "\n"
                + "        docker.registry = 'quay.io'\n"
                + "        docker.remove = true\n"
                + "        docker.runOptions = '--rm'\n"
                + "        docker.enabled = true\n"
                + "        docker.temp = 'auto'\n"
                + "        docker.process.executor = 'local'\n"
                + "        docker.fixOwnership = true\n"
                + "        docker.engineOptions = \"-H :$swarmPort\"\n"
                + "    }\n"
                + "\n"
                + "\n"
                + "\n"
                + "}\n"
                + "";       
       return template;
        
    }
    
    
    private static void getOptions(Path conffile) throws IOException {
        XSSFSheet sheet;
        Row row;
        Iterator<Row> rows;
        String var_name,def_val, use_val;
        FileInputStream f = new FileInputStream(conffile.toString());
        rows = new XSSFWorkbook(f).getSheetAt(0).rowIterator();
        while (rows.hasNext()) {
            row = rows.next();
            if (row.getCell(0).getStringCellValue().charAt(0)=='#') continue;
            var_name = row.getCell(1).getStringCellValue();
            def_val  = row.getCell(2) != null ? row.getCell(2).getStringCellValue() :  "";
            use_val  = (row.getCell(4) != null) ? row.getCell(4).getStringCellValue() : "";
            if (use_val.length() == 0 ) use_val = def_val;
            harg.put(var_name, use_val);
        }     
    }
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        getOptions(Paths.get(args[0]));
        System.out.println(template());
    }
    
}