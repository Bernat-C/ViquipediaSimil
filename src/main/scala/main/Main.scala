package main

import java.io.File

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import mapreduce._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.io.StdIn.readLine
import scala.util.Try

object Main {
  def main(args: Array[String]): Unit = {
    val folder = "viqui_files"
    val nJocsDeProves = 9;
    println("Benvingut al mesurador de similitud entre documents!")
    while (true) {
      println("Què vols fer?\n")
      println("    0: Calcular promig de referències")
      println("    1: Calcular promig d'imatges")
      println("    2: Pàgines més rellevants (paràmetres personalitzats)")
      println("    3: Pàgines més similars (paràmetres personalitzats)")
      println("    4: Pàgines més rellevants més similars (paràmetres personalitzats)")
      println("(5-" + (nJocsDeProves+4) + "): Executar joc de proves corresponent")
      println("    S: Sortir de l'aplicació\n")
      var userInput = readLine("Entra la opció desitjada: ")

      if (userInput.toLowerCase.equals("s")) {
        System.exit(0) //Sortir de l'aplicació
      }
      else {
        Try(userInput.toInt) match {
          case scala.util.Success(value) =>
            if (value == 2){
              //Pàgines més rellevants
              val (npagines, dummy, nmappers, nreducers) = llegirParametres(true,false)

              timeMeasurement(paginesRellevantsGlobal, (npagines, folder, nmappers, nreducers))
            }
            else if (value == 3){
              //Pàgines més similars
              val (npagines, llindarSimilitud, nmappers, nreducers) = llegirParametres(true,true)

              timeMeasurement(similarNotRefGlobal, (folder, npagines, llindarSimilitud, nmappers, nreducers))
            }
            else if (value == 4) {
              //Pàgines més rellevants més similars
              val (npagines, llindarSimilitud, nmappers, nreducers) = llegirParametres(true, true)

              mostrarMissatgeIniciExecucio(npagines, llindarSimilitud, nmappers, nreducers)

              timeMeasurement(paginesMesRellevantsISimilars, (folder, npagines, llindarSimilitud, nmappers, nreducers));
            }
            else if(value == 0){
              //Promig de referències
              val (dummy, dummy2, nmappers, nreducers) = llegirParametres(false,false)

              timeMeasurement(meanReferences,(folder, nmappers, nreducers))
            }
            else if(value == 1){
              //Promig d'imatges
              val (dummy, dummy2, nmappers, nreducers) = llegirParametres(false,false)

              timeMeasurement(meanImages, (folder, nmappers, nreducers))
            }
            //Proves
            else if (value == 5){
              println("Executant Prova1: 4 Pàgines més rellevants amb llindar de similitud 0.0:")
              timeMeasurement(JocsDeProves.prova1,folder)
            }
            else if(value == 6){
              println("Executant Prova2: 100 pàgines més rellevants amb llindar de similitud 0.5 amb 1 mapper i 1 reducer (seqüencial):")
              timeMeasurement(JocsDeProves.prova2,folder)
            }
            else if (value == 7) {
              println("Executant Prova3: 100 pàgines més rellevants amb llindar de similitud 0.5 amb 16 mappers i 16 reducers:")
              timeMeasurement(JocsDeProves.prova3, folder)
            }
            else if (value == 8) {
              println("Executant Prova4: 500 pàgines més rellevants amb llindar de similitud 0.5 amb 1 mapper i 1 reducer (seqüencial):")
              timeMeasurement(JocsDeProves.prova4, folder)
            }
            else if (value == 9) {
              println("Executant Prova5: 500 pàgines més rellevants amb llindar de similitud 0.5 amb 16 mappers i 16 reducers:")
              timeMeasurement(JocsDeProves.prova5, folder)
            }
            else if (value == 10){
              println("Executant Prova6: Pàgines més similars d'entre les primeres 100 pàgines amb llindar de similitud 0.5 amb 16 mappers i 16 reducers")
              timeMeasurement(JocsDeProves.prova6, folder)
            }
            else if (value == 11) {
              println("Executant Prova7: Pàgines més similars d'entre les primeres 500 pàgines amb llindar de similitud 0.5 amb 16 mappers i 16 reducers")
              timeMeasurement(JocsDeProves.prova7, folder)
            }
            else if(value == 12){
              println("Executant Prova8: Promig de referències amb 16 mappers i 16 reducers")
              timeMeasurement(JocsDeProves.prova8, folder)
            }
            else if (value == 13) {
              println("Executant Prova8: Promig d'imatges amb 16 mappers i 16 reducers")
              timeMeasurement(JocsDeProves.prova9, folder)
            }

            //Errors
            else if (value < 1 | value > 4+nJocsDeProves) {
              println("Aquesta opció no està disponible!")
            }
          case scala.util.Failure(exception) =>
            println("Aquesta opció no està disponible!")
        }
      }
      println("\n*****************\n")
    }
  }

  /**
   * Prints a message showing which execution will be performed
   * @param npagines
   * @param llindarSimilitud
   * @param nmappers
   * @param nreducers
   */
  def mostrarMissatgeIniciExecucio(npagines: Int, llindarSimilitud: Double, nmappers: Int, nreducers: Int): Unit = {
    println("\n*********************")
    println("INICIANT EXECUCIÓ AMB:")
    println(npagines + " Pàgines")
    if(llindarSimilitud != -1) println(llindarSimilitud + " de llindar de similitud")
    println(nmappers + " Mappers")
    println(nreducers + " Reducers")
    println("*********************\n")
  }

  /**
   * Shows very similar pages (taking a threshold as reference) found in the most relevant npagines pages
   * @param t (folder, npagines, llindarSimilitud, nmappers, nreducers)
   */
  def paginesMesRellevantsISimilars(t: (String, Int, Double, Int, Int)): Unit = {
    //Parsing
    val lf = ProcessListStrings.getListOfFiles(t._1); // Obtenim la llista de fitxers
    val stopWords = SimilitudEntreDocuments.readStopWordsFromFile("stopwordscatalanet.txt"); //Obtenim stopwords
    val pagines = lf.map(x => ProcessListStrings.getFileParts(t._1 + "/" + x.getName)) //Obtenim contingut dels fitxers

    //Obtenim les npagines més rellevants
    val paginesRell = paginesRellevants(pagines.map(x => (x._1, x._3)), t._2, t._4, t._5)
    println("\nPagines més rellevants mostrades!\n")
    //D'entre totes les pàgines i el seu contingut, ens quedem només el de les més rellevants
    val listWords = pagines.filter(x => paginesRell.contains(x._1))
    //Obtenim les pàgines més similars d'entre les més rellevants
    similarNotRef(listWords.map(x => (x._1, SimilitudEntreDocuments.strToNonStopList(SimilitudEntreDocuments.normalitza(x._2), stopWords), x._3)), t._3, t._4, t._5)
  }

  /**
   * Reads npages, llindarSimilitud, nmappers and nreducers
   * @return
   */
  def llegirParametres(pagines: Boolean, llindar: Boolean): (Int, Double, Int, Int) = {
    var npagines = -1
    var userInput : String = ""
    if(pagines){
      var userInput = readLine("Entra el nombre de pàgines a utilitzar: ")
      while (!Try(userInput.toInt).isSuccess || !(userInput.toInt > 0)) {
        println("ERROR: El nombre de pàgines ha de ser major que 0 i enter!\n")
        userInput = readLine("Entra el nombre de pàgines a utilitzar: ")
      }
      npagines = userInput.toInt
    }

    var llindarSimilitud = -1.0
    if(llindar){
      userInput = readLine("Entra el llindar de similitud a utilitzar: ")
      while (!Try(userInput.toDouble).isSuccess || !(userInput.toDouble >= 0 && userInput.toDouble <= 1)) {
        println("ERROR: El llindar ha d'estar entre 0 i 1!\n")
        userInput = readLine("Entra el llindar de similitud a utilitzar: ")
      }
      llindarSimilitud = userInput.toDouble
    }

    userInput = readLine("Entra el nombre de mappers a utilitzar: ")
    while (!Try(userInput.toInt).isSuccess || !(userInput.toInt > 0)) {
      println("ERROR: El nombre de mappers ha de ser major que 0 i enter!\n")
      userInput = readLine("Entra el nombre de mappers a utilitzar: ")
    }
    var nmappers = userInput.toInt

    userInput = readLine("Entra el nombre de reducers a utilitzar: ")
    while (!Try(userInput.toInt).isSuccess || !(userInput.toInt > 0)) {
      println("ERROR: El nombre de reducers ha de ser major que 0 i enter!\n")
      userInput = readLine("Entra el nombre de reducers a utilitzar: ")
    }
    var nreducers = userInput.toInt

    (npagines, llindarSimilitud, nmappers, nreducers)
  }

  /**
   * Prints the npagines most referenced that are contained in the folder.
   * @param t (npagines, folder, nmappers, nreducers)
   */
  def paginesRellevantsGlobal(t: (Int, String, Int, Int)): Unit = {
    val lf: List[File] = ProcessListStrings.getListOfFiles(t._2);

    val resultat = MRGetRefGlobal(lf, t._3, t._4);
    println("Pàgines més rellevants:")
    for (v <- resultat.toList.sortBy(_._2)(Ordering.Int.reverse).take(t._1)) println(v)
  }

  /**
   * Prints the npagines most referenced that are contained in the folder and returns the result as a Set.
   * @param pagines
   * @param npagines
   * @param nmappers
   * @param nreducers
   * @return
   */
  def paginesRellevants(pagines: List[(String, List[String])], npagines: Int, nmappers: Int, nreducers: Int): Set[String] = {
    val resultat = MRGetRef(pagines, nmappers, nreducers);
    println("Pàgines més rellevants:")
    val sortedRes = resultat.toList.sortBy(_._2)(Ordering.Int.reverse).take(npagines)
    for (v <- sortedRes) println(v)
    sortedRes.map(x => x._1).toSet
  }

  /**
   * Prints the mean references: The mean number of references each page in the folder has.
   * @param folder
   */
  def meanReferences(t : (String, Int, Int)): Unit = {
    val lf: List[File] = ProcessListStrings.getListOfFiles(t._1);

    // Change if you want less files to be used.
    val nFitxers = lf.length
    val pagines = lf.take(nFitxers).map(x => ProcessListStrings.getFileParts(t._1 + "/" + x.getName)).map(x => (x._1, x._3));
    val resultat = MRGetRef(pagines, t._2, t._3);

    println("La mitjana de referències a cada pàgina és " + resultat.values.sum.toDouble / nFitxers)
  }

  /**
   * Calculates the mean number of images in each page the folder has.
   * @param folder
   */
  def meanImages(t: (String, Int, Int)): Unit = {
    val lf: List[File] = ProcessListStrings.getListOfFiles(t._1);

    // Change if you want less files to be used.
    val nFitxers = lf.length
    val resultat = MROnlyImages(lf, t._2, t._3);
    println(resultat.size)
    println("La mitjana de imatges a cada pàgina és " + resultat.values.sum.toDouble / nFitxers)
  }

  /**
   * Calculates the most similar wikipedia pages inside folder that do not reference each other
   * @param t (folder, npagines, llindarSimilitud, nmappers, nreducers)
   */
  def similarNotRefGlobal(t: (String, Int, Double, Int, Int)): Unit = {
    val stopWords = SimilitudEntreDocuments.readStopWordsFromFile("stopwordscatalanet.txt");
    val lf: List[File] = ProcessListStrings.getListOfFiles(t._1);

    val resultat = TF_IDF.detectarSimilarsNoRefGlobal(lf, t._2, stopWords, t._3, t._4, t._5)

    println("Les pàgines més similars de " + t._3 + " són:")
    for (v <- resultat) println(v)
  }

  /**
   * Calculates the most similar wikipedia pages inside folder that do not reference each other. Takes a precalculated
   * List(String, List[String], List[String]) containing the elements of the pages that have to be processed.
   * @param listWords
   * @param llindarSimilitud
   * @param nmappers
   * @param nreducers
   */
  def similarNotRef(listWords: List[(String, List[String], List[String])], llindarSimilitud: Double, nmappers: Int, nreducers: Int): Unit = {

    val resultat = TF_IDF.detectarSimilarsNoRef(listWords, llindarSimilitud, nmappers, nreducers)

    println("Les pàgines més similars de " + llindarSimilitud + " són:")
    for (v <- resultat) println(v)
  }

  /**
   * Computes a function that has only one parameter and prints its time info on the terminal.
   * @param funcio Function to execute
   * @param args Argument of the function
   * @tparam A
   */
  def timeMeasurement[A](funcio: A => Unit, args: A): Long = {
    println("Començem a comptar el temps d'execució.")
    val inici = System.currentTimeMillis()
    funcio(args);
    val fi = System.currentTimeMillis()
    val temps = fi - inici
    println(s"La funció ha tardat: $temps milisegonds")
    temps
  }

  /**
   * Generic MapReduce implemented to optimize code and avoid repetitions
   * @param list List to MapReduce.
   * @param mapping Mapping function
   * @param reducing Reducing function
   * @param nmappers Number of mappers to use
   * @param nreducers Number of reducers to use
   * @tparam K1
   * @tparam V1
   * @tparam K2
   * @tparam V2
   * @tparam V3
   * @return The mapreduce results.
   */
  def MR[K1,V1,K2,V2,V3](list: List[(K1,List[V1])], mapping: (K1,List[V1]) => List[(K2,V2)], reducing: (K2,List[V2])=> (K2,V3), nmappers: Int, nreducers: Int): Map[K2, V2] = {
    // Creem el sistema d'actors
    val systema: ActorSystem = ActorSystem("sistema")

    val referenceCount = systema.actorOf(Props(new MapReduce(list, mapping, reducing, nmappers, nreducers)), name = "mastercount")

    // Els Futures necessiten que se'ls passi un temps d'espera, un pel future i un per esperar la resposta.
    // La idea és esperar un temps limitat per tal que el codi no es quedés penjat ja que si us fixeu preguntar
    // i esperar denota sincronització. En el nostre cas, al saber que el codi no pot avançar fins que tinguem
    // el resultat del MapReduce, posem un temps llarg (100000s) al preguntar i una Duration.Inf a l'esperar la resposta.

    // Enviem un missatge com a pregunta (? enlloc de !) per tal que inicii l'execució del MapReduce del wordcount.
    //var futureresutltwordcount = wordcount.ask(mapreduce.MapReduceCompute())(100000 seconds)

    implicit val timeout = Timeout(10000 seconds) // L'implicit permet fixar el timeout per a la pregunta que enviem al wordcount. És obligagori.
    var futureresutltreferenceCount = referenceCount ? mapreduce.MapReduceCompute()

    //println("Awaiting")
    // En acabar el MapReduce ens envia un missatge amb el resultat
    var referencesResult: Map[K2, V2] = Await.result(futureresutltreferenceCount, Duration.Inf).asInstanceOf[Map[K2, V2]]

    //println("Results Obtained")

    // Fem el shutdown del actor system
    //println("shutdown")
    systema.terminate()
    //println("ended shutdown")

    referencesResult
  }

  /**
   * Returns the MapReduce results of finding the number of references for each page.
   * @param nfitxers
   * @return
   */
  def MRMeanRef(lf: List[File]): Map[String, Int] = {
    val folder = "viqui_files"
    val nFitxers = lf.length
    val nmappers = lf.length
    val nreducers = lf.length
    val pagines = lf.take(nFitxers).map(x => ProcessListStrings.getFileParts(folder + "/" + x.getName));

    MR(pagines.map(x => ((x._1,pagines.map(_._1)), x._3)), MappersAndReducers.mappingMR, MappersAndReducers.reducingMR, nmappers, nreducers)
  }

  /**
   * Returns the MapReduce results of finding the number of references for each page file in the input.
   * @param nfitxers
   * @return
   */
  def MRGetRefGlobal(lf: List[File], nmappers: Int, nreducers: Int): Map[String, Int] = {
    val folder="viqui_files"

    val pagines = lf.map(x => ProcessListStrings.getFileParts(folder + "/" + x.getName)).map(x => (x._1,x._3));
    val result = MR(pagines, MappersAndReducers.mappingSD, MappersAndReducers.reducingSD, nmappers, nreducers)
    val paginesKeys: Set[String] = pagines.map(_._1).toSet
    result.filter(x => paginesKeys.contains(x._1))
  }

  /**
   * Returns the MapReduce results of finding the number of references for each page. Takes a precalculated
   * List[(String, List[String]) of the pages that should be processed
   * @param pagines
   * @param nmappers
   * @param nreducers
   * @return
   */
  def MRGetRef(pagines: List[(String, List[String])], nmappers: Int, nreducers: Int): Map[String, Int] = {
    val result = MR(pagines, MappersAndReducers.mappingSD, MappersAndReducers.reducingSD, nmappers, nreducers)
    val paginesKeys: Set[String] = pagines.map(_._1).toSet
    result.filter(x => paginesKeys.contains(x._1))
  }

  /**
   * Executes a MapReduce that computes the number of images every file in lf has.
   * @param lf
   * @return
   */
  def MROnlyImages(lf: List[File], nmappers: Int, nreducers: Int): Map[String, Int] = {
    val folder = "viqui_files"
    val nFitxers = lf.length
    val pagines = lf.take(nFitxers).map(x => ProcessListStrings.getFileParts(folder + "/" + x.getName));

    MR(pagines.map(x => (x, List())), MappersAndReducers.mappingMeanImg, MappersAndReducers.reducingMeanImage, nmappers, nreducers)
  }
}

object TF_IDF {
  /**
   * Calculates the idf index
   * @param terme word we are trying to calculate the idf from.
   * @param fitxers List of files where we calculate the idf from.
   * @return
   */
  def idf(terme: String, fitxers: List[List[String]]): Double = {

    val nC = fitxers.length;
    val file_ocurr = fitxers.count(_.contains(terme));

    Math.log(nC/file_ocurr)
  }

  /**
   * Calculates the tf_idf index.
   * @param terme word we are trying to calculate the tf_idf from.
   * @param fitxer document we are trying to calculate tf_idf
   * @param fitxers List of files where we calculate the idf from. May include fitxer.
   * @return
   */
  def tf_idf(terme: String, fitxer: List[String], fitxers: List[List[String]]): Double = {
    idf(terme, fitxers) * SimilitudEntreDocuments.freqList(fitxer).find(_._1.equals(terme)).getOrElse((terme, 0))._2
  }

  /**
   * Returns true if str1 references str2 and str2 references str1.
   * @param name1 Name of first page to be referenced.
   * @param name2 Name of second page to be referenced.
   * @param refs Map of the set of references each page has.
   * @return
   */
  def MutuallyReferenced(name1: String, name2: String, refs: Map[String, Set[String]]): Boolean = {
    refs.getOrElse(name1, Set()).contains(name2) && refs.getOrElse(name2, Set()).contains(name1)
  }

  /**
   * Returns the pages more similar than similaritat that do not reference each other
   * @param lf          List of files that contains the pages to consider
   * @param quantes     How many pages of lf do we want to use
   * @param stopWords   List of stopwords to not consider when doing the similarity assessment
   * @param similaritat Similarity Threshold to consider pages similar enough
   */
  def detectarSimilarsNoRefGlobal(lf: List[File], quantes: Int, stopWords: List[String], similaritat: Double,
                            nmappers: Int, nreducers: Int): List[((String, String), Double)] = {
    /** Folder to get files from */
    val folder = "viqui_files"

    /** Contains triplets of (Name, Words in content, References) */
    val listWords = lf.take(quantes).map(x => ProcessListStrings.getFileParts(folder + "/" + x.getName)).map(x => (x._1, SimilitudEntreDocuments.strToNonStopList(SimilitudEntreDocuments.normalitza(x._2), stopWords), x._3))
    /** Contains a dictionary of (PageName => Set of references) */
    val dictRefs: Map[String, Set[String]] = listWords.groupBy(_._1).view.mapValues(_.head._3.toSet).toMap

    /** Contains a map of (PageName1, PageName2) => Similarity */
    detectarPaginesSimilars(listWords.map(x => (x._1, x._2)), nmappers, nreducers).toList.filter(x => x._2 >= similaritat && !MutuallyReferenced(x._1._1, x._1._2, dictRefs)).toList.sortBy(_._2)(Ordering.Double.IeeeOrdering.reverse)
  }

  /**
   * Returns the pages more similar than similaritat that do not reference each other
   *
   * @param listWords   List of files that contains the pages to consider
   * @param quantes     How many pages of lf do we want to use
   * @param stopWords   List of stopwords to not consider when doing the similarity assessment
   * @param similaritat Similarity Threshold to consider pages similar enough
   */
  def detectarSimilarsNoRef(listWords: List[(String, List[String], List[String])], similaritat: Double,
                            nmappers: Int, nreducers: Int): List[((String, String), Double)] = {

    /** Contains a dictionary of (PageName => Set of references) */
    val dictRefs: Map[String, Set[String]] = listWords.groupBy(_._1).view.mapValues(_.head._3.toSet).toMap

    /** Contains a map of (PageName1, PageName2) => Similarity */
    detectarPaginesSimilars(listWords.map(x => (x._1, x._2)), nmappers, nreducers).toList.filter(x => x._2 >= similaritat && !MutuallyReferenced(x._1._1, x._1._2, dictRefs)).toList.sortBy(_._2)(Ordering.Double.IeeeOrdering.reverse)
  }

  /**
   * Returns a map of the similarity of every two pages.
   * @param lf Files to compute similarity
   * @param stopWords List of stopwords to discard when computing similarity
   * @return
   */
  def detectarPaginesSimilars(listWords: List[(String, List[String])],
                              nmappers: Int, nreducers: Int): Map[(String, String), Double] = {

    val dictIDF = calculateIDFDict(listWords, nmappers, nreducers);
    val dictTF = calculateTFDict(listWords, nmappers, nreducers);

    val vsms = calculateVSMS(listWords, dictIDF, dictTF);

    println("Performing comparisons...")
    val comparisons = for (i <- vsms.indices; j <- (i + 1) until vsms.length) yield ((vsms(i)._1, vsms(j)._1),List((vsms(i)._2, vsms(j)._2)))
    println("Comparisons generated!")
    calculateCompareSpaceModels(comparisons.toList, nmappers, nreducers);
  }

  /**
   * Calculates the list of vectorspacemodels for every file in lw
   * @param lw List of pairs of filename, list of words in file
   * @param dictIDF Precalculated map of word, IDF Weight in lw.
   * @param dictTF Precalculated map of (file,word) => TF Weight of word in file.
   * @return
   */
  def calculateVSMS(lw: List[(String, List[String])], dictIDF: Map[String, Double], dictTF: Map[(String, String), Int]): List[(String,Vector[(String, Double)])] = {

    var listVSMS: List[(String,Vector[(String, Double)])] = List()
    lw.foreach(x => {
      var vectorWeights: Vector[(String, Double)] = Vector()
      // We iterate over the distinct list of words in a file
      x._2.distinct.foreach(y => {
        val tuple: (String, Double) = (y, dictIDF.getOrElse(y, 0.0) * dictTF.getOrElse((x._1,y), 0).toDouble)
        // We add to the vector the tuple (word,file)
        vectorWeights = vectorWeights :+ tuple
      })
      listVSMS = listVSMS :+ (x._1, vectorWeights)
    })
    listVSMS
  }

  /**
   * Calculates a list of idfs of every word in a file in lf.
   * @param lf
   * @param stopWords
   * @return List(word, idf)
   */
  def calculateIDFDict(lw: List[(String, List[String])], nmappers: Int, nreducers: Int): Map[String, Double] = {
    val nFitxers = lw.length

    val result = Main.MR(lw, MappersAndReducers.mappingIDF, MappersAndReducers.reducingIDF, nmappers, nreducers)

    result.map{ case (k,v) => (k, Math.log(nFitxers/v)) }
  }

  /**
   * Calculates the comparisons of vectorSpaceModels
   * @param vsms
   * @return
   */
  def calculateCompareSpaceModels(vsms: List[ ( (String, String), List[ (Vector[(String, Double) ], Vector[(String, Double)]) ])],
                                  nmappers: Int, nreducers: Int): Map[(String, String), Double] = {

    Main.MR(vsms, MappersAndReducers.mappingCVSM, MappersAndReducers.reducingCVSM, nmappers, nreducers)
  }

  /**
   * Returns a dictionary of pairs (PageName, Word) => TF
   * @param lw List of (PageName, all words without stopwords in PageName)
   * @return
   */
  def calculateTFDict(lw: List[(String, List[String])], nmappers: Int, nreducers: Int): Map[(String, String), Int] = {
    mapreduceTF(lw, nmappers, nreducers);
  }

  /**
   * Calculates a list of Term Frequencies for every term in a file
   * @param lw List of (PageName, List(Words))
   * @return Map((PageName, term), TF of term in page)
   */
  def mapreduceTF(lw: List[(String, List[String])], nmappers: Int, nreducers: Int): Map[(String, String), Int] = {
    // List we are going to output.
    Main.MR(lw, MappersAndReducers.mappingTF, MappersAndReducers.reducingTF, nmappers, nreducers)
  }
}

object references {

  /**
   * We remove references that
   * - Contain #: Reference to a section of a page.
   * - Start with MG (Not defined pages)
   * - Start with Fitxer: (Files)
   * @param str
   */
  def shouldDeleteReference(str: String): Boolean = {
      str.contains("#") || str.startsWith("[[MG") || str.startsWith("[[Fitxer:")
  }

  /**
   * Cleans all references and removes all references we don't want.
   * From each reference
   * - Removes 2 first and last characters of every reference. (We supose every refference has the form [\[reference]\])
   * - Removes text after |
   * - Removes spaces in the begining or end
   *
   * @param str
   */
  def cleanReference(str: String): String = {
    str.substring(2, str.length - 2).takeWhile(_ != '|').trim()
  }
}
