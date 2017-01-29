package flappybird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;

public class FlappyBird implements ActionListener, MouseListener, KeyListener
{
	public static FlappyBird flappyB;
	public final int WIDTH=800, HEIGHT=600;
	public Renderer renderer;
	public Rectangle bird;
	Image birdImage;
	Image landscape;
	Image tree;
	
	public ArrayList<ImageWithAttributes>trees;
	public ArrayList<Rectangle> columns;
	public Random rand;
	
	public int ticks, yMotion, score, highScore;
	public boolean gameOver, started;
	public boolean soundAlreadyPlayed = false;
	public int speed = 5;
	public int countForChirps;
	
	File gameOverSound = new File("sounds/gameOver.wav");
	File wingsFlapSound = new File("sounds/wingsFlap.wav");
	File crashSound = new File("sounds/crash.wav");
	File dingSound = new File("sounds/ding.wav");
	File chirpSound = new File("sounds/chirp.wav");
	
	Color seeThrough = new Color(0,0,0,0);

	// Code below does not work properly for sounds
	/*
	InputStream gameOverFile = new FileInputStream("sounds/gameOver.wav");
	InputStream crashFile = new FileInputStream("sounds/crash.wav");
	InputStream wingsFlapFile = new FileInputStream("sounds/wingsFlap.wav");
	AudioStream gameOverSound;
	AudioStream crashSound;
	AudioStream wingsFlapSound;
	*/
	
	public FlappyBird() throws IOException
	{
		JFrame jframe = new JFrame();
		Timer timer = new Timer(20, this);
		renderer = new Renderer();
		rand = new Random();

		jframe.add(renderer);
		jframe.setSize(WIDTH, HEIGHT);
		jframe.setTitle("Flappy Bird");
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		jframe.setVisible(true);
		jframe.setDefaultCloseOperation(jframe.EXIT_ON_CLOSE);
		jframe.setResizable(false);
		
		try
		{
			birdImage = ImageIO.read(ResourceLoader.load("imgs/bird1.png"));
			landscape = ImageIO.read(ResourceLoader.load("imgs/landscape1.png"));
			tree = ImageIO.read(ResourceLoader.load("imgs/tree.png"));
		}
		catch(Exception e)
		{}
		bird = new Rectangle(WIDTH/2 -10, HEIGHT/2 -10, 20, 40);
		
		trees = new ArrayList<ImageWithAttributes>();
		addTree(true);
		addTree(true);
		addTree(true);
		
		columns = new ArrayList<Rectangle>();
		addColumn(true);
		addColumn(true);
		addColumn(true);
		addColumn(true);
		
		started = false;
		timer.start();	
	}
	
	
	public void addColumn(boolean start)
	{
		int space = 300;
		int width = 100;
		int height = 50 + rand.nextInt(300);
		
		if(start)
		{
			columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height -120, width, height));
			columns.add(new Rectangle(WIDTH + width + (columns.size()-1) * 300, 0, width, HEIGHT - height - space));
		}
		else
		{
			columns.add(new Rectangle(columns.get(columns.size()-1).x + 600, HEIGHT - height -120, width, height));
			columns.add(new Rectangle(columns.get(columns.size()-1).x, 0, width, HEIGHT - height - space));
		}
	}
	
	
	public void addTree(boolean start)
	{
		int scale = rand.nextInt(150) + 50;
		int offSet = rand.nextInt(900) + scale;

		if(start)
		{
			trees.add(new ImageWithAttributes(tree, WIDTH/2 + offSet, HEIGHT - 110 - scale, scale, scale));
		}
		else
		{
			trees.add(new ImageWithAttributes(tree, WIDTH + offSet, HEIGHT - 110 -scale, scale, scale));			
		}
	}
	 
	
	public void repaint(Graphics g)
	{
		g.setColor(Color.CYAN);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		g.drawImage(landscape, 0,0, WIDTH, HEIGHT - 110, null);
		/*
		g.drawImage(tree, treeX -= speed/2, HEIGHT - 200 - 100, 200, 200, null);
		g.drawImage(tree, treeX -= speed/4, HEIGHT - 200 - 100, 200, 200, null);
		g.drawImage(tree, treeX -= speed/4, HEIGHT - 200 - 100, 200, 200, null);
		*/
		for(ImageWithAttributes tree: trees)
		{
			paintTree(g, tree);
		}
		g.setColor(new Color(115,115, 10));
		g.fillRect(0, HEIGHT -120, WIDTH, 120);
		
		g.setColor(Color.green.darker().darker());
		g.fillRect(0, HEIGHT -120, WIDTH, 20);
			
		g.setColor(seeThrough);
		g.fillRect(bird.x, bird.y, bird.width, bird.height);
		g.drawImage(birdImage, bird.x - 30, bird.y -10, 60, 60 , null);
		
		for(Rectangle column : columns)
		{
			paintColumn(g, column);
		}
		
		g.setColor(Color.white);
		g.setFont(new Font("Arial", 1, 100));
		
		if(!started)
			g.drawString("Click to Start!", 60, HEIGHT/2 - 50);
		
		if(gameOver)
			g.drawString("Game Over!", 60, HEIGHT /2 - 50 );
		
		g.setFont(new Font("Arial", 1, 30));
		g.drawString("Score " + String.valueOf(score), 5, 30);
		g.drawString("High Score " + String.valueOf(highScore), 200, 30);
	}
	
	
	public void paintColumn(Graphics g, Rectangle column)
	{
		g.setColor(Color.GREEN.darker().darker());
		g.fillRect(column.x, column.y, column.width, column.height);
	}
	
	
	public void paintTree(Graphics g, ImageWithAttributes tree)
	{
		g.drawImage(tree.image, tree.x, tree.y, tree.width, tree.height, null);
	}
	
	
	public static void main(String[]args)
	{
		try {
			flappyB = new FlappyBird();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void playSounds(int option) throws IOException
	{
		try{
			switch(option)
			{
			case 0:
				if (soundAlreadyPlayed == false)
				{
					Clip clip = AudioSystem.getClip();
					clip.open(AudioSystem.getAudioInputStream(ResourceLoader.class.getResource("/sounds/gameOver.wav")));
					clip.start();
					soundAlreadyPlayed = true;
				}
				/*
				gameOverSound = new AudioStream(gameOverFile);
				AudioPlayer.player.start(gameOverSound);
				*/
				break;
			case 1:
				if (soundAlreadyPlayed == false)
				{
					Clip clip1 = AudioSystem.getClip();
					clip1.open(AudioSystem.getAudioInputStream(ResourceLoader.class.getResource("/sounds/crash.wav")));
					clip1.start();
				}
				/*
				crashSound = new AudioStream(crashFile);
				AudioPlayer.player.start(crashSound);
				*/
				break;
			case 2:
				Clip clip2 = AudioSystem.getClip();
				clip2.open(AudioSystem.getAudioInputStream(ResourceLoader.class.getResource("/sounds/wingsFlap.wav")));
				clip2.start();
				/*
				wingsFlapSound = new AudioStream(wingsFlapSound);
				AudioPlayer.player.start(wingsFlapSound);
				*/
				break;
			case 3:
				Clip clip3 = AudioSystem.getClip();
				clip3.open(AudioSystem.getAudioInputStream(ResourceLoader.class.getResource("/sounds/ding.wav")));
				clip3.start();
				break;
			case 4:
				Clip clip4 = AudioSystem.getClip();
				clip4.open(AudioSystem.getAudioInputStream(ResourceLoader.class.getResource("/sounds/chirp.wav")));
				clip4.start();
				break;
			}
		}
		catch(Exception e)
		{
		}
	}

	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(started)
		{		
			ticks++;
			for(int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);
				column.x -= speed;
			}
			
			for(int i = 0; i < trees.size(); i++)
			{
				ImageWithAttributes tree = trees.get(i);
				if(tree.height < 100)
					tree.x -= speed/3;
				else
					tree.x -= (speed/2 * (tree.height/100));
			}
			
			if(ticks%2==0 && yMotion < 15)
				yMotion+=2;
			
			for(int i =0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);
				if(column.x + column.width < 0)
				{
					columns.remove(column);
					if(column.y==0)
					{
						addColumn(false);
					}
				}
			}
			
			bird.y+= yMotion;
			
			for(int i = 0; i<trees.size(); i++)
			{
				ImageWithAttributes tree = trees.get(i);
				if(tree.x + tree.width < 0)
				{
					trees.remove(tree);
					addTree(false);
				}
			}
			
			for(Rectangle column: columns)
			{
				//if(column.y ==0 && bird.x + bird.width / 2 > column.x + column.width / 2 - 10 && bird.x + bird.width/2 < column.x + column.width/2 +10)
				if(column.y ==0 && bird.x + bird.width / 2 == column.x + column.width / 2)
				{
					countForChirps++;
					try {
						playSounds(3);
						if(countForChirps%3 == 0)
							playSounds(4);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					score++;
					if(score > highScore)
						highScore = score;
				}
				
				if(column.intersects(bird))
				{
					
					try {
						playSounds(1);
						playSounds(0);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					gameOver=true;
					
					if(bird.x <= column.x)
						bird.x = column.x - bird.width;
					
					else if(column.y != 0)
						bird.y = column.y - bird.height;
					
					else if (bird.y < column.height)
						bird.y = column.height;
				}		
			}
			
			if(bird.y > HEIGHT -120 - bird.height)
			{
				gameOver =true; 
				try {
					playSounds(1);
					playSounds(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				bird.y = HEIGHT - 120 - bird.height; 
				yMotion = 0;
			}
			
			if(bird.y < 0 )
			{
				gameOver =true; 
				try {
					playSounds(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}	
		renderer.repaint();	
	}


	public void jump() throws IOException
	{
		if(gameOver)
		{
			bird = new Rectangle(WIDTH/2 -10, HEIGHT/2 -10, 20, 40);
			columns.clear();
			trees.clear();
			yMotion = 0;
			score=0;
			countForChirps=0;
			
			addTree(true);
			addTree(true);
			addTree(true);
			
			addColumn(true);
			addColumn(true);
			addColumn(true);
			addColumn(true);
			
			gameOver = false;
			soundAlreadyPlayed = false;
			
			try{
				playSounds(4);
			}
			catch(Exception e)
			{}
		}
		if(!started)
		{
			started = true;
		}
		else if(!gameOver)
		{
			if(yMotion > 0)
				yMotion = 0;
			yMotion -=10;
			playSounds(2);
		}
		
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		try {
			jump();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	@Override
	public void mouseEntered(MouseEvent e) {		
	}


	@Override
	public void mouseExited(MouseEvent ej) {	
	}


	@Override
	public void mousePressed(MouseEvent e) {		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
	}


	
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
			try {
				jump();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
	}


	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}




class ImageWithAttributes implements Comparable<ImageWithAttributes>
{
	public int x, y, width, height;
	public Image image;
	
	public ImageWithAttributes(Image image, int x, int y, int width, int height)
	{
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public int compareTo(ImageWithAttributes otherImage) {
		if (this.height > otherImage.height)
			return 1;
		else if (this.height < otherImage.height)
			return -1;
		else
			return 0;
	}
}
