import java.util.List;
import java.util.ArrayList;

public class Computer extends Player{
	private List<Point> hits = new ArrayList<Point>();
	private List<Point> spaces = new ArrayList<Point>();
	private List<Point> randSpaces = new ArrayList<Point>(); //random spaces for firing on
	private List<List<Point>> backup = new ArrayList<List<Point>>(); //storage for backup sets of ship hits for special case (at bottom of class)

	public Computer(Grid grid, List<Point> x){
		super(grid);
		hits = x;
		/* When no ships have been hit, the computer will choose an optimal point from
		 * a set of points in a checkerboard pattern. It does this because of the fact
		 * that all ships have to occupy both "checkerboards", and that firing a shot 
		 * right next to another is less than ideal because if there were a ship there,
		 * firing in the next location over would check for more possible ship positions.
		 */
		int z = (int)(Math.random() * 2); //this variable toggles which checkerboard pattern is chosen

		for (int i = 0; i < 10; i += 2){
			for (int j = z; j < 10; j += 2){
				randSpaces.add(new Point(i, j));
			}
		}
		for (int i = 1; i < 10; i += 2){
			for (int j = 1 - z; j < 10; j += 2){
				randSpaces.add(new Point(i, j));
			}
		}
	}
	
	public void fire(Grid grid){
		List<Ship> ships = grid.getShips();
		//computes the length of the smallest human ship still alive
		int minLen = 6;
		for (int i = 0; i < ships.size(); i++){
			if (ships.get(i).length() < minLen){
				minLen = ships.get(i).length();
			}
		}

		if (hits.isEmpty() && backup.isEmpty()){ //no ships are partially hit
			/* finds an optimal point from checkerboard pattern.
			 * minLen is passed to optimal() so that all calculated points are optimal 
			 * with respect to the smallest ship still alive.
			 */
			Point p = optimal(grid, randSpaces, minLen);
			grid.fire(p);

			//removes the point from the possible points to prevent refiring
			randSpaces.remove(p);

			if (grid.getFired(p.getX(), p.getY()) == 1){ //updates hits if necessary
				hits.add(p);
			}

		} else if (hits.isEmpty() && !backup.isEmpty()){ //one set of ship hits stored in backup is used to continue fire
			hits = backup.remove(0);
			fire(grid);
		} else { //a ship has been partially hit
			spaces = getNeighbors(hits, grid, minLen); //finds best neighboring points the ship would be occupying
			int x = (int)(Math.random() * spaces.size()); //chooses one randomly
			grid.fire(spaces.get(x)); //fires on it

			//if the hit is successful and the ship lives
			if (grid.getFired(spaces.get(x).getX(), spaces.get(x).getY()) == 1){ 
				hits.add(spaces.get(x)); //updates hits
				for (int i = 0; i < randSpaces.size(); i++){ //removes point from random points available to be chosen
					if (randSpaces.get(i).equals(spaces.get(x))){
						randSpaces.remove(i);
					}
				}
			}
			//if the hit is successful and the ship dies
			if (grid.getFired(spaces.get(x).getX(), spaces.get(x).getY()) == 3){ 
				for (int i = hits.size() - 1; i >= 0; i-- ){
					if (grid.getFired(hits.get(i).getX(), hits.get(i).getY()) == 3){ //removes all hits of the sunken ship
						hits.remove(i);
					}
				}
				for (int i = 0; i < randSpaces.size(); i++){ //removes point from random points available to be chosen
					if (randSpaces.get(i).equals(spaces.get(x))){
						randSpaces.remove(i);
					}
				}
			}
		}
	}
	/*  gives a point a "score" counting how many ship orientations contain it:
	 *  the higher the score, the better the point is for firing.
	 */
	public int pointScore(Grid g, Point p, int len){ 
		return verScore(g, p, len) + horScore(g, p, len);
	}
	//computes maximum number of ship orientations of length len in grid g horizontally containing Point p
	public int horScore(Grid g, Point p, int len){ 
		int x = p.getX(), y = p.getY();
		int x1 = x, x2 = x;
		int score = 0;

		while (g.isValid(new Point(x1 + 1, y)) && g.getFired(x1 + 1, y) == 0 && (x1 + 1) - x < len){ 
			x1++; 
		}
		while (g.isValid(new Point(x2 - 1, y)) && g.getFired(x2 - 1, y) == 0 && x - (x2 - 1) < len){
			x2--;
		}
		
		for (int i = x2 + len - 1; i <= x1; i++){
			score++;
		}
		return score;
	}

	/*computes maximum number of ship orientations of length len in grid g vertically containing p
	 * (analogous to horScore)
	 */
	public int verScore(Grid g, Point p, int len){ 
		int x = p.getX(), y = p.getY();
		int y1 = y, y2 = y;
		int score = 0;

		while (g.isValid(new Point(x, y1 + 1)) && g.getFired(x, y1 + 1) == 0 && (y1 + 1) - y < len){ 
			y1++; 
		}
		while (g.isValid(new Point(x, y2 - 1)) && g.getFired(x, y2 - 1) == 0 && y - (y2 - 1) < len){
			y2--;
		}
		
		for (int i = y2 + len - 1; i <= y1; i++){
			score++;
		}
		return score;
	}

	//finds point most likely containing a ship of length len out of a set of points on grid g
	public Point optimal(Grid g, List<Point> points, int len) { 
		List<Integer> scores = new ArrayList<Integer>();
		/*  initializes an array of scores corresponding to each point in List<Point> points
		 */
		for (int i = 0; i < points.size(); i++){
			scores.add(pointScore(g, points.get(i), len)); //adds computed score to score list
		}
		
		List<List<Point>> scoreTiers = new ArrayList<List<Point>>(); //data structure organizing points by their "tiers" or scores
		for (int i = 0; i <= 2 * len; i++){ 
			scoreTiers.add(new ArrayList<Point>()); //makes a new list in scoreTiers for each possible score
			for (int j = 0; j < points.size(); j++){
				if (scores.get(j) == i){
					scoreTiers.get(i).add(points.get(j)); //adds points with the corresponding score to the current scoreTier list
				}
			}
		}
		/* returns the highest-scoring non-empty tier
		 * (the point(s) inside have the highest score(s) of all possible points)
		 */
		return Utilities.weightOptimal(scoreTiers); 
	}
	//returns unfired squares to fire on next, based on spaces of ship already hit
	public List<Point> getNeighbors(List<Point> points, Grid g, int len){ 
		List<Point> x1 = new ArrayList<Point>();
		if (points.size() == 1){ //if only one hit exists
			int x = points.get(0).getX();
			int y = points.get(0).getY();
			
			//checks if ship is more likely to be up-down or left-right
			if (horScore(g, new Point(x, y), len) > verScore(g, new Point(x, y), len)){ 
				//more likely left-right
				x1.add(new Point(x + 1, y));		
				x1.add(new Point(x - 1, y));
			} else if (horScore(g, new Point(x, y), len) <= verScore(g, new Point(x, y), len)){ 
				//more likely up-down
				x1.add(new Point(x, y + 1));
				x1.add(new Point(x, y - 1));
			}
			
			// when both orientations are equally probable, add all points
			if (x1.isEmpty()){ 
				x1.add(new Point(x + 1, y));
				x1.add(new Point(x - 1, y));	
				x1.add(new Point(x, y + 1));
				x1.add(new Point(x, y - 1)); 
			}
			
		} else { //if multiple hits exist
			if (points.get(0).getX() == points.get(1).getX()){ //ship is oriented up-down 
				int minI = 0;
				int min = g.getRows();
				for (int i = 0; i < points.size(); i++){
					if (points.get(i).getY() < min){
						min = points.get(i).getY();
						minI = i;
					}
				}
				//adds "bottom" side of ship to target list
				x1.add(new Point(points.get(minI).getX(), points.get(minI).getY() - 1)); 

				int maxI = 0;
				int max = 0;
				for (int i = 0; i < points.size(); i++){
					if (points.get(i).getY() > max){
						max = points.get(i).getY();
						maxI = i;
					}
				}
				//adds "top" side to target list
				x1.add(new Point(points.get(maxI).getX(), points.get(maxI).getY() + 1)); 

			} else { //ship is oriented left-right
				int minI = 0;
				int min = g.getColumns();
				for (int i = 0; i < points.size(); i++){
					if (points.get(i).getX() < min){
						min = points.get(i).getX();
						minI = i;
					}
				}
				//adds "left" end of ship to target list
				x1.add(new Point(points.get(minI).getX() - 1, points.get(minI).getY())); 

				int maxI = 0;
				int max = 0;
				for (int i = 0; i < points.size(); i++){
					if (points.get(i).getX() > max){
						max = points.get(i).getX();
						maxI = i;
					}
				}
				//adds "right" end of ship to target list
				x1.add(new Point(points.get(maxI).getX() + 1, points.get(maxI).getY())); 
			}
		}

		List<Point> x2 = new ArrayList<Point>();
		for (Point p : x1){
			if (g.isValid(p)){
				x2.add(p); //filters above neighbors for valid points
			}
		}

		List<Point> result = new ArrayList<Point>();
		for (Point x : x2){
			if (g.getFired(x.getX(), x.getY()) == 0){
				result.add(x); //filters valid points for unfired squares
			}
		}
		
		/* Case where no neighbors are found since two (or more) ships are side by side.
		 * The computer hits one square on each one and thinks that it has hit
		 * one continuous ship when it has actually hit two (or more). When it misses on 
		 * both sides of this "ship", it has no results returned for spaces to fire on.
		 */
		if (result.size() == 0) {
			/* All but one of the sets of ship hits are transferred to a "backup".
			 * When the one set of ship hits left is killed and the backup contains
			 * more hit data, another set of hits will be used from the backup.
			 */
			for (int i = 0; i < hits.size() - 1; i++){ 
				List<Point> p = new ArrayList<Point>();
				p.add(hits.get(i));
				backup.add(p);
			}
			//the remaining set of ship hits is taken as normal
			Point p = hits.get(hits.size() - 1);
			hits.clear();
			hits.add(p);
			return getNeighbors(hits, g, len);
		} else return result;
	}
}
