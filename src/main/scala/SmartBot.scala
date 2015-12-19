package com.test.neural_network

import collection.mutable.ArrayBuffer
import java.io.File

class SmartBot extends Player {
    var network: NeuralNetwork = new NeuralNetwork(Array(2, 1))
    var lastCall: ArrayBuffer[String] = null

    val check = new File("blackjack.json")
    if (check.exists()) {
        network.importFromFile("blackjack.json")
    } else {
        network.initWeights
        network.initBiases
    }

    override def call(hand: ArrayBuffer[String]): Boolean = {
        val handVal = evalHand(hand)
        if (lastCall != null) {
            // We hit last time and didn't bust
            val lastEval = evalHand(lastCall)
            network.train(Array(lastEval.toDouble, lastCall.length.toDouble), Array(0.9))
        }
        lastCall = hand
        val res = network.feed(Array(handVal.toDouble, hand.length.toDouble))
        res(0)> 0.5
    }

    override def viewResults(playerScore: Int, winnerScore: Int) = {
        val lastEval = evalHand(lastCall)
        if (playerScore != winnerScore) {
            network.train(Array(lastEval.toDouble, lastCall.length.toDouble), Array(0.1))
        } else {
            network.train(Array(lastEval.toDouble, lastCall.length.toDouble), Array(0.9))
        }
        network.exportToFile("blackjack.json")
    }
}
