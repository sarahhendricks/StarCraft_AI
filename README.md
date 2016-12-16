# StarCraft_AI
An artificial intelligence agent for playing StarCraft Brood War.

This is a final team project for CSC-568 Artificial Intelligence, created by Bex, Makaila, Fei, Natalie, Arunpreet and Sarah.

## Project requirements:
All deliverables should be available on GitHub. Required deliverables include:
 - Code that is clean, documented, and has appropriate class decomposition.
 - High-level overview of your approach
 - Explanation of all major classes, and a time-based guide as to what the agent does over time.    
 
 ## Reach Goals  
 -Complete build orders for multiple races  
 
 -Micromanaged attack strategies  
 
 -Dynamically position buildings  
 
 -Utilizing choke points for defense strategies  
 

## High Level Overview
At a very high level, the goal of our agent is to survive through the early game and into mid-late game at which point we would begin our offensive. We have two different build orders with rather different strategies. Our main approach was the creation of different functions to build buildings, units, and to attack depending on what point in the game we were. We then organized those functions based on how many units we had room for and what other buildings had been completed in order to follow our build order. 

In our Protoss vs. Protoss/Terran matchup, our priority was following our build order in order to achieve the tier two units we desired, Dark Templars. In the beginning of the game we have all of units harvesting minerals and building more probes until we build our first pylon. Then we build and assimilator and start collecting gas. From there we build our first gateway and start warping in zealots. Then we build a cybernetics core  and then a citadel so that we can build our Templar Archives to begin creation of Dark Templars, whilst creating more Zealots and some Dragoons along the way. Our end goal is to then attack the enemy’s base with our Dark Templars since they are invisible units and destroy their base and win!

## Code Structure
One of our overarching goals in the project, was to divide our build paths into a decision tree based on the enemy which we were fighting. Our goal for the code base, was to keep the majority of `matchFrame()` clear of actual code and mainly filled with function calls.
Therefore our major functions revolve around creation of buildings and units. They are as follows:
 - `protossVsPT` and `protossVsZerg` - Match frame branch splits on the opponent. This is the implementation of our build orders 
 - `collectMinerals` - Occurs throughout the games and has all non-assigned probes collect minerals
 - `collectGas` - Checks for the creation of an Assimilator (refinery) and if one such exists, assigns the gasProbe to harvest gas
 - `gasProbeCollect` - All of the pre-assigned gas probes switch from mineral to gas collecting
 - Our build functions: `buildProbes`, `buildPylons`, `buildGateway`, `buildCyber`, `buildCitadel`, `buildTemplarArchive`, `buildDrag`, `buildZealots`
 - `numType` - Counts the number of a specific type of unit.
 - `hasBuild` - Checks if a building has been initiated
 - `firstPylonPosition` - Hard code the first pylon position
 - `placement` - Spiral around pylons to the check for closest buildable positions
 - `checkSpot` - Check the radiuses of each checkposition in placement function
 - `bestGateway` - Compare the gateways and find the one with the smaller queue size.
 - `zealotAttack`, `dragoonAttack`, `templarAttack` - Send these units to attack once we hit a threshold count of units.
 - `bingAttacked` - Determine if our units are under attack.
 - `bestGateway` - Compare the gateways and find the one with the smaller queue size.
 - `findChockPoint` - Find the nearest choke point around our base.
 
## General Protoss V Protoss/Terran Timeline
The build order for Protoss V Protoss/Terran begines by building more probes and the initial pylon. After the first pylon the gateway and assimilator ar built. Along with this the production of zealots begins. After gas has begun to be collected the cybernetic core is built and multiple pylons are built. The zealots are then sent to attack and a second gateway is built. Then the production of dragoons begins and a citadel is built. During this time the zealots and dragoons are attacking and patrolling. As they are killed more will be produced based on the gateway with the smaller queue. Finally a Templar Archives is built and the Dark Templar is warped in. The Dark Templar is sent to attack and the production of zealots and dragoons continues until victory or failure.

## Protoss V Protoss/Terran Build Order
 - 4-8 / 9 - Build 4 probes (1 building, 3 gas)
 - 8 / 9 - Pylon (built by gas probe1)
 - 10 / 17 - Gateway (built by build probe)
 - 10-14 / 9 - Build 4 probes (1 building, 3 gas)
 - 10 / 17 - Assimilator (built by build probe)
 - 12-14/17 - Make 4 zealots
 - 14 / 17 - Cybernetics Core
 - 14-16/17 - Make 2 probes (1 gas, 1 mineral)
 - 16 / 17  - 3 Pylons (built by build probe)
 - 17-19 / 41 -  Make 2 zealots
 - Attack with zealots
 - 17-19 /41 -  Build gateway (built by build probe)
 - 19-24/41 - Build 5 mineral probes
 - 20-24 / 41 - Pylon (built by build probe)
 - 24-32/ 41 - Build 4 dragoons
 - 24-32 / 41 - Citadel (built by build probe)
 - 32-36 / 41 - Build dragoons
 - Attack with 2 dragoons
 - 32-36/41 - Build Templar Archives
 - 36/41 - Dark Templars
 - Attack with dark templar

## Protoss V Zerg Build Order
 - Forge Fast Expand:
 - 9/10: Pylon
 - 13/18: Forge (can be built at 13/18 if you do not send a Probe to scout)
 - 17/18: Nexus
 - 17/18: Pylon
 - 18/18: Gateway
 - 18/26: Photon Cannon
 - 18/26: Assimilator x2
 - From here, get continue Probe production and get up the Cybernetics Core. You can then progress into any of the tier 2 tech paths or timing attacks of your choice.

 - Fast Wall-Off
 - Only to be used if zerg rush spotted early in the game
 - 9/10: Pylon (this Probe goes out to scout)
 - 14/18: Forge
 - 17/18: Photon Cannon
 - Upon scouting cut Probe production to save for wall-in
 - 17/18: Gateway
 - 17/18: Pylon
 - 17/18: Gateway (used this to finish the wall-in - you may cancel before it completes and replace with a Cybernetics Core if you want).
 - Resume Probe Production
 - 18/26: Nexus
