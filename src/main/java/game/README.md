# README

An iteration of the classic 1978 arcade game, Space Invaders! Built with Java Swing, featuring animated aliens, a mystery UFO, and destructible bunkers. Extended from Asteroids base code.

![Gameplay Screenshot](spaceInvadersScreenshot.png)

## Game Play

The goal is to defeat waves of descending aliens by evading their bullets and shooting back with a fixed laser cannon. Stop the invaders while earning as many points as possible!

- **Alien Formation**: 55 aliens in 5 rows with different point values (10-40 points)
- **Mystery UFO**: Random appearances worth 50-300 points
- **Destructible Bunkers**: 4 protective barriers that degrade when hit by player or aliens
- **Progressive Difficulty**: (To-do) - Each level increases speed and lowers starting position of aliens
- **Sound Effects**: Background music, laser fire, explosions, and UFO sounds

### Controls

- **Arrow Keys**: Move laser cannon left/right
- **Spacebar**: Fire
- **S**: Start game | **P**: Pause | **M**: Mute Music | **Q**: Quit

## How to Run

Run the `main` method in [`Game.java`](mvc/controller/Game.java):

## Technical Architecture

Built on MVC pattern with provided base-code architecture:

- **Model**: Game objects:
  - [`Alien`](mvc/model/Alien.java)
  - [`AlienBullet`](mvc/model/AlienBullet.java)
  - [`AlienDebris`](mvc/model/AlienDebris.java)
  - [`LaserCannon`](mvc/model/LaserCannon.java)
  - [`MysteryUFO`](mvc/model/MysteryUFO.java)
  - [`Bunker`](mvc/model/Bunker.java)
  - [`Brick`](mvc/model/Brick.java)
  - [`Bullet`](mvc/model/Bullet.java)
  - [`UFOScoreDebris`](mvc/model/UFOScoreDebris.java)
- **View**: Rendering ([`GamePanel`](mvc/view/GamePanel.java), [`GameFrame`](mvc/view/GameFrame.java))
- **Controller**: Game logic ([`Game`](mvc/controller/Game.java), [`CommandCenter`](mvc/controller/CommandCenter.java))

## Project Reflection

This was my first time building a game, and the biggest challenge was understanding the fundamental architecture of how games work -- the continuous game loop, state management, and constant redrawing.

**Specific Challenges**:

- **Alien Synchronization**: Coordinating 55 aliens required tracking shared state (direction, animation frame, etc). In the real Space Invaders, music and alien animation speed increases with each drop-down. I attempted to complete this but was unable to in the allotted timeframe.
- **Collision Detection**: Understanding collision detection and implementing it for different scenarios
- **Bunker Degradation System**: Creating destructible Bunker structures from individual Brick components that degrade realistically. Implementing damage radius logic to remove neighboring bricks when hit, mimicking the real Space Invaders game.
- **Respawn Protection**: Implementing invulnerability frames in [`LaserCannon`](mvc/model/LaserCannon.java) to prevent unfair instant deaths

## Assets & Sources

**Inspiration**: [Free Invaders](https://freeinvaders.org) | [Wikipedia](https://en.wikipedia.org/wiki/Space_Invaders)

**Sound Effects**: [Classic Gaming - Space Invaders Sounds](https://classicgaming.cc/classics/space-invaders/sounds)

**Graphics**:

- Alien sprites: [8-bit Aliens Icons Set](https://pngtree.com/freepng/space-invaders8bit-aliens-icons-set-danger-attack-color-vector_9438715.html)
- UFO: [Space Invaders UFO Sprite](https://www.clipartmax.com/middle/m2i8i8G6G6K9m2Z5_space-invaders-ufo-shaped-sticker-space-invaders-sprites-png/)
- Explosion: [Explosion Icon](https://thenounproject.com/icon/explosion-900185/)

**Author**: Leah Perri
**Course**: MPCS 51036 - Java Programming  
**Institution**: University of Chicago  
**Quarter**: Winter 2025
