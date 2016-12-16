# StarCraft_AI
An artificial intelligence agent for playing StarCraft Brood War.

This is a final team project for CSC-568 Artificial Intelligence, created by Bex, Makaila, Fei, Natalie, Arunpreet and Sarah.

## Project requirements:
All deliverables should be available on GitHub. Required deliverables include:
 - Code that is clean, documented, and has appropriate class decomposition.
 - High-level overview of your approach (ballpark of 500 words in the README in the base of your repository).
 - Technical summary of your agent including a system diagram, explanation of all major classes, and a time-based guide as to what the agent does over time (also in the README).

## High Level Overview
At a very high level, the goal of our agent is to survive through the early game and into mid-late game at which point we would begin our offensive. We have two different build orders with rather different strategies. Our main approach was the creation of different functions to build buildings, units, and to attack depending on what point in the game we were. We then organized those functions based on how many units we had room for and what other buildings had been completed in order to follow our build order. 

In our Protoss vs. Protoss/Terran matchup, our priority was following our build order in order to achieve the tier two units we desired, Dark Templars. In the beginning of the game we have all of units harvesting minerals and building more probes until we build our first pylon. Then we build and assimilator and start collecting gas. From there we build our first gateway and start warping in zealots. Then we build a cybernetics core  and then a citadel so that we can build our Templar Archives to begin creation of Dark Templars, whilst creating more Zealots and some Dragoons along the way. Our end goal is to then attack the enemy’s base with our Dark Templars since they are invisible units and destroy their base and win!

## Code Structure
 One of our overarching goals in the project, was to divide our build paths into a decision tree based on the enemy which we were fighting. Our goal for the code base, was to keep the majority of `matchFrame()` clear of actual code and mainly filled with function calls.
 Therefore our major functions revolve around creation of buildings and units. They are as follows:
 - `collectMinerals` - Occurs throughout the games and has all non-assigned probes collect minerals
 - `collectGas` - Checks for the creation of an Assimilator (refinery) and if one such exists, assigns the gasProbe to harvest gas
 - `buildProbes` - Creates Probes, mainly used at the beginning of the game, but used throughout the build order
 - `buildPylons` - Builds Pylons based on positioning functions
 - `buildGateway` - Builds a Gateway based on positioning functions around a Pylon
 - `buildCyber` - Builds a Cybernetics Core
 - `buildCitadel` - Builds a Citadel
 - `buildTemplarArchive` - Builds a Templar Archive
 - `buildDrag` - Builds Dragoons
 - `buildZealots` - Builds Zealots
 - `pylonRadius` - Checks if buildings are within the region of a Pylon
 - `placement` - Spiral around pylons to the check for closest buildable positions
 - `checkSpot` - Check the radiuses of each checkposition in placement function

## Protoss V Protoss/Terran Build Order
 - 4-8 / 9 - Build 4 probes
 - 1 building
 - 1 mineral
 - 2 gas
 - 8 / 9 - Pylon (built by gas probe1)
 - 8-10 / 17 - Build 2 probes
 - 10 / 17 - Gateway (built by build probe)
 - 10-12/17 - Make 2 zealots
 - 12 / 17 - Assimilator (built by build probe)
 - 12-14/17 - Make 2 zealots
 - 14 / 17 - Cybernetics Core
 - 14-16/17 - Make 2 probes
 - 1 gas
 - 1 mineral
 - 16 / 17  - Pylon (built by build probe)
 - 17-19 / 25 -  Make 2 zealots
 - When zealot counter = 6 Attack with 6 zealots
 - 17-19 /25 -  Build gateway (built by build probe)
 - 19-24/25 - Build 5 mineral probes
 - 20-24 / 25 - Pylon (built by build probe)
 - 24-32/ 33 - Build 4 dragoons
 - 24-32 / 33 - Citadel (built by build probe)
 - Attack with 4 dragoons
 - 24 / 33 - Pylon (built by build probe)
 - 32-36 - Build 2 dragoons
 - Attack with 2 dragoons
 - 32-36/41 - Build Templar Archives
 - 36/41 - Dark Templars
 - Attack with dark templar
 - 38-41/41 - Pylon
 - 41-45/49 - Build 2 dragoons
 - 45-49/49 - Build 2 dark templars
 - Attack with 2 dragoons and 2 dark templars
 - If still alive repeat Pylon and build 4 dragoons….

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
