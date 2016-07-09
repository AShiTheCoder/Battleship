import java.util.ArrayList;
import java.util.List;

public class Utilities {
	public static boolean between(double a, double b, double c) {
		return (a >= b) && (a <= c);
	}
	
	/* Returns a random point based on each point's probability.
	 * In the parameter *list*, the index of a list in *list* corresponds to 
	 * the number of ship orientations each point in the list could occupy.
	 * 
	 * The purpose of this method is to pick a random point to fire upon with respect to
	 * how "optimal" each set of points is, instead of always picking the most optimal point.
	 * This prevents the computer from being outsmarted by strategies such as placing all
	 * human ships on the borders of the grid, which only occupies "suboptimal" points and
	 * avoids the computer's firing until all other better points have been exhausted.
	 * With a random scheme such as this, the computer is still more likely
	 * to fire on better points that could contain more ships but can also still
	 * occasionally fire on the border or other suboptimal locations.
	 */
	public static Point weightOptimal(List<List<Point>> list){
		List<Point> allPoints = new ArrayList<Point>();
		List<Integer> markers = new ArrayList<Integer>();
		
		for (int i = 0; i < list.size(); i++){
			if (!list.get(i).isEmpty()){
				for (Point p : list.get(i)){
					for (int j = 0; j < i; j++){
						allPoints.add(p);
						markers.add(i);
					}
				}
			}
		}
		int z = (int)(Math.random() * allPoints.size());
		return allPoints.get(z);
	}
}