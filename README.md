# Hyper Runner Tutorial
The initial hyper runner tutorial created by fngm expanded for my own learning - see https://github.com/rednblackgames/tutorial-space-platform

# What's here beyond the initial tutorial?
- talos vfx add-ons (thanks @fgnm for making this possible!!!)
-- the end portal is a talos vfx effect ... this is portal I made post the talos vfx portal tutorial
-- the projectiles used, 'bullets', are talos vfx orbs with lighting!
- "system" add-ons:
-- aliens and player are dynamically added
-- aliens have a basic ai
-- aliens and player both can shoot a 'bullet'
--- the bullet is an instance of a talos vfx orb effect with lighting!
-- depending on whom shot the 'bullet', either the player or alien dies
-- there is a concept of a basic "game loop": 
--- when player "dies" they sees a death screen & can retry the level
--- when player "wins" (they exit the level) they sees a level complete & can retry the level
-- there is a basic sound manager created which adds:
--- ability to load and play "sounds"
---- level 1 stage is music I wrote based on the "beads" library
---- lazer sound is thanks to "Kenny" media
---- other sound effects are my own

# some basics if your really new to this
- get android studio : https://developer.android.com/codelabs/basic-android-kotlin-compose-install-android-studio#0
-- this allow you to view the entire code set as the "project"
-- allows you launch the app as a desktop app
-- allows you launch the app within an "android instance"
-- provided you have the right "apple products & licenses" can create an iOS app

# how to build from command line
- 