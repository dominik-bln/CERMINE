package pl.edu.icm.cermine.evaluation

import java.io.{FileInputStream, File}
import java.util.Locale
import org.w3c.dom.{NodeList, Node}
import pl.edu.icm.ceon.scala_commons.xml.XPathEvaluator
import pl.edu.icm.cermine.evaluation.tools.{StringTools, CosineDistance}
import scala.collection.immutable.IndexedSeq
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics
import scala.collection.mutable.ListBuffer
import org.apache.commons.io.IOUtils

/**
 * @author Mateusz Fedoryszak (m.fedoryszak@icm.edu.pl)
 */
object BibRefEvaluator {
  object cosineSimilarity extends ((String, String) => Double) {
    private val dist = new CosineDistance

    def apply(s1: String, s2: String): Double =
      dist.compare(StringTools.tokenize(s1), StringTools.tokenize(s2))
  }

  def traverse(f: File): Stream[File] = f #:: Option(f.listFiles()).toStream.flatten.flatMap(traverse)

  /**
   * @return first element - PubMed, second - CERMINE
   */
  def extractFilePairs(path: File, ext: String) = {
    path.listFiles().groupBy(absolutePathWithoutExtension).values.flatMap { files =>
      val expected = List("nxml", ext)
      def findByExtension(files: Traversable[File])(extension: String) =
        files.find(_.getName.toLowerCase(Locale.ENGLISH).endsWith("." + extension))

      val pair = expected.map(findByExtension(files))

      pair match {
        case List(Some(f1), Some(f2)) => Some((f1, f2))
        case _ => None
      }
    }
  }

  def absolutePathWithoutExtension(f: File) =
    if (f.getName.split('.').length > 1)
      f.getAbsolutePath.split('.').init.mkString(".")
    else
      f.getAbsolutePath

  def extension(f: File): Option[String] = f.getName.split(".").lastOption

  def extractCitations(f: File) = XPathEvaluator.fromInputStream(new FileInputStream(f))
    .asNodes("//ref-list/ref").map(extractTextContent).map(cleanWhitespaces)

  def extractTextContent(n: Node):String = {
    def scalifyNodeList(ns: NodeList): List[Node] = {
      val buffer = ListBuffer[Node]()
      for(i <- 0 until ns.getLength) {
        buffer.append(ns.item(i))
      }
      buffer.toList
    }
    if (n.getNodeType == Node.TEXT_NODE)
      n.getNodeValue
    else {
      scalifyNodeList(n.getChildNodes).map(extractTextContent).mkString(" ")
    }
  }

  def computeStats(ref: String, cands: IndexedSeq[String]): Option[(Double, Double, Double, Double)] =
    if (cands.length < 2)
      None
    else {
      val sims = cands.map(cand => cosineSimilarity(ref, cand)).sortBy(-_)
      val statistics = new DescriptiveStatistics()
      sims.tail.foreach(statistics.addValue)

      Some((sims.head, sims.tail.head, statistics.getMean, statistics.getStandardDeviation))
    }

  def printCitationDiffs(workingDir: File, cermExt: String) {
    val pairs = traverse(workingDir).filter(_.isDirectory).flatMap(x => extractFilePairs(x, cermExt))

    val results = for {
      (fPmc, fCermine) <- pairs
      citsPmc = extractCitations(fPmc).length
      citsCermine = extractCitations(fCermine).length
    } yield (citsPmc, citsCermine, fPmc.getParentFile.getAbsolutePath)

    results.map(_.productIterator.mkString(",")).foreach(println)
  }

  def hasSimilar(threshold: Double, golds: IndexedSeq[String])(candidate: String) = {
    golds.exists(cosineSimilarity(_, candidate) > threshold)
  }

  case class BasicStats(path: String, precision: Double, recall: Double, isecCount: Int, cermineCount: Int, pmcCount: Int) {}

  def basicStats(workingDir: File, similarityThreshold: Double, cermExt: String) = {
    val pairs = traverse(workingDir).filter(_.isDirectory).flatMap(x => extractFilePairs(x, cermExt))

    pairs.map { case (fPmc, fCermine) =>
      val citsPmc = extractCitations(fPmc)
      val citsCermine = extractCitations(fCermine)

      val isec = citsCermine.count(hasSimilar(similarityThreshold, citsPmc))

      val precision = isec.toDouble / citsCermine.length
      val recall = isec.toDouble / citsPmc.length

      BasicStats(fPmc.getParentFile.getAbsolutePath, precision, recall, isec, citsCermine.length, citsPmc.length)
    }
  }
  
  def basicStatsFull(workingDir: File, similarityThreshold: Double, cermExt: String) = {
    val pairs = traverse(workingDir).filter(_.isDirectory).flatMap(x => extractFilePairs(x, cermExt))

    pairs.map { case (fPmc, fCermine) =>
      val citsPmc = extractCitations(fPmc)
      val citsCermine = extractCitations(fCermine)

      val isec = citsCermine.count(hasSimilar(similarityThreshold, citsPmc))

      var precision = isec.toDouble / citsCermine.length
      if (isec+citsCermine.length == 0) {
        precision = 1.
      } else if (citsCermine.length == 0) {
        precision = 0.
      }
      var recall = isec.toDouble / citsPmc.length
      if (isec+citsPmc.length == 0) {
        recall = 1.
      } else if (citsPmc.length == 0) {
        recall = 0.
      }

      BasicStats(fPmc.getAbsolutePath, precision, recall, isec, citsCermine.length, citsPmc.length)
    }
  }

  def precisionRecall(workingDir: File, threshold: Double, cermExt: String) = {
    val pairs = traverse(workingDir).filter(_.isDirectory).flatMap(x => extractFilePairs(x, cermExt))

    val (precisionsRaw, recallsRaw) = pairs.map { case (fPmc, fCermine) =>
      val citsPmc = extractCitations(fPmc)
      val citsCermine = extractCitations(fCermine)

      val isec = citsCermine.count(hasSimilar(threshold, citsPmc))

      val precision = isec.toDouble / citsCermine.length
      val recall = isec.toDouble / citsPmc.length
        
      (precision, recall)
    }.unzip

    val precisions = precisionsRaw.filterNot(_.isNaN)
    val recalls = recallsRaw.filterNot(_.isNaN)

    (precisions.sum / precisions.length, recalls.sum / recalls.length)
  }

  def cleanWhitespaces: String => String = _.replaceAll("""\s+""", " ")

  def printSameCitationNumberComparison(workingDir: File, cermExt: String) {
    val pairs = traverse(workingDir).filter(_.isDirectory).flatMap(x => extractFilePairs(x, cermExt))

    pairs
      .map{ case (fPmc, fCermine) =>
        (fPmc.getParentFile.getAbsolutePath, extractCitations(fPmc), extractCitations(fCermine)) }
      .filter{ case (_, pmc, cermine) => pmc.length == cermine.length }
      .flatMap{ case (name, pmc, cermine) =>
        for ((pmcCit, cermineCit) <- pmc zip cermine)
          yield (name, pmcCit, cermineCit, cosineSimilarity(pmcCit, cermineCit))
      }
      .map(_.productIterator.mkString(","))
      .foreach(println)
  }

  def printBasicStats(workingDir: File, cermExt: String) {
    basicStats(workingDir, 0.6, cermExt).foreach(println)
  }

  def printPrecisionRecall(workingDir: File, cermExt: String) {
    val (precision, recall) = precisionRecall(workingDir, 0.6, cermExt)
    println(s"Precision: $precision")
    println(s"Recall:    $recall")
    println(s"F1:        ${2 * precision * recall / (precision + recall) }")
  }

  def printStats(workingDir: File, cermExt: String) {
    val pairs = traverse(workingDir).filter(_.isDirectory).flatMap(x => extractFilePairs(x, cermExt))

    val results = for {
      (f1, f2) <- pairs
      cands = extractCitations(f2)
      ref <- extractCitations(f1)
      res <- computeStats(ref, cands)
    } yield res

    results.foreach(println)
  }



  def main(args: Array[String]) {
    val workingDir = new File(args(0))

    val stats = basicStatsFull(workingDir, 0.6, args(1))
    stats.foreach(println)
    val precisions = stats.map(_.precision)
    val recalls = stats.map(_.recall)
    val f1s = stats.map(x => if (x.precision + x.recall == 0) 0 else 2 * x.precision * x.recall / (x.precision + x.recall))
    println(s"Average precision: ${precisions.sum / precisions.length}")
    println(s"Average recall: ${recalls.sum / recalls.length}")
    println(s"Average F1: ${f1s.sum / f1s.length}")
    
  }
}
