package storm.starter;

/**
 * Created by Pradheep on 6/2/15.
 */

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.utils.Utils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import storm.starter.HelperClasses.WindowObject;
import storm.starter.bolt.MovingAverageBolt;
import storm.starter.spout.RandomIntegerSpout;

import java.io.File;

import static java.lang.System.exit;

public class CummulativeMovingAvgTopology {

    public static void main(String[] args) throws Exception {
        final Logger LOG = Logger.getLogger(CummulativeMovingAvgTopology.class.getName());
        System.gc();
        WindowObject wObject;

        String log4jConfigFile = System.getProperty("user.dir")
                + File.separator + "log4j.properties";

        LOG.info("!!!!!!!!!!!!!File Path is::" + log4jConfigFile);
        PropertyConfigurator.configure(log4jConfigFile);
        WindowTopologyBuilder builder;

        Config conf = new Config();
        conf.setDebug(false);
        LOG.info("Testing Time Based");
        
        wObject = new WindowObject("sliding", 10000, 500, false);
        //wObject = new WindowObject("landmark", 3, 5, true);
        builder = new WindowTopologyBuilder();
        builder.setSpout("RandomInt", new RandomIntegerSpout(), 10);
        builder.setBolt("Sliding", wObject.CreateWindow() ,1).shuffleGrouping("RandomInt");
        builder.setBolt("Average", new MovingAverageBolt(), 1).shuffleGrouping("Sliding","dataStream")
                .shuffleGrouping("Sliding","mockTickTuple");

        if (args != null && args.length > 0) {
            conf.setNumWorkers(1);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        }
        else {

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(1200000);
            //Utils.sleep(40000);
            cluster.killTopology("test");
            System.out.println("Topology Killed");
            cluster.shutdown();
            exit(0);
        }
    }
}


