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

        private RaceType enemy;

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
                                enemy = RaceType.RaceTypes.Protoss;
                        }
                        else if (race.equals(RaceType.RaceTypes.Zerg)) {
                                System.out.println("enemy is zerg");
                                enemy = RaceType.RaceTypes.Zerg;

                        }
                        else {
                                System.out.println("enemy is terran");
                                enemy = RaceType.RaceTypes.Terran;
                        }
                }
        }

        /*
         * The game strategy for Terran enemies.
         */
        private void protossVsTerran() {
        }

        /*
         * The game strategy for Zerg enemies.
         */
        private void protossVsZerg() {
                /* build order
                 * 8 - Pylon at Natural Expansion[1]
                 * 10 - Forge[2]
                 * 13 - two Photon Cannons[3]
                 * 15 - Pylon[4]
                 * 18 - Nexus
                 * 18 - Gateway [5]
                 * 20 - Assimilator [6]
                 * 22 - Cybernetics Core
                 * 25/26 - Assimilator[7]
                 * @ 100% Cybernetics Core - Dragoon[8]
                 * 100% Cybernetics Core - Stargate
                 * @ 100 Gas - Citadel of Adun[9]
                 * @ 100 Gas - Corsair
                 * @ 100 Gas - +1 Ground Attack
                 * @ 200 Gas - Templar Archives
                 * 3 Gateways
                 * @ 100% Templar Archives - 2 Archons[10]
                 * @ 2 Archons - Zealot Speedupgrade
                 * @ ~95% +1 Attack Upgrade - Army moves out
                 */
        }

        /*
         * The game strategy for Protoss enemies.
         */
        private void protossVsProtoss() {
        }

        /*
         * This runs every game frame (multiple times a second!!)
         */
        @Override
        public void matchFrame() {
                for (Unit u : bwapi.getAllUnits()) {
                        bwapi.drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
                }

                // Collecting minerals
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

                // branching off into our enemy-specific games
                if (enemy == RaceType.RaceTypes.Protoss) {
                        protossVsProtoss();
                }
                else if (enemy == RaceType.RaceTypes.Terran) {
                        protossVsTerran();
                }
                else {
                        protossVsZerg();
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