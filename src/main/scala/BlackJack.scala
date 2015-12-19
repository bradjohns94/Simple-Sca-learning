package com.test.neural_network

import scala.util.Random
import collection.mutable.ArrayBuffer
class BlackJackGame(players: Array[Player]) {
    var deck: Array[String] = new Array[String](52) 
    var hands: Array[ArrayBuffer[String]] = new Array[ArrayBuffer[String]](players.length)
    var index: Int = 0
    val r: Random = new Random()
    val cards: Array[String] = Array("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val suits: Array[String] = Array("S", "C", "H", "D")
    // Initialize the deck
    for { card <- cards
          suit <- suits } {
        deck(index) = card + suit
        index += 1
    }
    index = 0

    
    def play: Array[Int] = {
        /* play
         * shuffle, deal, and play a game of blackjack
         */
        shuffle
        deal
        for { player <- 0 until players.length } {
            println("Player " + player + " is dealt: " + hands(player))
            while (players(player).call(hands(player)) && evalHand(hands(player)) <= 21) {
                hands(player) += getNextCard
                println("Player " + player + " hits. Their hand is now " + hands(player))
            }
            val score = evalHand(hands(player))
            println("Player " + player + " finishes with hand " + hands(player) + " with final scores " + score)
            if (score > 21) players(player).viewBust(hands(player))
        }
        var finalScores: Array[Int] = new Array[Int](players.length)
        var winner = -1
        for { player <- 0 until players.length } {
            finalScores(player) = evalHand(hands(player))
            if (finalScores(player) <= 21 && (winner == -1 || finalScores(player) > finalScores(winner))) {
                winner = player
            }
        }
        for { player <- 0 until players.length } {
            if (winner > -1) {
                players(player).viewResults(finalScores(player), finalScores(winner))
            } else {
                players(player).viewResults(finalScores(player), 22)
            }
        }
        if (winner > -1) {
            println("Player " + winner + " wins with " + finalScores(winner) + " points")
        } else {
            println("All players bust.")
        }
        finalScores
    }


    private def evalHand(hand: ArrayBuffer[String]): Int = {
        /* evalHand
         * evaluate the blackjack value of a given hand.
         * params:
         *      hand    - the hand to be evaluated
         * return: the numerical value of the hand
         */
        var value = 0
        var aceCount = 0
        for { card <- hand } {
            if (card(0).isDigit) {
                if (card(0).equals("1")) {
                    value += 10
                } else {
                    value += Integer.parseInt(card(0).toString)
                }
            } else {
                if (card(0).equals('A')) {
                    aceCount += 1
                } else {
                    value += 10
                }
            }
        }
        while (aceCount > 0) {
            aceCount -= 1
            if (value + 11 + aceCount > 21) {
                value += 1
            } else {
                value += 11
            }
        }
        value
    }


    private def deal = {
        /* deal
         * clear out each players hand and deal them 2 cards each
         */
        for { i <- 0 until players.length } {
            hands(i) = new ArrayBuffer[String]
            hands(i) += getNextCard
            hands(i) += getNextCard
        }
    }

    
    private def shuffle = {
        /* shuffle
         * Randomly swap 2 cards in the deck 1000 times and reset
         * the index
         */
        for { i <- 0 to 1000 } {
            val swap1 = r.nextInt(deck.length)
            val swap2 = r.nextInt(deck.length)
            val tmp = deck(swap1)
            deck(swap1) = deck(swap2)
            deck(swap2) = tmp
        }
        index = 0
    }


    private def getNextCard: String = {
        /* getNextCard
         * get the top card in the deck and increment the index
         */
        index += 1
        deck(index - 1)
    }
}

object main {
    def main(args: Array[String]) {
        var start = 0
        if (args(start).equals("-t")) {
            start = 2
        }
        var players: Array[Player] = new Array(args.length - start)
        val iterations = if (start == 0) 1 else Integer.parseInt(args(1))
        for {i <- start until args.length} {
            if (args(i).equals("dealer")) {
                players(i - start) = new DealerBot()
            } else if (args(i).equals("smart")) {
                players(i - start) = new SmartBot()
            } else {
                println("Invalid player type. Exiting...")
                return
            }
        }
        val game = new BlackJackGame(players)
        for { i <- 1 to iterations } {
            game.play
        }
    }
}
