package com.test.neural_network

import collection.mutable.ArrayBuffer

abstract class Player {
    def call(hand: ArrayBuffer[String]): Boolean = {
        false
    }

    def viewBust(hand: ArrayBuffer[String]) = {
    }

    def viewResults(playerScore: Int, winnerScore: Int) = {
    }

    def evalHand(hand: ArrayBuffer[String]): Int = {
        /* evalHand
         * Given a hand in a game of blackjack, returns the
         * numerical value of the hand.
         * params
         *      hand - the hand to be evaluated
         * return: the value of hand
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
}
