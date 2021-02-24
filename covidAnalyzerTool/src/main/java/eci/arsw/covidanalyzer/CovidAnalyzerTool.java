package eci.arsw.covidanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Camel Application
 */
public class CovidAnalyzerTool {

    private ResultAnalyzer resultAnalyzer;
    private TestReader testReader;
    private int amountOfFilesTotal;
    private AtomicInteger amountOfFilesProcessed;
    
    ArrayList<AnalyzerThread> hilos = new ArrayList<>();

    public CovidAnalyzerTool() {
        resultAnalyzer = new ResultAnalyzer();
        testReader = new TestReader();
        amountOfFilesProcessed = new AtomicInteger();
    }
    
    public  void crearHilos(int n){
        amountOfFilesProcessed.set(0);
        List<File> resultFiles = getResultFileList();
        amountOfFilesTotal = resultFiles.size();
        int amountOfFiles = amountOfFilesTotal/n;
        int faltantes = amountOfFilesTotal % n;
        int inicio = 0;
        for(int i=0; i<n; i++){
            if(i == n-1){
                amountOfFiles += faltantes;
            }
            hilos.add(new AnalyzerThread(resultFiles.subList(inicio, inicio+amountOfFiles)));
            inicio += amountOfFiles;
        }
    }
    
    public void ejecutarHilos(){
        for(AnalyzerThread at: hilos){
            at.start();
        }
    }
    
    public void pausarHilos() {
        for (AnalyzerThread at : hilos) {
            at.setPause();
        }
    }

    public void activarHilos() {
        for (AnalyzerThread at : hilos) {
            at.setContinue();
        }
    }
    
    class AnalyzerThread extends Thread{
        
        List<File> resultFiles;
        private boolean pause = true;
        
        public AnalyzerThread(List<File> resultFiles){
            this.resultFiles = resultFiles;
        }
        
        public void run() {

            for (File resultFile : resultFiles) {
                
                synchronized (this) {
                    while (pause) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(CovidAnalyzerTool.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                List<Result> results = testReader.readResultsFromFile(resultFile);
                for (Result result : results) {
                    resultAnalyzer.addResult(result);
                }
                amountOfFilesProcessed.incrementAndGet();
            }

        }
        
        public synchronized void setPause() {
            this.pause = true;
        }

        public synchronized void setContinue() {
            this.pause = false;
            notifyAll();
        }
    }

    private List<File> getResultFileList() {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }


    public Set<Result> getPositivePeople() {
        return resultAnalyzer.listOfPositivePeople();
    }
    
    public int getAmountOfFilesProcessed(){
        return this.amountOfFilesProcessed.get();
    }
    
    public int getAmountOfFilesTotal(){
        return this.amountOfFilesTotal;
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */

}

