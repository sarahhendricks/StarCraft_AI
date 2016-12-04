package bot;

import java.util.Iterator;
import java.util.Set;
import jnibwapi.*;
import jnibwapi.types.*;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Example of a Java AI Client that does nothing.
 */
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.util.BWColor;

public class MinimalAIClient implements BWAPIEventListener {
        private final JNIBWAPI bwapi;

        private Set<Player> enemies;

        public static void main(String[] args) {
                new MinimalAIClient();
        }

        public MinimalAIClient() {
                bwapi = new JNIBWAPI(this, true);
                bwapi.start();
        }

        @Override
        public void connected() {}

        @Override
        public void matchStart() {
                System.out.println("Game Started");

                bwapi.enableUserInput();
                bwapi.enablePerfectInformation();
                //bwapi.setGameSpeed(0);

                // Determine what race the enemy is.
                enemies = bwapi.getEnemies();
                for (Iterator<Player> it = enemies.iterator(); it.hasNext(); ) {
                        RaceType race = it.next().getRace();
                        if (race.equals(RaceType.RaceTypes.Protoss)) {
                                System.out.println("enemy is protoss");
                        }
                        else if (race.equals(RaceType.RaceTypes.Zerg)) {
                                System.out.println("enemy is zerg");
                        }
                        else {
                                System.out.println("enemy is terran");
                        }
                }
        }

        @Override
        public void matchFrame() {
                for (Unit u : bwapi.getAllUnits()) {
                        bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
                }

                for (Unit unit : bwapi.getMyUnits()) {
                        // System.out.println("Print should be collecting minerals in this loop");
                        if (unit.getType() == UnitTypes.Protoss_Probe) {
                                //   System.out.println("GFound our probes!");
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

                //here we will create another protoss probe once we have 50 minerals
                // create new probe
                for (Unit nexus : bwapi.getMyUnits()) {
                        if (bwapi.getSelf().getMinerals() >= 60) {
                                //System.out.print("We have 50 minerals");
                                //make a new probe
                                if (bwapi.getSelf().getSupplyUsed() >= 8) {
                                        if (nexus.getType() == UnitTypes.Protoss_Nexus) {
                                                nexus.train(UnitTypes.Protoss_Probe);
                                                System.out.println("build Probe");
                                        }
                                        //unit.build(unit.getPosition(), UnitTypes.Protoss_Probe);
                                        // unit.morph(UnitTypes.Protoss_Probe);
                                        //  System.out.println("Created a probe");
                                }
                        }
                }
                for (Unit unit : bwapi.getMyUnits()) {
                        if (bwapi.getSelf().getMinerals() >= 100) {
                             //   System.out.print("We have 100 minerals to build the assimilator");
                        }
                               //need to find location of vespeon gas before building the assimilator
                                if (unit.getType() == UnitTypes.Protoss_Assimilator) {
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