# Simple Sca-learning

Probably one of the most underwhelming instances of machine learning around, but it's something more usable than a binary function that learns something.

This is effectively a simple text-based black jack game acting as an environment to test a scala implementation of an artificial neural network. Presumably if you're looking at this repo you're more interested in the network than the card game, so I'll try to give a reasonable explanation of both:

### The Learning Part

As a simple overview, artificial neural networks are popular structures used in machine learning in which a series of layers containing sets of "neurons" send information through a series of weighted connections and biases to get an end result which can then be attributed, right or wrong, to the results of each of those weights and biases. This is designed, on some level, to emulate the way people and animals work by sending signals between neurons in the brain. What is important to note here is that neural networks are used as a form of "supervised" learning where you can't just throw it in some random environment and hope it works, you have to train it in a specific way so that its outputs converge to the results you want. I'll talk more about how I did this for blackjack in that section.

To interact with the network here, there are two important functions to note, a constructor, and then a pretty wide set of really simple functions:

  - The NeuralNetwork class constructor: The constructor for this class is pretty dumb. It takes an array of integers specifying the number of neurons in each layer.
  - feed(): The simple function to get an output from the network without training it. Given an array of doubles the size of the input layer it returns an array for the outputs of the network.
  - train(): The function to make the network better when you know what your outputs should have been. Train takes the same array you would pass to feed as well as another array of doubles the size of the output layer for what you expected the network to spit out.
  - exportToFile(): Takes a filename and saves the weights and biases of the network to a json file so it doesn't have to re-learn the next time you run the program.
  - importFromFile(): Pretty much the opposite of exportToFile. Given a filename we set the weights and biases to the contents of the json file.
  - initWeights(): Pretty self-explanatory. It initializes all the weights in the network to random values.
  - initBiases(): The same as initWeights, but with biases.
  - setLearningRate(): Given a double value between 0 and 1, adjusts how dramatically the network will change itself.

If you don't know much about how these networks function and are interested in learning (heh.) more, I highly reccomend reading through [this online book] (it's free!)

### The Black Jack Part

The overall game structure is incredibly simple, even for a blackjack game, there's no betting, no splitting, it pretty much just exists to make sure the network worked in a way more interesting than having it figure out a binary function, however it is designed to allow for several interfaces for bots or human players.

The game gets initialed with some set of objects which inherit from the "Player" class, for me this was just a few instances of bots but this could easily be ported to be an interface for people as well if you were so inclined. From there, if you call "play", it shuffles the deck, deals the cards out, and keeps asking people if they want to hit until they're finished or bust.

The game comes equipped as is with two bots:
  
  - DealerBot: A really simple bot I used for training. It hits as long as the value of its hand is under 17.
  - SmartBot: The bot which uses a neural network. It takes in the value and size of its hand and says if it wants to hit or pass. If you run through the game without training it its pretty likely to bust every time, so if you want it to challenge you I'd reccomend training it against the dealer a few thousand times.

You can give the game as many players as you want, but I never really implemented anything to account for running out of cards, so I wouldn't reccomend adding too many players unless you want to fix that. The game takes all of this information through command line arguments, you can run the game using the following arguments:

```sh
sbt "run <bot1> <bot2> ... <botn>"
sbt "run -t <iterations> <bot1> <bot2> ... <botn>"
```

The only options for bots right now are "dealer" and "smart".

The "-t" flag lets you specify how many times you want to play if you want to run through things for training (for example, I used "-t 100000" to train my instance of smartBot).

If you want to get creative and make better bots, I'd reccomend specifying file names for various smartBot instances (or something similar) and setting them up in a round robin tournament. That way they're less likely to get overfit for training against one specific opponent and lose to everything else, but for my testing I just put it up against the dealer a few thousand times and made sure it was winning and its outputs were sensible.

### A Final Note

While I'm confident in this program's ability to work, this is still one of my first projects involving machine learning and there's a lot I don't know. If for some reason someone ends up reading this and has any tips on something I did horribly wrong or really anything I could do better I'm incredibly open to new ideas. On the flip side of things, if anyone who sees this is interested in learning more on the topic from me I'd be happy to answer any questions anyone has, so really if you have any questions, comments, or concerns about this project let me know, it was thrown together in a few days, after all.

   [this online book]: <http://neuralnetworksanddeeplearning.com/>
