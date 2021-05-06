package com.clocomp.webtier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.clocomp.helper.EC2Operations;
import com.clocomp.webtier.autoscale.ScaleController;

@SpringBootApplication
public class WebTierApplication {

	public static void main(String[] args) {

		ScaleController scaleController = new ScaleController();
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				scaleController.mainThread();
				
			}
		});
		t.start();
		
		SpringApplication.run(WebTierApplication.class, args);
		
	}

}
