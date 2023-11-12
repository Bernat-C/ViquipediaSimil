package main

import scala.io.Source
import scala.io.StdIn.readLine

object SimilitudEntreDocuments {

  def main(args: Array[String]): Unit = {

    var strfile1 = "primeraPartPractica/pg11.txt"
    var strfile2 = "primeraPartPractica/pg12.txt"
    var strStop = "primeraPartPractica/english-stop.txt"

    var stringBook1 = normalitza(readFileAsString(strfile1))
    var stringBook2 = normalitza(readFileAsString(strfile2))
    var listStopWords = readStopWordsFromFile(strStop)
    var ngram_size = 3

    while (true) {
      println("Què vols fer?\n")
      println("    0: Execució completa")
      println("    1: Canviar la mida d'N-Grams")
      println("    2: Canviar el fitxer 1 (Fitxer amb el que es fan totes les operacions)")
      println("    3: Canviar el fitxer 2 (Fitxer amb el que trobar la similitud)")
      println("    4: Canviar fitxer de stopWords")
      println("    5: Joc de proves #1")
      println("    6: Joc de proves #2")
      println("    7: Joc de proves #3")
      println("    S: Sortir de l'aplicació\n")
      val userInput = readLine("Entra la opció desitjada: ")
      try {
        userInput.toLowerCase() match {
          case "s" =>
            System.exit(0) //Sortir de l'aplicació
          case "0" =>
            println("\n*****************\n")
            println("Execució: ")
            println("Fitxer 1: " + strfile1)
            println("Fitxer 2: " + strfile2)
            println("Fitxer StopWords: " + strStop)
            println("N-Gram de mida: " + ngram_size)
            println("\n*****************\n")

          // FREQUENCIA DE PARAULES
          printWordOccurrence(freq(stringBook1))
          println("\n*****************\n")
          // FREQUENCIA DE PARAULES SENSE STOP-WORDS
          printWordOccurrence(nonStopFreq(stringBook1, listStopWords))
          println("\n*****************\n")
          // DISTRIBUCIÓ DE PARAULES
          paraulaFreqFreq(stringBook1)
          println("\n*****************\n")
          // N-GRAMS
          displayNGrams(stringBook1, ngram_size)
          println("\n*****************\n")
          println("La similitud és de " + cosinesim(stringBook1, stringBook2, listStopWords))
        case "1" =>
          val ngramSize = readLine("Quina mida d'n-gram vols fer?: ")
          ngram_size = ngramSize.toInt
        case "2" =>
          val strB1 = readLine("Quin vols que sigui el teu fitxer 1?: ")
          strfile1 = "primeraPartPractica/" + strB1
          stringBook1 = normalitza(readFileAsString("primeraPartPractica/"+strB1))
        case "3" =>
          val strB2 = readLine("Quin vols que sigui el teu fitxer 2?: ")
          strfile2 = "primeraPartPractica/" + strB2
          stringBook2 = normalitza(readFileAsString("primeraPartPractica/" + strB2))
        case "4" =>
          val stp = readLine("Quin és el nom del nou fitxer de stopWords?: ")
          strStop = "primeraPartPractica/" + stp
          listStopWords = readStopWordsFromFile("primeraPartPractica/" + stp)
        case "5" =>
          // FREQUENCIA DE PARAULES
          val strb1 = normalitza(readFileAsString("primeraPartPractica/pg11.txt"))
          val strb2 = normalitza(readFileAsString("primeraPartPractica/pg12.txt"))
          val ng = 3;
          printWordOccurrence(freq(strb1))
          println("\n*****************\n")
          // FREQUENCIA DE PARAULES SENSE STOP-WORDS
          printWordOccurrence(nonStopFreq(strb1, listStopWords))
          println("\n*****************\n")
          // DISTRIBUCIÓ DE PARAULES
          paraulaFreqFreq(strb1)
          println("\n*****************\n")
          // N-GRAMS
          displayNGrams(strb1, ng)
          println("\n*****************\n")
          println("La similitud és de " + cosinesim(strb1, strb2, listStopWords))
        case "6" =>
          // FREQUENCIA DE PARAULES
          val strb1 = normalitza(readFileAsString("primeraPartPractica/pg74.txt"))
          val strb2 = normalitza(readFileAsString("primeraPartPractica/pg2500.txt"))
          val ng = 5;
          printWordOccurrence(freq(strb1))
          println("\n*****************\n")
          // FREQUENCIA DE PARAULES SENSE STOP-WORDS
          printWordOccurrence(nonStopFreq(strb1, listStopWords))
          println("\n*****************\n")
          // DISTRIBUCIÓ DE PARAULES
          paraulaFreqFreq(strb1)
          println("\n*****************\n")
          // N-GRAMS
          displayNGrams(strb1, ng)
          println("\n*****************\n")
          println("La similitud és de " + cosinesim(strb1, strb2, listStopWords))
        case "7" =>
          // FREQUENCIA DE PARAULES
          val strb1 = normalitza(readFileAsString("primeraPartPractica/pg11.txt"))
          val strb2 = normalitza(readFileAsString("primeraPartPractica/pg11.txt"))
          val ng = 1;
          printWordOccurrence(freq(strb1))
          println("\n*****************\n")
          // FREQUENCIA DE PARAULES SENSE STOP-WORDS
          printWordOccurrence(nonStopFreq(strb1, listStopWords))
          println("\n*****************\n")
          // DISTRIBUCIÓ DE PARAULES
          paraulaFreqFreq(strb1)
          println("\n*****************\n")
          // N-GRAMS
          displayNGrams(strb1, ng)
          println("\n*****************\n")
          println("La similitud és de " + cosinesim(strb1, strb2, listStopWords))
        case _ =>
          println("Invalid option")
      }
      catch {
        case e: Exception =>
          println("Entrada no vàlida (nombre d'ngrams o nom de fitxer incorrectes)")
      }
      println("\n*****************\n")
    }
  }

  /**
   * Compares two texts
   * @param str1 Name of Page1
   * @param str2 Name of Page2
   * @param stopWords List of stopWords not to take into consideration in similarity assessment.
   * @return
   */
  def cosinesim(str1: String, str2: String, stopWords: List[String]): Double = {
    val vsm1 = getVectorSpaceModel(str1, 1, stopWords)
    val vsm2 = getVectorSpaceModel(str2, 1, stopWords)

    compareVectorSpaceModel(vsm1, vsm2)
  }

  /**
   * Compares two vectorspacemodels
   * @param vsm1 Vector of [(Word, Weight)]
   * @param vsm2 Vector of [(Word, Weight)]
   * @return Double: Similarity
   */
  def compareVectorSpaceModel(vsm1: Vector[(String, Double)], vsm2: Vector[(String, Double)]): Double = {
    val component1 = Math.sqrt(vsm1.map(x => x._2 * x._2).sum)
    val component2 = Math.sqrt(vsm2.map(x => x._2 * x._2).sum)

    val map1 = vsm1.toMap

    /**
     * We will do a fold left so that we iterate over vsm2 and we accumulate the multiplication of each element of vsm2 that we can find in vsm1.
     * If we cannot find it as the multiplication would return 0 we simply return the accumulated value.
     */
    val scalarProduct = vsm2.foldLeft(0.0) { (acm, word) =>
      map1.get(word._1) match {
        case Some(weight) => acm + (weight * word._2)
        case None => acm
      }
    }

    scalarProduct / (component1 * component2)
  }


  /**
   * Returns the string passed with only one space separating each words and with all stopwords removed.
   * @param str
   * @param stopWords
   * @return
   */
  def getStringWithoutStopWords(str: String, stopWords: List[String]): String = {

    var words = strToList(str).filter(x => !stopWords.contains(x))
    words.mkString(" ")
  }

  /**
   * Returns the vectorSpaceModel for the string str with n-word strings.
   * @param str
   * @param n
   * @return
   */
  def getVectorSpaceModel(str: String, n: Int, stopWords: List[String]): Vector[(String, Double)] = {
    val nonStopString = getStringWithoutStopWords(str, stopWords)
    val wordsCounts = freqNGrams(nonStopString, n)
    val maxFreq = wordsCounts.map(_._2).max

    var vectorWeights: Vector[(String, Double)] = Vector()
    wordsCounts.foreach(x => {
      val tuple: (String, Double) = (x._1, calculaFrequenciaNormalitzada(x._2, maxFreq))
      vectorWeights :+= tuple
    })

    vectorWeights
  }

  /**
   * Calcula la freqüència normalitzada d'una paraula en un text que conté una frequencia maxima freqMaxWord
   * @param freqWord
   * @param freqMaxWord
   * @return
   */
  def calculaFrequenciaNormalitzada(freqWord: Int, freqMaxWord: Int): Double = {
    freqWord.toDouble/freqMaxWord.toDouble
  }

  /**
   * Calculates the frequencies of every ngram composed of n words in str.
   * @param str
   * @param n
   * @return
   */
  def freqNGrams(str: String, n: Int): List[(String, Int)] = {

    val listNGrams = strToList(str).sliding(n).toList
    val ngrams = listNGrams.map(ngram => ngram.mkString(" "))

    val listAppearances = ngrams.groupBy(identity).view.mapValues(_.size).toList.sortBy(_._2)(Ordering.Int.reverse)

    listAppearances
  }

  /**
   * Displays the 10 most common n-grams
   * @param str string to search n-grams from
   * @param n number of words that make the n-grams
   */
  def displayNGrams(str: String, n: Int): Unit = {

    val ngrams: List[(String, Int)] = freqNGrams(str, n)

    println("NGrams més freqüents:")
    for (ngram <- ngrams.take(10)) {
      // Imprimim primer el 2: nombre de paraules que han aparegut n vegades, i després 1: nombre de vegades que han aparegut.
      println(f"${ngram._1}%-25s ${ngram._2}%3s")
    }
  }

  /**
   * Displays the appearances that have the most and the least number of words.
   * @param str
   */
  def paraulaFreqFreq(str: String): Unit = {

    val listCounts: List[(String, Int)] = freq(str);

    /**
     * First we group by the second parameter all elements of the list, meaning we're going to get a Map of number of appearences and List of tuples with that number of appearances.
     * Then we apply mapValues to apply the function size to each one of the values (as it is a map those are the tuples of words appearing that many times).
     * This returns a map, that we transform to a list, containing a Number of appearances - number of words with that appearances.
     */
    val frequencyMap = listCounts.groupBy(_._2).view.mapValues(_.size).toList.sortBy(_._2)(Ordering.Int.reverse)

    val first10 = frequencyMap.take(10)
    val last5 = frequencyMap.takeRight(5)

    println("Les 10 frequencies mes frequents:")
    for (word <- first10){
      // Imprimim primer el 2: nombre de paraules que han aparegut n vegades, i després 1: nombre de vegades que han aparegut.
      println(word._2 + " paraules apareixen " + word._1 + " vegades")
    }

    println("\nLes 5 frequencies menys frequents:")
    for (word <- last5) {
      // Imprimim primer el 2: nombre de paraules que han aparegut n vegades, i després 1: nombre de vegades que han aparegut.
      println(word._2 + " paraules apareixen " + word._1 + " vegades")
    }
  }

  /**
   * Reads from file and returns a List containing all lines from file as strings.
   * @param path
   * @return
   */
  def readStopWordsFromFile(path: String): List[String] = {
    val source = Source.fromFile(path)
    try {
      source.getLines().toList
    } finally {
      source.close()
    }
  }

  /**
   * Prints information about a list containing words and their number of appearances in a text.
   * @param list
   */
  def printWordOccurrence(list: List[(String, Int)]): Unit = {

    // Calculate the total number of words and different Words
    val totalWords = list.map(_._2).sum
    val differentWords = list.length

    // Print the header
    println(s"Num de Paraules:    " + totalWords + "     Diferents:     " + differentWords)
    val header = f"Paraules       ocurrencies   frequencia"
    println(header)
    println("---------------------------------------")

    // Print the sorted word counts with frequency percentages
    for ((word, count) <- list.take(10)) {
      val frequency = (count.toDouble / totalWords * 100).formatted("%.2f")
      // Negatiu alinea esquerra, positiu dreta
      println(f"${word}%-15s ${count}%5d ${frequency}%6s")
    }
  }

  /**
   * Returns a list of the words in the text without stopWords.
   * @param str
   * @param stopWords
   * @return
   */
  def strToNonStopList(str: String, stopWords: List[String]): List[String] = {
    strToList(str).filter(!stopWords.contains(_))
  }

  /**
   * Returns a List<String, Int> that contains all words that appear in str and do not appear in stopWords ordered by number of appearances
   * @param str string containing text.
   * @param stopWords list of words to not include in the returned list.
   * @return
   */
  def nonStopFreq(str: String, stopWords: List[String]): List[(String, Int)] = {
    var words = strToList(str).filter(x => !stopWords.contains(x))
    freqList(words);
  }

  /**
   * Returns a List<String, Int> that contains all words that appear in str ordered by number of appearances.
   * @param str
   * @return
   */
  def freq(str: String): List[(String, Int)] = {
    freqList(strToList(str));
  }

  /**
   * Returns a list of words in str (sequences separated by whitespaces)
   * @param str
   * @return
   */
  def strToList(str: String): List[String] = {
    str.split("\\s+").filter(_.nonEmpty).toList
  }

  /**
   * Returns a list of frequencies of each word in ls.
   * @param ls
   * @return
   */
  def freqList(ls: List[String]): List[(String, Int)] = {
    ls.groupBy(identity).view.mapValues(_.size).toList.sortBy(_._2)(Ordering.Int.reverse)
  }

  /**
   * Reads file and returns a string containing it's contents
   * @param path String
   * @return
   */
  def readFileAsString(path: String): String = {
    val source = Source.fromFile(path)
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  /**
   * Returns the string str to lowercase and with any character other than a-z (and accents) replaced by a space.
   * It removes all numbers intentionally as they introduce noise.
   *
   * @param str
   * @return
   */
  def normalitza(str: String): String = {

    val regex = "\\[\\[.*?\\]\\]".r
    val result = regex.replaceAllIn(str, "") // Remove everything between "[[Fitxer:" and "]]"

    result.map {
      /** If c is a letter or accented letter */
      case c if Character.isLetter(c) =>
        if (c.isUpper) c.toLower else c
      case _ => ' '
    }
  }
}



