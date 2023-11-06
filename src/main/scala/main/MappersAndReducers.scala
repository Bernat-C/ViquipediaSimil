package main

import scala.language.postfixOps

object MappersAndReducers {

  // Unused code
  /**
   * The mapping makes the multiplications and returns always the same word.
   * That will make the dictionary put all the multiplications in the same list.
   * @param word
   * @param vsm2
   * @return
  def mappingScalarProduct(word: (String, Double), vsm2: List[(String, Double)]): List[(String, Double)] = {
    val result = vsm2.find(_._1 == word._1)
    result match {
      case Some(value) =>
        List(("total", word._2*value._2));
      case None =>
        List();
    }
  }

  /**
   * As all multiplications will be in vsm we just make the addition part of the scalar product.
   * @param word
   * @param vsm
   * @returns
   */
  def reducingScalarProduct(word: String, vsm: List[Double]): (String, Double) = {
    (word,vsm.sum)
  }*/

  /**
   *
   * @param names Titles of each page
   * @param vsms Vector Space Models for each word in each page
   * @return
   */
  def mappingCVSM(names: (String, String), vsms:  List[(Vector[(String, Double)], Vector[(String, Double)])]): List[((String,String), Double)] = {
    for (vsm <- vsms) yield (names, SimilitudEntreDocuments.compareVectorSpaceModel(vsm._1,vsm._2))
  }

  /**
   * There will only be one element in weights as each pair is unique.
   * @param pages
   * @param weights
   */
  def reducingCVSM(pages: (String,String), weights: List[Double]): ((String,String), Double) = {
    (pages, weights.sum)
  }

  /**
   * @param pagina Tuple containing the name of the current page and the list of all pages to be able to distinguish if the reference is in our set of pages.
   * @param referencies
   * @return
   */
  def mappingMR(pagina: (String, List[String]), referencies: List[String]): List[(String, Int)] = {
    // We clean all references at once because we have to delete some of them.
    for (ref <- referencies; if !references.shouldDeleteReference(ref) && pagina._2.contains(references.cleanReference(ref))) yield ("total", 1)
  }

  /**
   *
   * @param pagina
   * @param nums
   * @return
   */
  def reducingMR(pagina: String, nums: List[Int]): (String, Int) = {
    (pagina, nums.sum)
  }

  /**
   *
   * @param pagina Title of the page
   * @param referencies List of references in the page
   * @return
   */
  def mappingSD(pagina: String, referencies: List[String]): List[(String, Int)] = {
    // We clean all references at once because we have to delete some of them.
    for (ref <- referencies; if !references.shouldDeleteReference(ref)) yield (references.cleanReference(ref), 1)
  }

  /**
   *
   * @param pagina
   * @param nums
   * @return
   */
  def reducingSD(pagina: String, nums: List[Int]): (String, Int) = {
    (pagina, nums.sum)
  }

  /**
   *
   * @param pagina Title, Content and Referenes of a page
   * @param dummy_content Dummy parameter
   * @return
   */
  def mappingMeanImg(pagina: (String, String, List[String]), dummy_content: List[String]): List[(String, Int)] = {
    for (pag <- pagina._2.split(" ").toList; if pag.contains("[[Fitxer:")) yield (pagina._1, 1)
  }

  /**
   *
   * @param pagina
   * @param nums
   * @return
   */
  def reducingMeanImage(pagina: String, nums: List[Int]): (String, Int) = {
    (pagina, nums.sum)
  }

  /**
   * Mapper to calculate TF of every word in page
   * @param pagina page of words
   * @param words  list of words in pagina
   * @return List of ((PageName, Word), 1)
   */
  def mappingTF(pagina: String, words: List[String]): List[((String, String), Int)] = {
    for (w: String <- words) yield ((pagina, w), 1)
  }

  /**
   * Gets a list of (page, word),List(1,1,1,1,1...)
   * @param t
   * @param nums
   * @return for each page and word the number of occurrences
   */
  def reducingTF(t: (String, String), nums: List[Int]): ((String, String), Int) = {
    (t, nums.sum)
  }

  /**
   * Mapper to calculate the IDF.
   * @param pagina Name of page
   * @param words  List of strings in page
   * @return List of (Word, 1)
   */
  def mappingIDF(pagina: String, words: List[String]): List[(String, Int)] = {
    (for (w: String <- words.toSet) yield (w, 1)).toList
  }

  /**
   * Reducer to calculate the IDF
   * @param word Name of page
   * @param nums List of 1s
   * @return Returns the pair of (Word, Sum of nums)
   */
  def reducingIDF(word: String, nums: List[Int]): (String, Int) = {
    (word, nums.sum)
  }
}
