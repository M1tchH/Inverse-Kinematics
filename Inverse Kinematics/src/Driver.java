
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.*;
import java.awt.geom.AffineTransform;

public class Driver extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

	public class limb {
		private class segment {
			double length;
			double angle;

			public segment(double angle, double length) {
				this.length = length;
				this.angle = angle;
			}

			public double[] getEnd(double[] in) {
				return new double[] { in[0] + angle, Math.cos(in[0] + angle) * length + in[1],
						Math.sin(in[0] + angle) * length + in[2] };
			}

			public void paint(Graphics g, double[] in) {
				int x1 = (int) in[1];
				int y1 = (int) in[2];
				int x2 = (int) (Math.cos(in[0] + angle) * length + in[1]);
				int y2 = (int) (Math.sin(in[0] + angle) * length + in[2]);
				g.fillOval(x1 - 3, y1 - 3, 6, 6);
				g.drawOval(x2 - 3, y2 - 3, 6, 6);
				g.drawLine(x1, y1, x2, y2);
			}

			public void shift(double a) {
				angle += a;
			}
			
		}

		segment segments[];
		double x, y;

		public limb(int size, double length, double x, double y) {
			segments = new segment[size];
			for (int i = 0; i < size; i++) {
				segments[i] = new segment(0, length);
			}
			this.x = x;
			this.y = y;
		}

		public void paint(Graphics g) {
			double in[] = new double[] { 0, x, y };

			for (int i = 0; i < segments.length; i++) {
				segments[i].paint(g, in);
				in = segments[i].getEnd(in);
			}

		}

		public double[] getEnd() {
			double in[] = new double[] { 0, x, y };

			for (int i = 0; i < segments.length; i++) {
				in = segments[i].getEnd(in);
			}
			
			return new double[] {in[1], in[2]};
		}
		
		private double[] getStart(int iter) {
			if(iter >= segments.length) {
				return null;
			}
			
			double in[] = new double[] { 0, x, y };
			for (int i = 0; i < iter; i++) {
				in = segments[i].getEnd( in );
			}
			
			return new double[] {in[1], in[2]};
		}
		
	
	
		
		public void optimize(double[] target) {
			double shift = 0.01;
			//System.out.println("------------------------------------------------------------------------------");
			for(int j = 0; j <  10; j++)
			for(int i = 0;i < segments.length; i++) {
				double start[] = getStart(i);
				double end[] = getEnd();
				double distance = Math.sqrt( Math.pow(end[0] - start[0], 2) + Math.pow(end[1] - start[1], 2));
				double theta = Math.atan2((end[1] - start[1]), (end[0] - start[0]));
				double theta2 = Math.atan2((target[1] - start[1]), (target[0] - start[0]));
				
				
				double deltaDistance = distance * ( (target[0] - start[0]) * Math.sin(theta) + (start[1] - target[1]) * Math.cos(theta) );				
				deltaDistance /= Math.sqrt( Math.pow(target[1] -start[1] - distance * Math.sin(theta), 2) + Math.pow(target[0] - start[0] - distance * Math.cos(theta), 2) );
				
				double tDistance =  Math.sqrt( Math.pow(target[0] - end[0], 2) + Math.pow(target[1] - end[1], 2));
				if(tDistance < 1)
					return;
				
					//deltaDistance *= -1;
				//System.out.printf("segment: %d, start: (%f, %f), end: (%f,%f), \n distance: %f, theta: %f, deltaDistance: %f, TargetDistance:%f \n\n", i,start[0],start[1],end[0],end[1],distance,theta,deltaDistance,tDistance);
				
				if(Math.abs(deltaDistance) <= 0.3)
					continue;
				shift = 0.01;
				shift *= Math.pow(Math.abs(theta - theta2)*30 / segments.length , 0.6);
				//shift = shift * Math.pow(tDistance,0.2);
				if(deltaDistance < 0) {
					segments[i].shift(shift);
					continue;
				}
				segments[i].shift(-shift);
			}
		}
		

	}

	limb Limb[] = new limb[10];
			//(10, 40, 500, 500);

	double Target[] = new double[]{200, 460} ;

	public void paint(Graphics g) {
		super.paintComponent(g);

		//g.drawOval((int)Target[0] - 3, (int)Target[1] - 3, 6, 6);
		for(int i = 0; i < Limb.length;i++) {
			Limb[i].paint(g);
		}
		/*
		 * g.setColor(Color.red); g.drawOval((int)Limb.getStart(0)[0] -
		 * 3,(int)Limb.getStart(0)[1] - 3, 6, 6); g.drawOval((int)Limb.getStart(1)[0] -
		 * 3,(int)Limb.getStart(1)[1] - 3, 6, 6); g.drawOval((int)Limb.getEnd()[0] -
		 * 3,(int)Limb.getEnd()[1] - 3, 6, 6); g.setColor(Color.black);
		 */
		
		
	}

	private void update() {
		// TODO Auto-generated method stub
		for(int i = 0; i < Limb.length;i++) {
			Limb[i].optimize(Target);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		update();
		repaint();
	}

	public static void main(String[] arg) {
		Driver d = new Driver();
	}

	public Driver() {
		JFrame f = new JFrame();
		f.setTitle("INVERSE KINEMATICS");
		f.setSize(1000, 1000);
		f.setBackground(Color.BLACK);
		f.setResizable(false);
		f.addKeyListener(this);
		f.addMouseMotionListener(this);
		f.addMouseListener(this);
		f.add(this);
		// f.setLayout(null);
		t = new Timer(17, this);
		t.start();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

		for(int i = 0; i < Limb.length;i++) {
			Limb[i] = new limb((int)(Math.random()*10),(int)(Math.random()*80)+20,(int)(Math.random()*800 + 100),(int)(Math.random()*800+100));
		}
	}

	Timer t;

	@Override
	public void keyPressed(KeyEvent e) {
		// movement booleans and functions
		for(int i = 0; i < Limb.length;i++) {
			Limb[i] = new limb((int)(Math.random()*10),(int)(Math.random()*80)+20,(int)(Math.random()*1000),(int)(Math.random()*1000));
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	public void reset() {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) { // big collision detection
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		Target[0] = e.getX()-10;
		Target[1] = e.getY()-30;
	}
}
