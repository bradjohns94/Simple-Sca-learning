package com.test.neural_network

import scala.util.Random
import java.io._
import java.util.Scanner
import play.api.libs.json._

class NeuralNetwork(structure: Array[Int]) {
    var layout: Array[Int] = structure
    var weights = collection.mutable.Map[Int, collection.mutable.Map[Int, collection.mutable.Map[Int, Double]]]()
    var bias = collection.mutable.Map[Int, collection.mutable.Map[Int, Double]]()
    var learningRate = 0.5
    val r = new Random()

    def feed(inputs: Array[Double]): Array[Double] = {
        /* feed
         * Given some set of inputs, feed the values through the network and return the output
         * without reweighting
         * params: 
         *      inputs - A series of floating point inputs into the network - 1 for each input neuron
         * return: the output of the network
         */
        if (inputs.length != layout(0)) {
            throw new IllegalArgumentException("Inputs did not match size of input layer.")
        }
        var activations: Array[Array[Double]] = new Array[Array[Double]](layout.length)
        var z: Array[Array[Double]] = new Array[Array[Double]](layout.length)
        for { i <- 0 until layout.length } {
            activations(i) = new Array[Double](layout(i))
            z(i) = new Array[Double](layout(i))
        }
        feedHelper(inputs, layout.length - 1, activations, z)
        activations(layout.length - 1)
    }


    def train(inputs: Array[Double], expected: Array[Double]) = {
        /** train
         * Given a set of inputs and a set of expected outputs this function
         * trains the neural network using the backpropagation algorithm:
         * Steps:
         *      1) Get the activations and inputs of each neuron in the network
         *      2) Calculate the cost of error of the output layer
         *      3) Determine the error of the output layer
         *      4) Determine the error of each hidden layer
         *      5) Calculate how much each weight and bias attribute to the error
         *      6) Adjust weights and biases using gradient descent
         * params:
         *      inputs      - The set of inputs into the network
         *      expected    - The set of expected outputs from the network
         */
        if (inputs.length != layout(0)) {
            throw new IllegalArgumentException("Inputs did not match size of input layer.")
        }
        if (expected.length != layout.last) {
            throw new IllegalArgumentException("Expected outputs did not match size of output layer.")
        }
        // Get the activations of each neuron in the network and initialize the error variable
        var activations: Array[Array[Double]] = new Array[Array[Double]](layout.length)
        var z: Array[Array[Double]] = new Array[Array[Double]](layout.length)
        var error: Array[Array[Double]] = new Array[Array[Double]](layout.length)
        for { i <- 0 until layout.length } {
            activations(i) = new Array[Double](layout(i))
            z(i) = new Array[Double](layout(i))
            error(i) = new Array[Double](layout(i))
        }
        feedHelper(inputs, layout.length - 1, activations, z)
        // Calculate the cost of error in the output layer
        var cost = 0.0
        for { i <- 0 until layout.last } {
            val diff = activations(layout.length - 1)(i) - expected(i)
            cost += math.pow(diff, 2)
        }
        // Cost = (1/2n) * Sum of squared differences
        cost *= 1 / (2 * layout.last)
        // Determine the error of the output layer
        for { i <- 0 until layout.last } {
            // Error_L = dC/da * sig'(z)
            error(layout.length - 1)(i) = activations(layout.length - 1)(i) - expected(i)
            error(layout.length - 1)(i) *= sigmoidPrime(z(layout.length - 1)(i))
        }
        // Determine the error of each hidden layer
        for { layer <- layout.length - 2 until 0 by -1 } {
            // Error_l = w_l_transposed * Error_l+1 * sig'(z)
            val transposedWeights = getTransposedWeights(layer + 1)
            val factoredError = new Array[Array[Double]](layout(layer + 1))
            for { i <- 0 until layout(layer + 1) } {
                factoredError(i) = Array(error(layer + 1)(i))
            }
            val product = matrixMultiply(transposedWeights, factoredError)
            for { neuron <- 0 until layout(layer) } {
                error(layer)(neuron) = product(neuron)(0)
                error(layer)(neuron) *= sigmoidPrime(z(layer)(neuron))
            }
        }
        // Determine how much each weight/bias attributes to cost and adjust their values
        for { layer <- 1 until layout.length
              neuron <- 0 until layout(layer) } {
            val dCdb = error(layer)(neuron) // Change in cost in respect to bias
            bias(layer)(neuron) -= learningRate * dCdb
            for { input <- 0 until layout(layer - 1) } {
                // Change in cost in respect to weight = activation in * error out
                val dCdw = activations(layer - 1)(input) * error(layer)(neuron)
                weights(layer)(neuron)(input) -= learningRate * dCdw
            }
        }
    }


    def initWeights = {
        /* initWeights
         * Initializes the weights in the network to random floating
         * values between 0 and 1
         */
        for { layer <- 1 until layout.length } {
            weights += (layer -> collection.mutable.Map[Int, collection.mutable.Map[Int, Double]]())
            for { output <- 0 until layout(layer) 
                  input <- 0 until layout(layer - 1) } {
                if (!weights(layer).contains(output)) {
                    weights(layer) += (output -> collection.mutable.Map[Int, Double]())
                }
                weights(layer)(output) += (input -> r.nextDouble())
            }
        }
    }


    def initBiases = {
        /* initBiases
         * Initializes the biases in the network to random floating values
         * between 0 and 1
         */
        for { layer <- 1 until layout.length } {
            bias += (layer -> collection.mutable.Map[Int, Double]())
            for { neuron <- 0 until layout(layer) } {
                bias(layer) += (neuron -> r.nextDouble())
            }
        }
    }


    def setLearningRate(rate: Double) = {
        /** setLearningRate
         * adjusts the learning rate of the network
         * params:
         *      rate - the new learning rate
         */
        if (rate > 1.0 || rate < 0.0) {
            throw new IllegalArgumentException("learningRate rate must be between 0 and 1.")
        }
        learningRate = rate
    }

    
    def exportToFile(filename: String) = {
        /* export
         * Converts the weights and biases into a Json object and exports
         * it as a string to a specified outfile.
         * params:
         *      filename - the file to write to
         */
        var jsWeights: JsObject = new JsObject(Seq())
        var jsBias: JsObject = new JsObject(Seq())
        for { layer <- 1 until layout.length } {
            var layerWeights: JsObject = new JsObject(Seq())
            var layerBias: JsObject = new JsObject(Seq())
            for { neuron <- 0 until layout(layer) } {
                var inputWeights: JsObject = new JsObject(Seq())
                for { input <- 0 until layout(layer - 1) } {
                    inputWeights = inputWeights + (input.toString, JsNumber(weights(layer)(neuron)(input)))
                }
                layerWeights = layerWeights + (neuron.toString, inputWeights)
                layerBias = layerBias + (neuron.toString, JsNumber(bias(layer)(neuron)))
            }
            jsWeights = jsWeights + (layer.toString, layerWeights)
            jsBias = jsBias + (layer.toString, layerBias)
        }
        val outputJson: JsObject = new JsObject(Seq( ("weights", jsWeights), ("bias", jsBias) ))
        val writer = new PrintWriter(new File(filename))
        writer.write(outputJson.toString)
        writer.close()
    }


    def importFromFile(filename: String) = {
        /* import
         * Reads in a json file in the format written to in export and loads
         * it into the weights and bias maps
         * params:
         *      filename - the file to read from
         */
        val scanner: Scanner = new Scanner(new File(filename))
        val input = scanner.nextLine()
        val jsInput: JsObject = Json.parse(input).as[JsObject]
        val jsWeights = (jsInput \ "weights").as[JsObject]
        val jsBias = (jsInput \ "bias").as[JsObject]
        for { layer <- jsWeights.keys } {
            weights += (layer.toInt -> collection.mutable.Map[Int, collection.mutable.Map[Int, Double]]())
            bias += (layer.toInt -> collection.mutable.Map[Int, Double]())
            val weightsAtLayer = (jsWeights \ layer).as[JsObject]
            val biasAtLayer = (jsBias \ layer).as[JsObject]
            for { neuron <- weightsAtLayer.keys } {
                weights(layer.toInt) += (neuron.toInt -> collection.mutable.Map[Int, Double]())
                val neuronBias = (biasAtLayer \ neuron).as[JsNumber]
                bias(layer.toInt) += (neuron.toInt -> neuronBias.value.toDouble)
                val inputWeights = (weightsAtLayer \ neuron).as[JsObject]
                for { input <- inputWeights.keys } {
                    val connectionWeight = (inputWeights \ input).as[JsNumber]
                    weights(layer.toInt)(neuron.toInt) += (input.toInt -> connectionWeight.value.toDouble)
                }
            }
        }
    }
    
    
    private def feedHelper(inputs: Array[Double], layer: Int, activations: Array[Array[Double]],
                           z: Array[Array[Double]]): Array[Array[Double]] = {
        /* feedHelper
         * Recursively fill an array of the activation of each neuron.
         * params:
         *      inputs      - The initial inputs into the network
         *      layer       - The current layer of the recursive model
         *      activations - The location to enter results of each neuron
         *      z           - An array to represent the input value to the activation
         *                    function for each neuron
         * return: The activations array because scala makes us return something.
         */
        if (layer == 0) {
            activations(layer) = inputs
            z(layer) = inputs
        } else {
            feedHelper(inputs, layer - 1, activations, z)
            for { output <- 0 until layout(layer) } {
                for { input <- 0 until layout(layer - 1) } {
                    activations(layer)(output) += activations(layer - 1)(input) * weights(layer)(output)(input)
                }
                // Input of neuron is the sum of the inputs * their weights + the bias
                activations(layer)(output) += bias(layer)(output)
                z(layer)(output) = activations(layer)(output)
                activations(layer)(output) = sigmoid(activations(layer)(output))
            }
        }
        activations
    }


    private def sigmoid(x: Double): Double = 1.0 / ( 1.0 + math.pow(math.E, (-1.0 * x)) )


    private def sigmoidPrime(x: Double): Double = sigmoid(x) * sigmoid(1-x)


    private def getTransposedWeights(layer: Int): Array[Array[Double]] = {
        /* getTransposedWeights
         * Given a layer, returns an array representing the matrix transposition
         * of the weights going into the layer.
         * params:
         *      layer - the layer to get the vector for
         * return: The transposed weight matrix
         */
        var res: Array[Array[Double]] = new Array[Array[Double]](layout(layer - 1))
        for { i <- 0 until res.length } {
            res(i) = new Array[Double](layout(layer))
            for { j <- 0 until res(i).length } {
                res(i)(j) = weights(layer)(j)(i)
            }
        }
        res
    }


    private def matrixMultiply(x: Array[Array[Double]], y: Array[Array[Double]]): Array[Array[Double]] = {
        /* matrixMultiply
         * Given 2 matrices return the result of multiplication
         * params:
         *      x   - The matrix on the left side of the multiplication
         *      y   - The matrix on the right side of the multiplication
         * return: The product matrix
         */
        if (x(0).length != y.length) {
            throw new IllegalArgumentException("Matrix length/width don't match.")
        }
        var product: Array[Array[Double]] = new Array[Array[Double]](x.length)
        for { productRow <- 0 until product.length } {
            product(productRow) = new Array[Double](y(0).length)
            for { productColumn <- 0 until product(productRow).length
                  i <- 0 until y.length } {
                product(productRow)(productColumn) += x(productRow)(i) * y(i)(productColumn)
            }
        }
        product
    }
}
