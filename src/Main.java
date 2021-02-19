import org.json.simple.parser.ParseException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        String examsPath = "C:\\Users\\olgam\\IdeaProjects\\IntelProject\\rok6.json";
        String roomsPath = "C:\\Users\\olgam\\IdeaProjects\\IntelProject\\sale6.json";
        String solutionPath = "C:\\Users\\olgam\\IdeaProjects\\IntelProject\\mySolutionArc6.csv";
        String stepsPath = "C:\\Users\\olgam\\IdeaProjects\\IntelProject\\stepsArc.txt";

        new App(examsPath, roomsPath, solutionPath, stepsPath, true, 5, false, true).solve();

    }

}
