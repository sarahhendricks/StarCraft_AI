package bot;

/**
 * Example of a Java AI Client that does nothing.
 */
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.util.BWColor;

import java.util.HashSet;

public class MinimalAIClient implements BWAPIEventListener {
    /** reference to JNI-BWAPI */
    private final JNIBWAPI bwapi;

    /** used for mineral splits */
    private final HashSet<Unit> claimedMinerals = new HashSet<>();

    /** have drone 5 been morphed */
    private boolean morphedDrone;

    /** the drone that has been assigned to building a pool */
    private Unit poolDrone;

    /** when should the next overlord be spawned? */
    private int supplyCap;

    /**
     * Create a Java AI.
     */
	public static void main(String[] args) {
		new MinimalAIClient();
	}

    /**
     * Instantiates the JNI-BWAPI interface and connects to BWAPI.
     */
	public MinimalAIClient() {
		bwapi = new JNIBWAPI(this, false);
		bwapi.start();
	}

    /**
     * Connection to BWAPI established.
     */
	@Override
	public void connected() {
        System.out.println("Connected");
    }

    /**
     * Called at the beginning of a game.
     */
	@Override
	public void matchStart() {
        System.out.println("Game Started");

        bwapi.enableUserInput();
        bwapi.enablePerfectInformation();
        bwapi.setGameSpeed(0);

        // reset agent state
        claimedMinerals.clear();
        morphedDrone = false;
        poolDrone = null;
        supplyCap = 0;
    }

    /**
     * Called each game cycle.
     */
	@Override
	public void matchFrame() {
		for (Unit u : bwapi.getAllUnits()) {
			bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
		}
	}
	
	@Override
	public void keyPressed(int keyCode) {}
	@Override
	public void matchEnd(boolean winner) {}
	@Override
	public void sendText(String text) {}
	@Override
	public void receiveText(String text) {}
	@Override
	public void nukeDetect(Position p) {}
	@Override
	public void nukeDetect() {}
	@Override
	public void playerLeft(int playerID) {}
	@Override
	public void unitCreate(int unitID) {}
	@Override
	public void unitDestroy(int unitID) {}
	@Override
	public void unitDiscover(int unitID) {}
	@Override
	public void unitEvade(int unitID) {}
	@Override
	public void unitHide(int unitID) {}
	@Override
	public void unitMorph(int unitID) {}
	@Override
	public void unitShow(int unitID) {}
	@Override
	public void unitRenegade(int unitID) {}
	@Override
	public void saveGame(String gameName) {}
	@Override
	public void unitComplete(int unitID) {}
	@Override
	public void playerDropped(int playerID) {}
}
