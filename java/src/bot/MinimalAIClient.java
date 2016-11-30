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
        private final JNIBWAPI bwapi;

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
        }

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