# Hyper Runner Tutorial
The initial hyper runner tutorial created by fngm expanded for my own learning - see https://github.com/rednblackgames/tutorial-space-platform

# What's here beyond the initial tutorial?
1. talos vfx add-ons (thanks @fgnm for making this possible!!!)
	1. the end portal is a talos vfx effect ... this is a portal I made post the talos vfx portal tutorial - see https://www.youtube.com/watch?v=7deMylejPyw
	2. the projectiles used, 'bullets', are talos vfx orbs with lighting!
2. "system" add-ons:
	1. aliens and player are dynamically added
	2. aliens have a basic ai
	3. aliens and player both can shoot a 'bullet'
		1. the bullet is an instance of a talos vfx orb effect with lighting!
	4. depending on whom shot the 'bullet', either the player or alien dies
	5. there is a concept of a basic "game loop" : 
		1. when player "dies" they sees a death screen & can retry the level
		2. when player "wins" (they exit the level) they sees a level complete & can retry the level
	6. there is a basic sound manager created which adds:
		1. ability to load and play "sounds"
			1. level 1 stage is music I wrote based on the java beads library
			2. lazer sound is thanks to "Kenny" (the jesus media god of open source!)
			3. other sound effects are my own fun 

# some basics if your really new to this
1. get android studio : https://developer.android.com/codelabs/basic-android-kotlin-compose-install-android-studio#0
	1. this allow you to view the entire code set as the "project"
	2. allows you launch the app as a desktop app
	3. allows you launch the app within an "android instance"
	4. provided you have the right "apple products & licenses" can create an iOS app

# how to build from command line
1. on a *nix machine
	1. `cd` to $home
		1. execute: `./gradlew lwjgl3:distTar`
2. on a windoze machine
	1. `cd` to $home
		1. execute: `gradlew.bat lwjgl3:distTar`
3. created jar will be complied to $home\lwjgl3\build\libs\HyperRunner-0.0.1.jar
	1. to run the created jar (double clicking it from the "windows-exploder" ui can launch it if you have java 11 set as your default jvm... otherwise issue will arise)
		1. cd to $home\lwjgl3\build\libs\
			1. java -jar HyperRunner-0.0.1.jar

# JDK requirement
1. you need JDK 11 - see https://www.oracle.com/ca-en/java/technologies/javase/jdk11-archive-downloads.html
	1. create an account an download the jvm
	2. you only need to do this if you're just creating the demo from command line, otherwise, the "java" JDK stuff is taken care of by android studio if you more or less know what your doing within the ide

