package assignment;
import processing.core.*;

public class MainApp extends PApplet {

	float theta = 0;

    public void setup() {
        size(640, 480, P3D);
    }

    public void draw() {
    	 background(255);
    	 lights();
    	 noStroke();
    	 
    	 translate(width/2, height/2);


    	fill(255, 0, 0);
    	 pushMatrix();


    	   rotateX(radians(frameCount*3));
    	   rotateY(radians(frameCount*3));
    	   sphereDetail(30);
    	   sphere(100);

    	 popMatrix();
    	 
    	}
    }

