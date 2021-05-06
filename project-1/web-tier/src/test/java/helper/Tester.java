package helper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.clocomp.helper.EC2Operations;

public class Tester {
	
	private static final Logger logger = Logger.getLogger("Helper test");
	
	private static void initialize() {
		Handler fileHandler = null;
		try {
			fileHandler = new FileHandler("./HelperTest.log");
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.addHandler(fileHandler);
		
		fileHandler.setLevel(Level.ALL);
		fileHandler.setFormatter(new SimpleFormatter());
        logger.setLevel(Level.ALL);
	}
	
	public static void main(String[] args) {
		
		initialize();
		
		EC2Operations ec2Operations = new EC2Operations();
		
		logger.info(String.valueOf(ec2Operations.getNumberOfRunningOrPendingInstances()));
		
		/*String instanceId = ec2Operations.createInstance("ami-00ddb0e5626798373", "kp1");
		logger.info("Created instance : " + instanceId);*/
		
		logger.info(String.valueOf(ec2Operations.getNumberOfRunningOrPendingInstances()));
		
	}
}
