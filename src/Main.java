import javax.swing.JFrame;

public class Main {
	public static final int FRAME_WIDTH = 1024;
	public static final int FRAME_HEIGHT = 652;
	public static Player[] player = new Player[2];

    public static void main(String[] args) {
    		Display d = new Display(FRAME_WIDTH, FRAME_HEIGHT, player);
    		d.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		d.setVisible(true);
    }
}