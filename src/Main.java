import org.apache.commons.cli.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    private static Queue<Integer> queue;

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("id", true, "인하광장 로그인 아이디");
        options.addOption("pw", true, "인하광장 로그인 패스워드");
        options.addOption("thread", true, "쓰레드 수");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse( options, args);

        if(cmd.hasOption("id") && cmd.hasOption("pw") && cmd.hasOption("thread")) {
            makeQueue(228590, 397579);

            int thread = Integer.parseInt(cmd.getOptionValue("thread"));
            for(int i=0 ; i<thread; i++) {
                InhaPlazaCrawler inhaPlazaCrawler = new InhaPlazaCrawler(cmd.getOptionValue("id"), cmd.getOptionValue("pw"), queue);
                inhaPlazaCrawler.start();
            }
        }
        else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "inha-plaza-crawler", options );
        }
    }

    private static void makeQueue(int start, int end) {
        queue = new ConcurrentLinkedQueue<Integer>();

        for(int i=start; i<= end; i++) {
            queue.add(new Integer(i));
        }
    }
}
