package com.test.neural_network

import collection.mutable.ArrayBuffer

class DealerBot extends Player {
    override def call(hand: ArrayBuffer[String]): Boolean = {
        evalHand(hand) < 17
    }
}
