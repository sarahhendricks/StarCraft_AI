package bot;

import java.util.HashSet;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType;
import jnibwapi.types.UpgradeType.UpgradeTypes;

/**
 * Example of a Java AI Client that does nothing.
 */
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.util.BWColor;

public class MinimalAIClient implements BWAPIEventListener {
        /** reference to JNI-BWAPI */
        private final JNIBWAPI bwapi;

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
                bwapi = new JNIBWAPI(this, true);
                bwapi.start();
        }

		/**
		 * Connection to BWAPI established.
		 */
		@Override
        public void connected() {System.out.println("Connected to our new client");}

        /**
         * Called at the beginning of a game.
         */
        @Override
        public void matchStart() {
                System.out.println("Game Started");

                bwapi.enableUserInput();
                bwapi.enablePerfectInformation();
                //bwapi.setGameSpeed(0);
        }

        /**
         * Called each game cycle.
         */
        @Override
        public void matchFrame() {
                for (Unit u : bwapi.getAllUnits()) {
                        bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
                }

                for (Unit unit : bwapi.getMyUnits()) {
                        System.out.println("Print should be collecting minerals in this loop");
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                System.out.println("GFound our probes!");
                                // You can use referential equality for units, too
                                if (unit.isIdle()) {
                                        for (Unit minerals : bwapi.getNeutralUnits()) {
                                                if (minerals.getType().isMineralField()) {
                                                        //& !claimedMinerals.contains(minerals)
                                                        double distance = unit.getDistance(minerals);

                                                        if (distance < 300) {
                                                                unit.rightClick(minerals, false);
                                                                //claimedMinerals.add(minerals);
                                                                break;
                                                        }
                                                }
                                        }
                                }
                        }
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