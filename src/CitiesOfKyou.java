import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.Math;
import java.util.Random;
import java.util.ArrayList;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

//Class to start everything up
public class CitiesOfKyou {
	//Window dimensions
	private static final int WIDTH = 800;
	private static final int HEIGHT = 640;
	
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public static void createAndShowGUI() {
		JFrame frame = new GameFrame(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
	}
}



//This class represents the game window (menubar, frame)
class GameFrame extends JFrame {
	//Variables for the dimensions of the frame
	private final int WIDTH;
	private final int HEIGHT;
	
	//Load file chooser for loading/saving games
	private JFileChooser chooser = new JFileChooser();
	
	//Constructor
	public GameFrame(int width, int height) {
		setTitle("Cities of Kyou");
		WIDTH = width;
		HEIGHT = height;
		setSize(width, height);
		
		//Add file menu to game screen
		addMenu();
	}
	
	private void addMenu() {
		//Create menu object
		JMenu menu = new JMenu("File");
		
		//Create a new item to put in menu
		//First item is to start a new game
		JMenuItem item = new JMenuItem("New Game");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				newGame();
			}
		});
		menu.add(item);
		
		//Option to load game
		item = new JMenuItem("Load Game");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				load();
			}
		});
		menu.add(item);
		
		//Option to save game
		item = new JMenuItem("Save Game");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				save();
			}
		});
		menu.add(item);
		
		//Option to exit/quit the game
		item = new JMenuItem("Exit");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		menu.add(item);
		
		//Attach menu to the menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		setJMenuBar(menuBar);
	}
	
	//Method used to start a new game
	public void newGame() {
		//Get input for the player's name
		String input = JOptionPane.showInputDialog("What is your character's name?");
		final Game game = new Game(input);

		//Add the game to the frame and give it the focus
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setContentPane(game);
				validate();
				getContentPane().requestFocusInWindow();
			}
		});
	}
	
	//Method used to load an older game
	public void load() {
		
	}
	
	//Method used to save out current game
	public void save() {
		
	}
}



//This class handles all the game functionality
//it houses major aspects of the game, so it needs
//to extend JPanel to display things
class Game extends JPanel {
	//Different characteristics of the game
	private String playerName;
	private Player player;
	private City city;
	private Menu menu;
	
	//Determine if in pause/start menu
	private boolean paused = false;
	//Determine if inside a store
	private boolean inside = false;
	//Which building is player in
	//And where did they enter from
	private int buildingNum, oldX, oldY;
	//Determine if inside the corporation building
	private boolean insideCorp = false;

	//Timer to redraw/animate graphics
	private Timer timer;

	//Graphical assests for the game
	private Graphics2D g2d;
	
	//How big the viewport is
	private final int WIDTH = 800;
	private final int HEIGHT = 640;
	
	public Game(String name) {
		//Instantiate parts of the game
		city = new City();		
		playerName = name;
		player = new Player(name);
		menu = new Menu(player.items);
		
		//Set panel characteristics
		setFocusable(true);
		setVisible(true);
		setBackground(Color.BLACK);

		//Add timer to add in the animation
		//Redraws the image every half-second
		timer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				repaint();
			}
		});

		//Add a listner to manage the keyboard controls
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				int key = event.getKeyCode();

				//Key events for game play
				if(!paused) {
					//Get player's x and y in the city grid
					//Refer to CITY class
					int pX = player.getX()/20;
					int pY = player.getY()/20;
					
					//Moving the player controls
					//Using a WASD movement layout, check for collisions with
					//map borders, buildings, and other characters
					
					//Move up with 'W' key
					if(key == KeyEvent.VK_W) {
						//Check for upper boundary
						if(pY != 0) {
							//Check for collision in outside world
							if(!inside && !insideCorp) {
								//Next to something
								if(city.map[pX][pY-1]) {
									//Check to see if next to store door
									if(city.map[pX-1][pY-1] && city.map[pX-2][pY-1] && !(city.map[pX-3][pY-1]) && city.map[pX+1][pY-1] && city.map[pX+2][pY-1] && !(city.map[pX+3][pY-1])
										|| city.map[pX-1][pY-1] && !(city.map[pX-2][pY-1]) && city.map[pX+1][pY-1] && city.map[pX+2][pY-1] && city.map[pX+3][pY-1] && !(city.map[pX+4][pY-1])) {
										//Go inside building
										inside = true;
										//Stop citizens from moving
										city.npcTimer.stop();
										//Remember where the player entered at
										oldX = player.x; player.x = 240;
										oldY = player.y; player.y = 500;
										//Determine which building was entered
										//Made sure to place every building at a different Y location
										for(int i=0; i<city.buildings.length;i++) {
											if(city.buildings[i].getY() == oldY - 100) { buildingNum = i; }
										}
									}
									//Not next to door
									else { return; }
								}
							}
							//Check inside store
							else if(inside) {
								if(city.buildings[buildingNum].map[pX][pY-1]) { return; };
							}
							//Check inside corporation
							else if(insideCorp) {
								if(city.corporation.map[pX][pY-1]) { return; }
							}
							player.moveUp();
						}
					}
					
					//Move left with 'A' key
					else if(key == KeyEvent.VK_A) {
						//Check for left boundary
						if(pX != 0) {
							if(!inside && !insideCorp) {
								if(city.map[pX-1][pY]) { return; }
							}
							//Check inside store
							else if(inside) {
								if(city.buildings[buildingNum].map[pX-1][pY]) { return; }
							}
						}
						//Check inside corporation
						else if(insideCorp) {
							//Check if leaving the corporation
							if(pX == 0 && pY == 12) { 
								insideCorp = false;
								//Start citizens moving again
								city.npcTimer.restart();
								//Remember where the player entered at
								player.x = oldX;
								player.y = oldY;
								return;
							}
							//Check other objects
							else if(pX != 0) {
								if(city.corporation.map[pX-1][pY]) { return; }
							}
						}
						if(pX != 0) { player.moveLeft(); }
					}
					
					//Move down with 'S' key
					else if(key == KeyEvent.VK_S) {
						if(!inside && !insideCorp) {
							//Check bottom boundary
							if(pY == 53) { return; }
							//Check other objects
							if(city.map[pX][pY+1]) { return; }
						}
						//Check for inside store
						else if(inside) {
							//Check to see if exiting the building
							if(pY == 24 && pX == 12) {
								player.y = oldY;
								player.x = oldX;
								inside = false;
								city.npcTimer.restart();
								return;
							} 
							//Check bottom boundary
							else if(pY == 24) { return; }
							//Check other objects
							else if(city.buildings[buildingNum].map[pX][pY+1]) { return; }
						}
						//Check for inside corporation
						else if(insideCorp) {
							//Check bottom boudnary
							if(pY == 24) { return; }
							//Check other objects
							else if(city.corporation.map[pX][pY+1]) { return; }
						}
						player.moveDown();
					}
					
					//Move right with 'D' key
					else if(key == KeyEvent.VK_D) {
						if(!inside && !insideCorp) {
							//Check right boundary
							if(pX == 94) { return; }
							//Check if entering into corporation
							else if(city.map[pX+1][pY] && city.map[pX+2][pY] && city.map[pX+1][pY+1] &&  city.map[pX+1][pY+2] && !(city.map[pX+1][pY+3])
									|| city.map[pX+1][pY] && city.map[pX+2][pY] && city.map[pX+1][pY+1] &&  city.map[pX+1][pY+2] && city.map[pX+1][pY+3]) {
								insideCorp = true;
								//Stop citizens from moving
								city.npcTimer.stop();
								//Remember where the player entered at
								oldX = player.x; player.x = 0;
								oldY = player.y; player.y = 240;
								return;
							}
							//Check other objects
							else if(city.map[pX+1][pY]) { return; }
						}
						//Check in store
						else if(inside) {
							//Check right boundary
							if(pX == 24) { return; }
							//Check other objects
							else if(city.buildings[buildingNum].map[pX+1][pY]) { return; }
						}
						//Check in corporation
						else if(insideCorp) {
							//Check right boundary
							if(pX == 24) { return; }
							//Check other objects
							else if(city.corporation.map[pX+1][pY]) { return; }
						}
						player.moveRight();
					}

					//Enter into player menu/pause screen
					else if(key == KeyEvent.VK_M) {
						timer.stop();
						city.npcTimer.stop();
						paused = true;
					}

					//Interact with people/objects by using 'SPACE'
					else if(key == KeyEvent.VK_SPACE) {
						//Check for interactions out in the city
						if(!inside && !insideCorp) {
							//Check to see if near something
							int locX, locY;

							try {
								if(city.map[pX][pY+1]) {
									locX = pX * 20;
									locY = (pY+1) * 20;
								} else if(city.map[pX][pY-1]) {
									locX = pX * 20;
									locY = (pY-1) * 20;
								} else if(city.map[pX+1][pY]) {
									locX = (pX+1) * 20;
									locY = pY * 20;
								} else if(city.map[pX-1][pY]) {
									locX = (pX-1) * 20;
									locY = pY * 20;
								}

								//Not next to anything
								else { return; }
							} catch (ArrayIndexOutOfBoundsException e) { return; }
							
							//Determine if person
							for(int i=0; i<city.citizens.length; i++) {
								if(city.citizens[i].getX() == locX && city.citizens[i].getY() == locY) {
									//Match found
									//If the person is an NPC open a dialog box
									if(city.citizens[i] instanceof NPC) {
										NPC npc = (NPC)city.citizens[i];
										city.npcTimer.stop();
										npc.talk();
										city.npcTimer.restart();
									}
								}
							}
						}
						//Next to store owner behind counter
						else if(inside) {
							//If standing next counter where shopkeeper is
							if(pX == 12 && pY == 15) {
								//Select the shopkeeper and talk start talking
								Shopkeeper shopkeep = (Shopkeeper)city.buildings[buildingNum].getOwner();
								int result = shopkeep.talk();

								switch(result) {
									//Option to buy something
									case JOptionPane.YES_OPTION:	if(shopkeep.buy(player)) {
																		//Decrease the player's preferential treatment
																		//with every other shopkeeper
																		for(int j=0; j<4; j++) {
																			if(j != buildingNum) {
																				shopkeep = (Shopkeeper)city.buildings[j].getOwner();
																				shopkeep.preferenceDown();
																			}
																		}
																	}
																	break;
									//Option to sell something
									case JOptionPane.NO_OPTION: 	if(shopkeep.sell(player)) {
																		//Decrease the player's preferential treatment
																		//with every other shopkeeper
																		for(int j=0; j<4; j++) {
																			if(j != buildingNum) {
																				shopkeep = (Shopkeeper)city.buildings[j].getOwner();
																				shopkeep.preferenceDown();
																			}
																		}
																	}
																	break;
								}
							}
							return;
						}
						//Check for next to treasure chest in corporation
						//Player must be below it
						else if(insideCorp) {
							if(pX == city.corporation.chest.getX()/20 && (pY-1) == city.corporation.chest.getY()/20) {
								Item item = city.corporation.chest.takeItem();
								player.items.add(item);
								JOptionPane.showMessageDialog(null, "Congrats! You got " + item.description);
							}
						}
					}
				}

				//Game is paused/player is in menu
				else {
					//Key events for menu
					//Exit from menu
					if(key == KeyEvent.VK_M) {
						paused = false;
						timer.restart();
						//If outside, start the characters moving again
						if(!inside && !insideCorp) {
							city.npcTimer.restart();
						}
					}

					//Controls for navigating the menu
					else if(key == KeyEvent.VK_W) {
						if(menu.selection > 0) { menu.selection--; }
					}
					else if(key == KeyEvent.VK_S) {
						if(menu.selection < player.items.size() - 1) { menu.selection++; }
					}
				}
				repaint();
			}
		});

		//Start animation timer
		timer.start();
	}
	
	public void paint(Graphics g) {
		//Render graphics for gameplay
		g2d = (Graphics2D) g;

		//Paint background black to define edges of playable area
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, WIDTH, HEIGHT);

		//Render graphics for the city
		if(!paused && !inside && !insideCorp) {
			g2d.drawImage(city.getImage(),  WIDTH/2 - player.getX(), HEIGHT/2 - player.getY(), null);
			for(int i=0; i<city.buildings.length; i++) {
				g2d.drawImage(city.buildings[i].getImage(), (WIDTH/2 - player.getX()) + city.buildings[i].getX(), (HEIGHT/2 - player.getY()) + city.buildings[i].getY(), null);
			}
			for(int i=0; i<city.citizens.length; i++) {
				g2d.drawImage(city.citizens[i].getImage(), (WIDTH/2 - player.getX()) + city.citizens[i].getX(), (HEIGHT/2 - player.getY()) + city.citizens[i].getY(), null);
			}
			g2d.drawImage(player.getImage(), WIDTH/2, HEIGHT/2, null);
		}

		//Render graphics for inside one of the stores
		else if(!paused && inside) {
			g2d.drawImage(city.buildings[buildingNum].getImageInside(), WIDTH/2 - player.getX(), HEIGHT/2 - player.getY(), null);
			g2d.drawImage(player.getImage(), WIDTH/2, HEIGHT/2, null);
			Shopkeeper owner = city.buildings[buildingNum].getOwner();
			g2d.drawImage(owner.getImage(), (WIDTH/2 - player.getX()) + owner.getX(), (HEIGHT/2 - player.getY()) + owner.getY(), null);
			
			//Check to see if player leveled up or not
			if(player.levelUp()) {
				JOptionPane.showMessageDialog(null, "Congratulations! You advanced to level: " + player.level);
			}
		}

		//Render graphics for inside corporation
		else if(!paused && insideCorp) {
			g2d.drawImage(city.corporation.getImage(), WIDTH/2 - player.getX(), HEIGHT/2 - player.getY(), null);
			g2d.drawImage(player.getImage(), WIDTH/2, HEIGHT/2, null);
			g2d.drawImage(city.corporation.chest.getImage(), (WIDTH/2 - player.getX()) + city.corporation.chest.getX(), (HEIGHT/2 - player.getY()) + city.corporation.chest.getY(), null);
			
			boolean detected = false;
			int gX, gY, los;
			int pX = player.getX()/20;
			int pY = player.getY()/20;
			
			for(int i=0; i<city.corporation.guards.length; i++) {
				gX = city.corporation.guards[i].getX();
				gY = city.corporation.guards[i].getY();
				g2d.drawImage(city.corporation.guards[i].getImage(), (WIDTH/2 - player.getX()) + gX, (HEIGHT/2 - player.getY()) + gY, null);
				//Check to see if player is detected
				gX = gX/20; gY = gY/20;
				los = city.corporation.guards[i].getLOS();
				switch (city.corporation.guards[i].getDir()) {
					//Guard is facing up
					case 0:		if(pY < gY && pY > gY - los && pX == gX) { detected = true; }
								break;
					//Guard is facing left
					case 1:		if(pX < gX && pX > gX - los && pY == gY) { detected = true; }
								break;
					//Guard is facing down
					case 2:		if(pY > gY && pY < gY + los && pX == gX) { detected = true; }
								break;
					//Guard is facing right			
					case 3:		if(pX > gX && pX < gX + los && pY == gY) { detected = true; }
								break;
								
				}
			}
			
			if(detected) {
				JOptionPane.showMessageDialog(null, "You've been caught!");
				player.credits -= 50 * player.level;
				insideCorp = false;
				//Start citizens moving again
				city.npcTimer.restart();
				//Remember where the player entered at
				player.x = oldX;
				player.y = oldY;
			}
		}

		//Render graphics for pause screen/menu
		else {
			//Draw menu background
			g2d.drawImage(menu.getImage(), 0, 0, null);

			//Display default player information (level, credits, next level credits)
			g2d.setColor(Color.WHITE);
			g2d.drawString("Level: " + player.level, 16, 24);
			g2d.drawString("Credits: " + player.credits, 16, 48);
			g2d.drawString("Next Level: " + player.nextLevel, 16, 72);

			//Display items in player's inventory and their value
			for(int i=0; i<menu.items.size(); i++) {
				if(i != menu.selection) {
					g2d.setColor(Color.WHITE);
				} else {
					//If the item is selected turn red
					g2d.setColor(Color.RED);
				}
				g2d.drawString(menu.items.get(i).description + "   " + menu.items.get(i).value, 16, (i+4)*24);
			}
		}
	}
}



//Class used to represent the city in which the game takes place
class City {
	//Properties
	Building[] buildings = new Building[4];
	Character[] citizens = new Character[20];
	Corporation corporation = new Corporation(2);
	private Image image;

	//This timer will handle the movement of the "citizens"
	Timer npcTimer;
	private static final int NPC_TIME = 1000;
	//Random number generator for the dialog selection
	//and determining the way they move on the map
	private Random rand = new Random();
	private int direction = 0;
	
	//Array used to represent every space on the grid
	//Map is 1900x1080, with 20 pixels spaces, so it is 95x54
	//False = vacant, true = occupied
	protected boolean[][] map = new boolean[95][54];
	
	//Constructor
	public City() {
		//Initialize map array to be empty
		for(int i=0; i<95; i++) {
			for(int j=0; j<54; j++) {
				map[i][j] = false;
			}
		}
		
		//Load image asset
		ImageIcon ii = new ImageIcon("CityMap.png");
		image = ii.getImage();

		//Load image again, but as a buffered image to read in color data
		try {
			BufferedImage bi = ImageIO.read(new File("CityMap.png"));
			//Create map in array using color values
			for(int i=0; i<95; i++) {
				for(int j=0; j<54; j++) {
					//If the area is white or gray, set it as occupied
					if(bi.getRGB(i*20, j*20) == 0xFFFFFFFF || bi.getRGB(i*20, j*20) == 0xFFCCCCCC) {
						map[i][j] = true;
					}
				}
			}
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Error: map was not loaded properly");
		}

		//Instantiate each store
		for(int i=0; i<4; i++) { buildings[i] = new Building(); }
		buildings[0].setX(100); buildings[0].setY(60);
		buildings[1].setX(560); buildings[1].setY(420);
		buildings[2].setX(180); buildings[2].setY(880);
		buildings[3].setX(1540); buildings[3].setY(820);
		//Add each store to map
		for(int i=0; i<4; i++) {
			int bX = buildings[i].getX()/20;
			int bY = buildings[i].getY()/20;
			//Buildings are 100x100 pixels, so they occupy 5x5 in the array
			for(int j=0; j<5; j++) {
				for(int k=0; k<5; k++) {
					map[bX+j][bY+k] = true;
				}
			}
		}

		//Different dialog options for NPCs
		String[] dialogOptions = {"Hi there!", 
		"Did you know that if you press the 'M' button you can view your inventory?",
		"You should really check out Bill's shop!",
		"Store owner's will give you better deals the more you buy from them",
		"People are selfish, I wouldn't be surprised if you got charged more if\nthe shopkeepers found out you trading with their competition",
		"I hear the big mansion has lots of cool stuff in it!",
		"I really, really like pie.",
		"Hey! Do you like music? You should check out mellowandmetal.tumblr.com"};

		//Instantiate each citizen (these are the NPCs that roam the city)
		for(int i=0; i<20; i++) {
			citizens[i] = new NPC("Bill", dialogOptions[rand.nextInt(dialogOptions.length)]);

			//Make sure the location of the NPC isn't already occupied
			//like by a building or person
			int cX, cY;
			do {
				cX = rand.nextInt(94) * 20;
				cY = rand.nextInt(53) * 20;
			} while(map[cX/20][cY/20]);

			citizens[i].setX(cX);
			citizens[i].setY(cY);
			//Set the corresponding space to occupied on the map
			map[cX/20][cY/20] = true;
		}

		//Initialize timer to control NPC movement
		npcTimer = new Timer(NPC_TIME, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				//Stop the timer to make sure next frame is ready
				npcTimer.stop();

				//The citizens x and y location in the grid
				int cX, cY;
				
				for(int i=0; i<citizens.length; i++) {
					direction = rand.nextInt(6);
					cX = citizens[i].getX()/20;
					cY = citizens[i].getY()/20;
					switch(direction) {
						//Moving upwards
						case 0:		if(cY == 0) { npcTimer.restart(); return; }
									else if(map[cX][cY-1]) { npcTimer.restart(); return; }
									map[cX][cY] = false;
									citizens[i].moveUp();
									map[cX][cY-1] = true;
									break;
						//Moving left
						case 1:		if(cX == 0) { npcTimer.restart(); return; }
									else if(map[cX-1][cY]) { npcTimer.restart(); return; }
									map[cX][cY] = false;
									citizens[i].moveLeft();
									map[cX-1][cY] = true;
									break;
						//Moving down
						case 2:		if(cY == 53) { npcTimer.restart(); return; }
									else if(map[cX][cY+1]) { npcTimer.restart(); return; }
									map[cX][cY] = false;
									citizens[i].moveDown();
									map[cX][cY+1] = true;
									break;
						//Moving right
						case 3:		if(cX == 94) { npcTimer.restart(); return; }
									else if(map[cX+1][cY]) { npcTimer.restart(); return; }
									map[cX][cY] = false;
									citizens[i].moveRight();
									map[cX+1][cY] = true;
									break;
						default:	//Do nothing
					}
				}
		
				//Restart timer to display next frame
				npcTimer.restart();
			}
		});
		
		npcTimer.start();
	}

	public Image getImage() { return image; }
}



/*
 * BUILDING CLASSES
 * STORES/CORPORATION
 */
//This class represents individual stores
class Building {
	//Properties
	private Image image, image_inside;
	private int x, y;
	private Shopkeeper owner;
	
	//Array map of interior
	protected boolean[][] map = new boolean[25][25];
	
	//Constructor
	public Building() {
		//Load building image
		ImageIcon ii = new ImageIcon("Store.png");
		image = ii.getImage();

		//Load interior image
		ii = new ImageIcon("Store_Inside.png");
		image_inside = ii.getImage();

		//Create owner
		owner = new Shopkeeper("Bill");
		owner.setX(240);
		owner.setY(260);
		
		//Instantiate map array
		for(int i=0; i<25; i++) {
			for(int j=0; j<25; j++) {
				map[i][j] = false;
			}
		}
		
		//Add owner/counter to map
		//Upper-left corner is at 9,8
		for(int i=0; i<7; i++) {
			for(int j=0; j<6; j++) {
				map[9+i][9+j] = true;
			}
		}

		x = y = 0;
	}

	//Accessors
	public void setX(int x) {this.x = x;}
	public void setY(int y) {this.y = y;}
	public int getX() {return x;}
	public int getY() {return y;}
	public Image getImage() { return image; }
	public Image getImageInside() { return image_inside; }
	public Shopkeeper getOwner() { return owner; }
}

//This class represents the big corporation building in the
//northeast corner of the map
class Corporation {
	private Image image;
	protected Guard[] guards;
	protected Chest chest;
	protected boolean[][] map = new boolean[25][25];
	protected Timer guardTimer;

	public Corporation(int numOfGuards) {
		//Load image assests
		ImageIcon ii = new ImageIcon("Corporation.png");
		image = ii.getImage();

		//Initialize map array
		//Instantiate map array
		for(int i=0; i<25; i++) {
			for(int j=0; j<25; j++) {
				map[i][j] = false;
			}
		}

		//Initialize all the guards to a random location
		guards = new Guard[numOfGuards];
		final Random rand = new Random();
		for(int i=0; i<numOfGuards; i++) {
			guards[i] = new Guard("Steve");
			guards[i].setX(rand.nextInt(24) * 20);
			guards[i].setY(rand.nextInt(24) * 20);
			map[guards[i].getX()/20][guards[i].getY()/20] = true;
		}

		//Add timer to animate guards
		//Initialize timer to control NPC movement
		guardTimer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				//Stop the timer to make sure next frame is ready
				guardTimer.stop();

				//The guards x and y location in the grid
				int gX, gY;
				
				for(int i=0; i<guards.length; i++) {
					int direction = rand.nextInt(6);
					gX = guards[i].getX()/20;
					gY = guards[i].getY()/20;
					switch(direction) {
						//Moving upwards
						case 0:		if(gY == 0) { guardTimer.restart(); return; }
									else if(map[gX][gY-1]) { guardTimer.restart(); return; }
									map[gX][gY] = false;
									guards[i].moveUp();
									map[gX][gY-1] = true;
									break;
						//Moving left
						case 1:		if(gX == 0) { guardTimer.restart(); return; }
									else if(map[gX-1][gY]) { guardTimer.restart(); return; }
									map[gX][gY] = false;
									guards[i].moveLeft();
									map[gX-1][gY] = true;
									break;
						//Moving down
						case 2:		if(gY == 24) { guardTimer.restart(); return; }
									else if(map[gX][gY+1]) { guardTimer.restart(); return; }
									map[gX][gY] = false;
									guards[i].moveDown();
									map[gX][gY+1] = true;
									break;
						//Moving right
						case 3:		if(gX == 24) { guardTimer.restart(); return; }
									else if(map[gX+1][gY]) { guardTimer.restart(); return; }
									map[gX][gY] = false;
									guards[i].moveRight();
									map[gX+1][gY] = true;
									break;
						default:	//Do nothing
					}
				}
		
				//Restart timer to display next frame
				guardTimer.restart();
			}
		});
		
		//Initialize the treasure chest
		chest = new Chest(5);
		map[chest.getX()/20][chest.getY()/20] = true;
		
		guardTimer.start();
	}

	//Accessors
	public Image getImage() { return image; }
}



/*
 *CHARACTER CLASSES
 *PLAYER, NPC, SHOPKEEPER, GUARD
 */
//This is the general character class
abstract class Character {
	//Properties
	protected Image image;
	protected int x, y; 	//Position of the character
	protected int dx, dy; 	//Amount to move by
	protected String name;
	
	//Constructor
	public Character(String name) {
		this.name = name;
		dx = dy = 20;
	}
	
	//Accessors
	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
	public int getX() { return x; }
	public int getY() { return y; }
	public Image getImage() { return image; }

	//Movement methods
	public void moveUp() { y -= dy; }
	public void moveDown() { y += dy; }
	public void moveLeft() { x -= dx; }
	public void moveRight() { x += dx; }
}

//This class represents the playable character
//that the user controls
class Player extends Character {
	//Properties
	ArrayList<Item> items;
	int credits, level, prevLevel, nextLevel;
	
	//Constructor
	public Player(String name) {
		super(name);
		ImageIcon ii = new ImageIcon(this.getClass().getResource("Player.png"));
		image = ii.getImage();
		items = new ArrayList<Item>();
		items.add(new Electronic(100));
		items.add(new Credit(100));
		x = 400;
		y = 320;
		credits = 1000;
		level = 1;
		prevLevel = 1000;
		nextLevel = prevLevel * 2;
	}

	//Check if the amount of player credits is greater
	//than the amount needed to level up
	public boolean levelUp() {
		if(credits >= nextLevel) {
			prevLevel = nextLevel;
			nextLevel = prevLevel * 2;
			level++;
			return true;
		}
		return false;
	}
}

//This class represent non-player characters
//essentially they just roam the city and the player
//can talk to them
class NPC extends Character {
	//Properties
	String dialogue;

	//Constructor
	public NPC(String name, String dialogue) {
		super(name);
		ImageIcon ii = new ImageIcon("NPC.png");
		image = ii.getImage();
		x = y = 0;
		this.dialogue = dialogue;
	}

	public void talk() {
		JOptionPane.showMessageDialog(null, dialogue, name, JOptionPane.PLAIN_MESSAGE);
	}
}

//This class represents the owners of the stores in the city
//They have the ability to buy and sell with the player
class Shopkeeper extends Character {
	//Properties
	private ArrayList<Item> inventory;
	//This determines how much a shopkeeper likes/dislikes you
	private float preference;
	//This determines what type of items a shopkeeper prefers to buy
	//0=electronics, 1=credits, 2=databanks
	private int typePref;

	//Constructor
	public Shopkeeper(String name) {
		super(name);
		ImageIcon ii = new ImageIcon("Shopkeeper.png");
		image = ii.getImage();
		x = y = 0;

		//Set a default starting inventory
		inventory = new ArrayList<Item>();
		inventory.add(new Electronic(100));
		inventory.add(new Credit(50));
		inventory.add(new Databank(100));

		//Set base preference
		preference = 1;

		//Set type preference
		Random rand = new Random();
		typePref = rand.nextInt(3);
	}

	public int talk() {
		Object[] options = {"Buy", "Sell", "No, thank you"};
		int input = JOptionPane.showOptionDialog(null, "Hi, would you like to buy or sell something?", name, JOptionPane.YES_NO_CANCEL_OPTION,
													JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		return input;
	}

	public boolean buy(Player player) {
		try {
			Object[] selection = new Object[inventory.size()];
			for(int i=0; i<selection.length; i++) {
				selection[i] = inventory.get(i).description + "    " + Math.round((2-preference) * inventory.get(i).value);
			}
			int input = (int)JOptionPane.showOptionDialog(null, "What item would you like?", name +"'s Shop", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
														null, selection, selection[0]);
			if(player.credits >= Math.round((2-preference) * inventory.get(input).value)) {
				player.credits -= (Math.round((2-preference) * inventory.get(input).value));
				player.items.add(inventory.get(input));
				inventory.remove(input);

				//Preference with that player goes up
				preferenceUp();
				return true;
			} else {
				JOptionPane.showMessageDialog(null, "Sorry, you do not have enough credits for this item", name, JOptionPane.PLAIN_MESSAGE);
				return false;
			}
		} catch (Exception e) { return false; }
	}

	public boolean sell(Player player) {
		try {
			Object[] selection = new Object[player.items.size()];
			for(int i=0; i<selection.length; i++) {
				selection[i] = player.items.get(i).description + "    " + Math.round((preference * player.items.get(i).value)/(3-preference));
			}
			int input = (int)JOptionPane.showOptionDialog(null, "What item would you like to sell?", name +"'s Shop", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
														null, selection, selection[0]);
			player.credits += (Math.round(preference * player.items.get(input).value));
			inventory.add(player.items.get(input));
			player.items.remove(input);

			//Preference with that player goes up
			preferenceUp();
			return true;
		} catch (Exception e) { return false; }
	}

	//Change how much the store owner likes or dislikes you
	public void preferenceUp() { preference += (0.1/preference); }
	public void preferenceDown() { preference -= (.1/preference); }

	//Method to add more items to store
	public void restock(int playerLevel) {
		Random rand = new Random();
		int itemType = rand.nextInt(3);
		Item itemToAdd;
		//Create a new item of a random type with random value
		//The value can be any multiple of fifty up to the player's level * 100
		switch (itemType) {
			case 0:	itemToAdd = new Electronic(rand.nextInt(playerLevel*2) * 50);
					inventory.add(itemToAdd);
					break;
			case 1:	itemToAdd = new Credit(rand.nextInt(playerLevel*2) * 50);
					inventory.add(itemToAdd);
					break;
			case 2:	itemToAdd = new Databank(rand.nextInt(playerLevel*2) * 50);
					inventory.add(itemToAdd);
					break;
		}
	}
}

//This class represents the guards inside the corporation
//building. They guard the treasure if a player is caught
//in their line of sight they incur a fine and are kicked out of the building
class Guard extends Character {
	//Properties
	private int lineOfSight, direction;

	//Constructor
	public Guard(String name) {
		super(name);
		ImageIcon ii = new ImageIcon("Guard.png");
		image = ii.getImage();
		x = y = 0;
		lineOfSight = 5;
		direction = 2;
	}
	
	//Accessors
	public int getLOS() { return lineOfSight; }
	public int getDir() { return direction; }
}



/*
 *ITEM CLASSES
 */
//General class for item characteristics
abstract class Item {
	//Properties
	protected Image image;
	int value;
	String description;
	
	//Constructor
	public Item(int value) {
		this.value = value;
	}
	public Image getImage() { return image; }
}

class Electronic extends Item {
	public Electronic(int value) {
		super(value);
		ImageIcon ii = new ImageIcon("Electronic.png");
		image = ii.getImage();
		this.description = "A cool electronic device!";
	}
}

class Credit extends Item {
	public Credit (int value) {
		super(value);
		ImageIcon ii = new ImageIcon("Credit.png");
		image = ii.getImage();
		this.description = "Credits, ya know, money";
	}
}

class Databank extends Item {
	public Databank(int value) {
		super(value);
		ImageIcon ii = new ImageIcon("Databank.png");
		image = ii.getImage();
		this.description = "A databank, this is worth some money!";
	}
}

//Class used to represent the container from
//which the player "steals" items
class Chest {
	//Properties
	private Item item;
	private int x, y;
	private Image image;
	
	//Constructor
	public Chest(int playerLevel) {
		//Load image assests
		ImageIcon ii = new ImageIcon("Chest.png");
		image = ii.getImage();
		x = 480; y = 240;
		
		//Create an item with a high value based
		//on the probability given
		double probOfValue = playerLevel * .1;
		Random rand = new Random();
		int value = 0;
		if(rand.nextDouble() < probOfValue) {
			value = playerLevel * 100;
		} else {
			value = playerLevel * 50;
		}
		
		//Determine item type
		int type = rand.nextInt(3);
		
		//Create item
		switch (type) {
			case 0: 	item = new Electronic(value);
						break;
			case 1:		item = new Credit(value);
						break;
			case 2:		item = new Databank(value);
						break;
		}
	}
	
	//Accessors
	public Item takeItem() { return item; }
	public Image getImage() { return image; }
	public int getX() { return x; }
	public int getY() { return y; }
}



/*
 *MENU CLASSES
 */
class Menu {
	//Properties
	private Image image;
	protected ArrayList<Item> items;
	protected int selection;	//Used to determine which item is currently selected

	//Constructor
	public Menu(ArrayList<Item> items) {
		//Load image assests
		ImageIcon ii = new ImageIcon("Menu.png");
		image = ii.getImage();
		
		//Initial selection is the first item, index 0
		selection = 0;
		
		//The items list is generated from the player's inventory which is passed in
		this.items = items;
	}

	//Accessors
	public Image getImage() { return image; }
}