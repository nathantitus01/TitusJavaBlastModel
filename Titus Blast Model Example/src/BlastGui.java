/*
 * BlastGui.java
 * Author: Nathan Titus
 * Date: 4/22/2023
 * Purpose: Instantiates and contains the methods required to animate the particles.
 * 
 * Version notes: Currently handles calculations of particle interactions, but 
 * these should be moved to a separate thread later. Also, too many variables are
 * global to the class and should be moved into specific methods.
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

import java.util.Arrays;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;

import java.lang.Math;

//Contains methods for visualizing and animating example blast simulations.
public class BlastGui extends JFrame {
	
	static final long serialVersionUID = 123456;
	
	//Simulation variables
	private int ballNumber = 1001;
	private double aFactor = 100;
	private int wFactor = 1+(int)ballNumber/450;
	private int timerInt = 100;
	private int tickMax = 355;
	
	//Utility variables
	private javax.swing.Timer timer;
	private Random rand = new Random();
	private int tick;
	
	//Graphics variables
	private int x = 20;
	private int y = 40;
	private int graphShiftx = 30;
	private int graphShifty = 75;	
	private int width = 400;
	private int height = 200;
	private int gap = 80;
	
	//Ball Variables
	public Ellipse2D[] ballVec = new Ellipse2D[ballNumber];
	public double[] ballXvec = new double[ballNumber];
	public double[] ballYvec = new double[ballNumber];
	public double[] ballVelXvec = new double[ballNumber];
	public double[] ballVelYvec = new double[ballNumber];

	//Wall Variables
	public double[] wallXvec = {60, 80, 80, 80, 80, 60};
	public double[] wallYvec = {75, 45, 15, -15, -45, -75};
	public int[] wallStrengthvecPrime = {2, 2, 2, 2, 2};
	public int[] wallStrengthvec = new int[wallStrengthvecPrime.length];
	public double[] wallMvec = new double[wallStrengthvecPrime.length];
	public double[] wallYintvec = new double[wallMvec.length];
	
	//Flux calculation and recording variables
	private int[] radialBallCounter = new int[tickMax+10];
	private int[] radialBallChange = new int[tickMax+10];
	private int fluxRadius = 70;
	
	
	//Contains the animations of the 
	public BlastGui() {
		//set Jframe details
		setFrameDetails();
		
		//Instantiate and set particle start locations
		initializeBalls();
		initializeBallLocations();
		
		//Used later to calculate interactions.
		calcWallCoeff();
		
		//Draw the objects on the JFrame 
		repaint();
		
		//Controls when the objects are redrawn
		timer = new javax.swing.Timer(timerInt, new TimerListener());
		
	}
	 
	//Draw Graphics Objects 
    public void paint(Graphics g) {
        super.paint(g);
        drawGraphs(g);
        drawVisuals(g);
        drawPlots(g);
    }
    
    //Initializes and reinitializes variables for simulations 
    private void startAnimating() {
		//reset the animation frames
    	tick = 0;
    	
    	//reset the balls
		initializeBallLocations();
		
		//reset the walls
		for (int j = 0; j<wallStrengthvec.length; j++) {
			wallStrengthvec[j] = wallStrengthvecPrime[j]*wFactor;
		}
		
		//reset the plots
		Arrays.fill(radialBallCounter, 0);
		Arrays.fill(radialBallChange, 0);
		
		//start the timer
		timer.start();
	}
    
    //Create ball graphics and set unmoving ball location
    private void initializeBalls() {
    	for (int i = 0; i<ballNumber; i++) {
			ballVec[i] = new Ellipse2D.Double();
		}
		ballXvec[0] = 0;
		ballYvec[0] = -1;
		ballVelXvec[0] = 0;
		ballVelYvec[0] = 0;
    }
    
    //Set initial locations of moving balls
    private void initializeBallLocations() {
    	for (int i = 1; i<ballNumber; i++) {
    		ballXvec[i] = rand.nextDouble()-0.5;
    		ballYvec[i] = rand.nextDouble()-0.5;
    		ballVelXvec[i] = ballXvec[i]+0.1;
    		ballVelYvec[i] = ballYvec[i];
    		
    	}
    }
    
    //Calculate the linear coefficients defining the "wall" lines
    public void calcWallCoeff() {
    	double y1; double y2; double x1; double x2;
    	double m; double yint;
    	for (int i = 0; i<wallXvec.length-1; i++) {
    		y1 = wallYvec[i]; y2 = wallYvec[i+1];
    		x1 = wallXvec[i]; x2 = wallXvec[i+1];
    		
    		//slope and y intercept
    		m = (y2-y1)/(x2-x1); yint = y1-x1*m;
    		wallMvec[i] = m; wallYintvec[i] = yint;
    	}
    }
    
    //Calculates the "pressure" interactions between the balls
    public void updateBallVelocities() {
    	double pushVecX; double pushVecY; double pushVecMag;
    	
    	//Adjusts ball velocities based on relative position to other balls
    	for (int i = 1; i<ballNumber; i++) {
    		for (int j = i+1; j<ballNumber; j++) {
    			pushVecX = ballXvec[j]-ballXvec[i];
    			pushVecY = ballYvec[j]-ballYvec[i];
    			pushVecMag = Math.sqrt(pushVecX*pushVecX+pushVecY*pushVecY);
    			if (pushVecMag<0.01) { pushVecMag = 0.01; }
    			pushVecX = pushVecX/pushVecMag;
    			pushVecY = pushVecY/pushVecMag;
    			
    			ballVelXvec[i] -= aFactor*pushVecX/pushVecMag/ballNumber/(tick+200);
    			ballVelYvec[i] -= aFactor*pushVecY/pushVecMag/ballNumber/(tick+200);
    			ballVelXvec[j] += aFactor*pushVecX/pushVecMag/ballNumber/(tick+200);
    			ballVelYvec[j] += aFactor*pushVecY/pushVecMag/ballNumber/(tick+200);
    		}
    	}
    	
    	//Since no atmospheric pressure exists these calculations ensure
    	//a negative phase to the pressure wave
    	for (int i = 1; i<ballNumber; i++) {
    		for (int j = 0; j<1; j++) {
    			pushVecX = ballXvec[j]-ballXvec[i];
    			pushVecY = ballYvec[j]-ballYvec[i];
    			pushVecMag = Math.sqrt(pushVecX*pushVecX+pushVecY*pushVecY);
    			pushVecX = pushVecX/pushVecMag;
    			pushVecY = pushVecY/pushVecMag;
    			ballVelXvec[i] += pushVecX/100;
    			ballVelYvec[i] += pushVecY/100;
    		}
    	}
    	
    }
    
    //Animate the Balls
    public class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//Update Ball Velocities
			updateBallVelocities();
			
			//Update Ball and Wall Position
			updateVisualsPositions();
			
			tick++;
			//for now stop animation after a certain number of frames
			if (tick > tickMax)
				timer.stop();
		}
	};
	
	//Updates the Balls and Walls, also handles ball/wall interactions
	private void updateVisualsPositions() {
		int wallNum; double xvel; double yvel;
		double[] newVels = new double[2]; 
		int[] wallHitVec = new int[wallStrengthvec.length]; 
		int[] tempWallHitVec = new int[wallStrengthvec.length];
		int[] wallHitVecSum = new int[wallStrengthvec.length]; 
		Arrays.fill(wallHitVec,0);
		
		//Update Ball Positions
		for (int i = 1; i<ballNumber; i++) {
			Arrays.fill(tempWallHitVec,0);
			xvel = ballVelXvec[i];
			yvel = ballVelYvec[i];
			
			//Which wall (if any) is hit by the ball
			wallNum = checkIntercepts(i,xvel,yvel);
			
			//Handle ball wall interactions until ball doesn't hit wall
			while (wallNum>=0) {
				//Track which walls are hit
				tempWallHitVec[wallNum]++;
				
				//Deal with walls joined by severe acute angles
				if (tempWallHitVec[wallNum]>1) {
					tempWallHitVec[wallNum]--;
					xvel = 0; yvel = 0;
					break;
				}
				
				//Define new velocities using wall interactions
				newVels = slideBall(i,wallNum,xvel,yvel);
				xvel = newVels[0]; yvel = newVels[1];
				
				//Avoid numerical errors
				xvel=(int)(xvel*100)/100.0;
				yvel=(int)(yvel*100)/100.0;
				
				//Update ball velocities
				ballVelXvec[i] = xvel;
				ballVelYvec[i] = yvel;
				
				//Do new velocities result in another interaction?
				if (xvel!=0 || yvel!=0) {
					wallNum = checkIntercepts(i,xvel,yvel);
				} else {
					wallNum = -1;
				}
			}
			
			//Update Final Posiiton
			ballXvec[i]+=xvel;
			ballYvec[i]+=yvel;
			
			//Count balls inside and outside of flux surface
			if (Math.sqrt(ballXvec[i]*ballXvec[i]+ballYvec[i]*ballYvec[i])>fluxRadius) {
				radialBallCounter[tick+1]++;
			}
			
			//Dont allow graphics to leave graph
			if (ballXvec[i]>2+width/2) { ballXvec[i]=2+width/2-1; }
			if (ballXvec[i]<4-width/2) { ballXvec[i]=4-width/2; }
			if (ballYvec[i]<53-height) { ballYvec[i]=53-height; }
			if (ballYvec[i]>height-50) { ballYvec[i]=height-50; }
			
			//Total interactions of balls and walls per wall
			for (int j = 0; j<wallHitVec.length; j++) {
				wallHitVecSum[j] = wallHitVec[j]+tempWallHitVec[j];
				wallHitVec[j] = wallHitVecSum[j];
			}
		}
		//Tracks change instead of sum
		radialBallChange[tick+1] = radialBallCounter[tick+1]-radialBallCounter[tick];
		
		//update Graphics
		repaint();
		
		//Check to see if a wall broke
		int comp;
		for (int j = 0; j<wallHitVec.length; j++) {
			comp = wallStrengthvec[j]-wallHitVec[j];
			if (comp<1) {
				wallStrengthvec[j]=0;
			}
		}
	}
	
	//Does the ball trajectory cross a wall? Return which wall
	private int checkIntercepts(int ind, double xvel, double yvel) {
		//x and y points of ball trajectory
		double x11 = ballXvec[ind];
		double x12 = x11+xvel;
		double y11 = ballYvec[ind];
		double y12 = y11+yvel;
		int wallNum = -1; boolean isect; double m; double yint;
		double x21; double x22; double y21; double y22;
		//check for intersections with walls
		for (int i=0; i<wallXvec.length-1;i++) {
    		if (wallStrengthvec[i]>=1) {
    			x21 = wallXvec[i];
    			x22 = wallXvec[i+1];
    			y21 = wallYvec[i];
    			y22 = wallYvec[i+1];
    			m = wallMvec[i];
    			yint = wallYintvec[i];
    			
    			//does y domain intercept
    			isect = (y21>y11)&&(y21>y12)&&(y22>y11)&&(y22>y12);
    			isect = isect || (y21<y11)&&(y21<y12)&&(y22<y11)&&(y22<y12);
    			//does x domain intercept
    			isect = isect || (x21>x11)&&(x21>x12)&&(x22>x11)&&(x22>x12);
    			isect = isect || (x21<x11)&&(x21<x12)&&(x22<x11)&&(x22<x12);
    			//do lines actually cross
    			if (Float.isFinite((float)m)) {
    	    		isect = isect || (y11-x11*m-yint)*(y12-x12*m-yint)>0.0001;
    			} else { //Wall is vertical
    				isect = isect || (x22-x11)*(x22-x12)>0;
    			}
    			if (!isect) {
    				wallNum=i;
    				break;
    			}
    		} 
    	}
		return wallNum;
	}
	
	//Slide ball along wall and handle final velocities
	private double[] slideBall(int ind, int wallNum, double xvel, double yvel) {
		//x and y points of ball trajectory
		double x11 = ballXvec[ind];
		double y11 = ballYvec[ind];
		
		//Magnitude of Ball Velocity
		double totalBallVel = Math.sqrt(xvel*xvel+yvel*yvel);
		
		//x and y points of wall segment
		double x21 = wallXvec[wallNum];
		double x22 = wallXvec[wallNum+1];
		double y21 = wallYvec[wallNum];
		double y22 = wallYvec[wallNum+1];
		
		//wall vector components
		double xdir = x22-x21;
		double ydir = y22-y21;
		
		double yint; double xint; double yint2;

		//Find intercept location
		if (xvel!=0) {
			if (xdir!=0) {
				yint = y11-x11*yvel/xvel; //Ball Y-intercept
				yint2 = y21-x21*ydir/xdir; //Wall Y-Intercept
				xint = (yint2-yint)/(yvel/xvel-ydir/xdir); //X at Ball/Wall Intercept
				yint = xint*yvel/xvel+yint; //Y at Ball/Wall Intercept;
			} else { //Wall is vertical
				xint = x22;
				yint = y11+yvel*(xint-x11)/xvel;
			}
		} else { //Ball is moving vertically
			xint = x11;
			yint = y21+ydir*(xint-x21)/xdir;
		}

		//Move Ball Next to But not Into Wall
		if (Math.abs(xint-x11)>0.01) {
			xint = x11+(xint-x11)-(xint-x11)/Math.abs(xint-x11);
		} else {
			xint=x11;
		}
		if (Math.abs(yint-y11)>0.01) {
			yint = y11+(yint-y11)-(yint-y11)/Math.abs(yint-y11);
		} else {
			yint=y11;
		}
		ballXvec[ind] = xint;
		ballYvec[ind] = yint;
		
		//Find new velocity direction
		double dot = xvel*xdir+yvel*ydir;
		if (dot<0) {
			xdir = xdir*-1;
			ydir = ydir*-1;
		}
		double wallMag = Math.sqrt(xdir*xdir+ydir*ydir);
		xdir = xdir/wallMag; ydir = ydir/wallMag;
		
		//Reduce movement by distance to intercept
		totalBallVel = totalBallVel-Math.sqrt((yint-y11)*(yint-y11)+(xint-x11)*(xint-x11));

		
		//Use magnitude and direction to update velocity vectors
		xvel = xdir*totalBallVel; yvel = ydir*totalBallVel;
		
		//Return velocities
		double[] vels = {xvel,yvel};
		return vels;
	}
    
	//Create Graphs for Plotting
    void drawGraphs(Graphics g) {	
    	drawBackground(g,graphShiftx,graphShifty,width,(int)height*2-100,8,"X".toCharArray(),"Y".toCharArray(),"Animation Example".toCharArray());
    	drawBackground(g,graphShiftx+width+gap,graphShifty,width-40,height-70,6,"Time".toCharArray(),"Pressure".toCharArray(),"Blast Pressure".toCharArray());
    	drawBackground(g,graphShiftx+width+gap,height+graphShifty-30,width-40,height-70,6,"Time".toCharArray(),"Impulse".toCharArray(),"Blast Impulse".toCharArray()); 
    }
    
    //Draw Physical Objects
    void drawVisuals(Graphics g) {	
    	drawWall(g);
    	drawBalls(g);
    }
    
    //Draw Plots
    void drawPlots(Graphics g) {	
    	int xadd = graphShiftx+width+2+gap; int yadd = height+graphShifty-92; 
    	int yadd2 = (int)height*2+graphShifty-122; int tickScale = 1;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLACK);
    	for (int i = 0; i<tickMax+1; i++) {
			g2.drawLine(i*tickScale+xadd, -2000*radialBallChange[i]/ballNumber+yadd, (i+1)*tickScale+xadd, -2000*radialBallChange[i+1]/ballNumber+yadd);
			g2.drawLine(i*tickScale+xadd, -120*radialBallCounter[i]/ballNumber+yadd2, (i+1)*tickScale+xadd, -120*radialBallCounter[i+1]/ballNumber+yadd2);	
    	}
    }
    
    //Used to draw a graph on an existent Graphics context
    private void drawBackground(Graphics g, int x, int y, int width, int height, int gridFactor, char[] xlabel, char[] ylabel, char[] title) {
    	//Transform for features
        Graphics2D g2d = (Graphics2D) g;
        
        //Draw Background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x,y,width,height);
        
        //Draw Grid
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 1; i<gridFactor/2+1; i++) {
            g2d.drawRect(x+(int)(i*width/gridFactor),y,(int)(((int)gridFactor/2)*width/gridFactor),height);
        }
        for (int i = 1; i<gridFactor/2+1; i++) {
            g2d.drawRect(x,y+(int)(i*height/gridFactor),width,(int)(((int)gridFactor/2)*height/gridFactor));
        }
        
        //Draw Border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x,y,width,height);
        
        //Add Labels
        g2d.drawChars(xlabel, 0, xlabel.length, (int)(x+width/2-xlabel.length*3), y+height+12);
        g2d.drawChars(ylabel, 0, ylabel.length, x-ylabel.length*6-6, (int)(y+height/2)+4);
        g2d.drawChars(title, 0, title.length, (int)(x+width/2-title.length*3), y-6);
    }
    
    //Draws balls on graph
    public void drawBalls(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int xadd = (int)width/2-5+(int)graphShiftx;
		int yadd = (int)height-15+(int)graphShifty/2;
		g2.setPaint(Color.RED);
    	for (int i=1; i<ballXvec.length;i++) {
			ballVec[i].setFrame((int)ballXvec[i]+xadd, (int)ballYvec[i]+yadd, 4+(int)500/ballNumber, 4+(int)500/ballNumber);
			g2.fill(ballVec[i]);
    	}
		g2.setPaint(Color.GREEN);
		ballVec[0].setFrame((int)ballXvec[0]+xadd, (int)ballYvec[0]+yadd, 10, 10);
		g2.fill(ballVec[0]);
    }
    
    //Draws unbroken walls on graph
    public void drawWall(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(5));

		double xadd = width/2+graphShiftx;
		double yadd = height+graphShifty/2-12;
		
    	for (int i=0; i<wallXvec.length-1;i++) {
    		if (wallStrengthvec[i]>1) {
    			g2.setPaint(Color.BLACK);
    		} else {
    			if (wallStrengthvec[i]<1) {
        			g2.setPaint(Color.WHITE);
    			} else {
        			g2.setPaint(Color.GREEN);
    			}
    		}
    		
			g2.drawLine((int)(wallXvec[i]+xadd), (int)(wallYvec[i]+yadd), (int)(wallXvec[i+1]+xadd), (int)(wallYvec[i+1]+yadd));
    	}
    }
    
    //Make the window visible
	public void setVisible(boolean vis) {
		super.setVisible(vis);
	}
	
	//Adds a button, sets relevant boundaries, and adds an event listener to button
	private void setFrameDetails() {
		getContentPane().setBackground(Color.LIGHT_GRAY);
		setBounds(x,y,(int)width*2+gap+graphShiftx,(int)height*2+graphShifty);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JButton btnNewButton = new JButton("Start Animation");
		btnNewButton.setBounds(width-gap, (int)(height*2-graphShifty/2), gap*3, (graphShifty/2));

		getContentPane().setLayout(null);
		btnNewButton.setForeground(Color.WHITE);
		btnNewButton.setBackground(Color.BLUE);
		getContentPane().add(btnNewButton);
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startAnimating();
			}
		});
	}
	
}