# Space Invaders - To Do

https://en.wikipedia.org/wiki/Space_Invaders

# Off Screen

Space Invaders
alien 1 icon = 10 points
alien 2 icon = 20 points
alien 3 icon = 40 points
red mystery UFO = ??? points

Play Space Invaders - clickable

# Graphics

X 5 alien graphics
X 1 red mystery ship graphic
X green laser base graphic (shooter)

X 4 bunkers that are "breakable"
X Alien projectile graphics
X Adjust laser base projectile graphics
X Animation when alien is struck
X Score # animation when mystery UFO is struck
X Animation when laser base is hit

# Game stats

- score (top right)
- lives (bottom right)

# Sound

X Space Invaders music (spaceinvaders1.mpeg) speeds up as invaders descend
X Mystery UFO music - loop ufo_highpitch.wav
X Sound effect for shooting - shoot.wav
X Sound effect for alien hit - invaderkilled.wav

# Game Logic

X Alien starting formation: rectangle, 5 rows, 11 columns (55 aliens)
X Player fixed at bottom of screen, moves horizontally
X Aliens move in grid left to right and shift down each time they reach an edge

X Fix alien animation speed
X Game ends when all aliens are destroyed OR if an alien reaches the player's level
X Aliens fire at random intervals, often targeting player
X Laser hitting an alien removes alien from screen
X Laser hitting the player's cannon results in loss of a life
X Bunkers disintegrate when hit by laser (alien or player)
X Points awarded for each alien shot down
X Bonus points awarded by hitting mystery UFO

- Multilevel
- When player wins a level, next level aliens start lower and move faster
- Music increases as aliens descend
