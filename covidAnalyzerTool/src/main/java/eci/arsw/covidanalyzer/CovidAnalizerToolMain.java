/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eci.arsw.covidanalyzer;

import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author StivenVanegas
 */
public class CovidAnalizerToolMain {

    public static void main(String... args) throws Exception {
        CovidAnalyzerTool covidAnalyzerTool = new CovidAnalyzerTool();
        covidAnalyzerTool.crearHilos(5);
        covidAnalyzerTool.ejecutarHilos();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if (line.contains("exit")) {
                break;
            } else if (line.equals("")) {
                covidAnalyzerTool.activarHilos();
                String message = "Processed %d out of %d files.\nFound %d positive people:\n%s";
                Set<Result> positivePeople = covidAnalyzerTool.getPositivePeople();
                String affectedPeople = positivePeople.stream().map(Result::toString).reduce("", (s1, s2) -> s1 + "\n" + s2);
                message = String.format(message, covidAnalyzerTool.getAmountOfFilesProcessed(), covidAnalyzerTool.getAmountOfFilesTotal(), positivePeople.size(), affectedPeople);
                System.out.println(message);
            }
        }
    }
}
